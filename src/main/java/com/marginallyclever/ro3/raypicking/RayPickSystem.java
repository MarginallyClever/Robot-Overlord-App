package com.marginallyclever.ro3.raypicking;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.pathtracer.PathMesh;
import com.marginallyclever.ro3.apps.pathtracer.PathTriangle;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;

import javax.vecmath.Point3d;
import java.util.*;

/**
 * <p>{@link RayPickSystem} is for finding the {@link MeshInstance}s that collide with a {@link Ray}. This is used for
 * ray picking and path tracing.</p>
 * <p>For path tracing, it is important to optimize the ray/mesh intersection tests as much as possible.  This is done
 * by transforming each mesh into world space in a {@link PathMesh}.  For ray picking, the optimization is less
 * important, so the meshes are tested in their local space.</p>
 * <p>To use the {@link RayPickSystem}, create an instance, call <code>reset(optimize)</code>, and then </p>
 */
public class RayPickSystem {
    private final List<MeshInstance> sceneElements = new ArrayList<>();
    private int numEmissiveTriangles;

    /**
     * A cache of meshes transformed into world space.  This is much faster than transforming each ray into local
     * space for every test.
     */
    private final Map<MeshInstance, PathMesh> cache = new HashMap<>();
    // all meshes that have an emissive material.
    private final List<MeshInstance> emissiveMeshes = new ArrayList<>();
    private boolean optimize = false;

    public RayPickSystem() {
        super();
    }

    /**
     * Reset the system and rebuild the list of scene elements.  This should be called whenever the scene graph changes.
     * @param optimize true if extra steps should be taken to optimize, typically for path tracing.
     *                 If false, the system will do less work and use less memory, but ray/mesh intersection tests
     *                 will be slower.
     */
    public void reset(boolean optimize) {
        this.optimize = optimize;
        cache.clear();
        emissiveMeshes.clear();
        sceneElements.clear();
        buildSceneList();
    }

    /**
     * Build the list of scene elements by traversing the scene graph.  Also count the number of emissive triangles if optimizing.
     */
    private void buildSceneList() {
        numEmissiveTriangles = 0;
        Queue<Node> toTest = new LinkedList<>();
        toTest.add(Registry.getScene());
        while (!toTest.isEmpty()) {
            Node node = toTest.remove();
            toTest.addAll(node.getChildren());
            if(node instanceof MeshInstance meshInstance) {
                sceneElements.add(meshInstance);

                if(optimize) {
                    var pathMesh = meshInstance.createPathMesh();
                    cache.put(meshInstance, pathMesh);
                    var mat = getMaterial(meshInstance);
                    if(mat!=null && mat.isEmissive()) {
                        emissiveMeshes.add(meshInstance);
                        numEmissiveTriangles += pathMesh.getTriangleCount();
                    }
                }
            }
        }
    }

    /**
     * Find the nearest {@link Material} in the scene graph, starting from the given {@link MeshInstance}.  The nearest
     * material is the first one found in the given mesh instance or its parents.
     * @param meshInstance the mesh instance to start searching from.
     * @return the nearest {@link Material}, or null if none is found.
     */
    public static Material getMaterial(Node meshInstance) {
        if(meshInstance==null) return null;

        var mat = meshInstance.findFirstSibling(Material.class);
        if(mat != null) return mat;

        // check all parents until a material is found
        var parent = meshInstance.getParent();
        while(parent != null) {
            mat = parent.findFirstSibling(Material.class);
            if(mat != null) return mat;
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Traverse the scene and find the nearest {@link MeshInstance} that collides with the ray.
     * This computes all intersections and then sorts them by distance, so it is a good idea to keep the ray length
     * short.
     * @param ray the ray to test.
     * @return the nearest {@link Hit} by the ray, or null if no entity was hit.
     */
    public Hit getFirstHit(Ray ray) {
        List<Hit> hits = findRayIntersections(ray);
        // handle the case where the ray starts inside the mesh ("shadow acne")
        while(!hits.isEmpty() && hits.getFirst().distance() < 1e-9) {
            hits.removeFirst();
        }
        if(hits.isEmpty()) return null;
        return hits.getFirst();
    }

    /**
     * Traverse the scene and find all the {@link MeshInstance}s that collide with the ray.
     * @param ray the ray to test.
     * @return all {@link Hit} by the ray, sorted by distance.
     */
    public List<Hit> findRayIntersections(Ray ray) {
        List<Hit> hits = new LinkedList<>();

        for(var meshInstance : sceneElements) {
            if(!optimize /*|| meshInstance.getMesh() instanceof Sphere*/) {
                Hit hit = meshInstance.intersect(ray);
                if (hit != null) {
                    hits.add(hit);
                }
                continue;
            }
            // optimized path: use the cached world mesh.
            PathMesh worldMesh = getCachedMesh(meshInstance);
            if (worldMesh == null) continue;

            Hit hit2 = worldMesh.intersect(ray);
            if (hit2 != null) {
                hits.add(new Hit(meshInstance, hit2.distance(), hit2.normal(), hit2.point(),hit2.triangle()));
            }
        }

        hits.sort(Comparator.comparingDouble(Hit::distance));

        return hits;
    }

    private PathMesh getCachedMesh(MeshInstance meshInstance) {
        if (!cache.containsKey(meshInstance)) {
            // create a copy of meshInstance that is transformed to world space.
            cache.put(meshInstance, meshInstance.createPathMesh());
        }
        return cache.get(meshInstance);
    }

    /**
     * Guaranteed to return one valid result if there is at least one emissive triangle in the scene.
     * @return a {@link Hit} on a random emissive surface, or null if there are no emissive surfaces.
     */
    public Hit getRandomEmissiveSurface() {
        for(var meshInstance : emissiveMeshes) {
            PathMesh worldMesh = getCachedMesh(meshInstance);
            if(worldMesh==null) continue;

            PathTriangle pt = worldMesh.getRandomTriangle();
            if(pt==null) continue;

            Point3d inside = pt.getRandomPointInside();

            return new Hit(meshInstance, 0, pt.getNormalAt(inside), inside, pt);
        }
        return null;
    }
}
