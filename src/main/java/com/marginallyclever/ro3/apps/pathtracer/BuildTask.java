package com.marginallyclever.ro3.apps.pathtracer;


import com.marginallyclever.ro3.mesh.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class BuildTask extends RecursiveTask<BVHNode> {
    public static final int PARALLEL_THRESHOLD = 256; // min #tris in range to justify parallel split

    private final List<PathTriangle> tris;

    public BuildTask(List<PathTriangle> tris) {
        this.tris = tris;
    }

    @Override
    protected BVHNode compute() {
        BVHNode node = new BVHNode();
        node.box = computeBounds();

        if (tris.size() <= BoundingVolumeHeirarchy.MAX_TRIANGLES_PER_LEAF) {
            node.tris = new ArrayList<>(tris);
            return node;
        }

        findBestSplit(node);
        return node;
    }

    private AABB computeBounds() {
        AABB box = new AABB();
        for(PathTriangle tri : tris) {
            box.union(tri.bounds);
        }
        return box;
    }

    private void findBestSplit(BVHNode node) {
        AABB parent = node.box;
        int count = tris.size();
        // Find best SAH split
        double bestCost = BoundingVolumeHeirarchy.C_INT * count;
        double leafCost = bestCost;
        int bestAxis = -1;
        int bestIndex = -1;
        double parentArea = parent.surfaceArea();

        // Temporary arrays sized to current count
        AABB[] leftBounds  = new AABB[count];
        AABB[] rightBounds = new AABB[count];

        for (int axis = 0; axis < 3; axis++) {
            final var myAxis = axis;
            tris.sort(Comparator.comparingDouble(t -> t.getCentroidAxis(myAxis)));

            // Prefix
            AABB acc = new AABB();
            for (int i = 0; i < count; i++) {
                acc = new AABB(acc);
                acc.union(tris.get(i).bounds);
                leftBounds[i] = acc;
            }
            // Suffix
            acc = new AABB();
            for (int i = count - 1; i >= 0; i--) {
                acc = new AABB(acc);
                acc.union(tris.get(i).bounds);
                rightBounds[i] = acc;
            }

            // Evaluate splits between i-1 | i
            for (int i = 1; i < count; i++) {
                int nLeft = i;
                int nRight = count - i;
                double sahCost = BoundingVolumeHeirarchy.C_TRAV
                        + (leftBounds [i - 1].surfaceArea() / parentArea) * nLeft  * BoundingVolumeHeirarchy.C_INT
                        + (rightBounds[i    ].surfaceArea() / parentArea) * nRight * BoundingVolumeHeirarchy.C_INT;
                if (sahCost < bestCost) {
                    bestCost = sahCost;
                    bestAxis = axis;
                    bestIndex = i;
                }
            }
        }

        // If no improvement over leaf cost keep as leaf
        if (bestCost >= leafCost) {
            node.tris = new ArrayList<>(tris);
            return;
        }

        // Split at best axis/index
        final var myAxis = bestAxis;
        tris.sort(Comparator.comparingDouble(t -> t.bounds.getCentroidAxis(myAxis)));

        BuildTask leftTask  = new BuildTask(tris.subList(0, bestIndex));
        BuildTask rightTask = new BuildTask(tris.subList(bestIndex, tris.size()));

        // Parallel only if large enough
        if (count >= PARALLEL_THRESHOLD) {
            leftTask.fork();
            node.right = rightTask.compute();
            node.left  = leftTask.join();
        } else {
            node.left  = leftTask.compute();
            node.right = rightTask.compute();
        }
    }
}
