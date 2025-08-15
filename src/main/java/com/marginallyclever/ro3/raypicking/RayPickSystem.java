package com.marginallyclever.ro3.raypicking;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.pathtracer.PathMesh;
import com.marginallyclever.ro3.apps.pathtracer.PathTriangle;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.VertexProvider;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.material.Material;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.*;

/**
 * A system for finding the nearest {@link MeshInstance} that collides with a ray.
 */
public class RayPickSystem {
    private static final Logger logger = LoggerFactory.getLogger(RayPickSystem.class);
    // all the MeshInstances in the scene.
    private final List<MeshInstance> sceneElements = new ArrayList<>();
    private int numEmissiveTriangles;

    /**
     * A cache of meshes transformed into world space.  This is much faster than transforming each ray into local
     * space for every test.
     */
    private final Map<MeshInstance, PathMesh> cache = new HashMap<>();
    // all meshes that have an emissive material.
    private final List<MeshInstance> emissiveMeshes = new ArrayList<>();

    public RayPickSystem() {
        super();
    }

    public void reset(boolean optimize) {
        logger.debug("reset");
        cache.clear();
        emissiveMeshes.clear();
        sceneElements.clear();
        buildSceneList(optimize);
    }

    private void buildSceneList(boolean optimize) {
        logger.debug("start");
        numEmissiveTriangles = 0;
        // build the list of scene elements once
        Queue<Node> toTest = new LinkedList<>();
        toTest.add(Registry.getScene());
        while (!toTest.isEmpty()) {
            Node node = toTest.remove();
            if(node instanceof MeshInstance meshInstance) {
                sceneElements.add(meshInstance);
                if(optimize) {
                    cache.put(meshInstance, createPathMesh(meshInstance));
                }
                var mat = getMaterial(meshInstance);
                if(mat!=null && mat.isEmissive()) {
                    emissiveMeshes.add(meshInstance);
                    if(optimize) {
                        numEmissiveTriangles += cache.get(meshInstance).getTriangleCount();
                    }
                }
            }
            toTest.addAll(node.getChildren());
        }
        logger.debug("done with {} cached items", cache.size());
    }

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
     * Traverse the scene Entities and find the nearest {@link MeshInstance} that collides with the ray.
     * This computes all intersections and then sorts them by distance, so it is a good idea to keey the ray length
     * short.
     * @param ray the ray to test.
     * @param optimize true if extra steps should be taken to optimize, typically for path tracing.
     * @return the nearest {@link RayHit} by the ray, or null if no entity was hit.
     */
    public RayHit getFirstHit(Ray ray,boolean optimize) {
        List<RayHit> rayHits = findRayIntersections(ray,optimize);
        if(rayHits.isEmpty()) return null;
        rayHits.sort(Comparator.comparingDouble(RayHit::distance));
        // handle the case where the ray starts inside the mesh ("shadow acne")
        while(!rayHits.isEmpty() && rayHits.getFirst().distance() < 1e-9) {
            rayHits.removeFirst();
        }
        if(rayHits.isEmpty()) return null;
        return rayHits.getFirst();
    }

    /**
     * Traverse the scene {@link Node}s and find all the {@link MeshInstance}s that collide with the ray.
     * @param ray the ray to test.
     * @param optimize true if extra steps should be taken to optimize, typically for path tracing.
     * @return all {@link RayHit} by the ray.  It may be an empty list.
     */
    public List<RayHit> findRayIntersections(Ray ray,boolean optimize) {
        List<RayHit> rayHits = new LinkedList<>();

        for(var meshInstance : sceneElements) {
            if(!optimize || meshInstance.getMesh() instanceof Sphere) {
                RayHit hit = meshInstance.intersect(ray);
                if (hit != null) rayHits.add(hit);
                continue;
            }

            if(optimize) {
                PathMesh worldMesh = getCachedMesh(meshInstance);
                if (worldMesh == null) continue;

                RayHit hit2 = worldMesh.intersect(ray);
                if (hit2 != null) {
                    rayHits.add(new RayHit(meshInstance, hit2.distance(), hit2.normal(), hit2.point(),hit2.triangle()));
                }
            }
        }
        return rayHits;
    }

    private PathMesh getCachedMesh(MeshInstance meshInstance) {
        if (!cache.containsKey(meshInstance)) {
            // create a copy of meshInstance that is transformed to world space.
            cache.put(meshInstance, createPathMesh(meshInstance));
        }
        return cache.get(meshInstance);
    }

    /**
     * Create a copy of mesh transformed to world space.
     * @param meshInstance the meshInstance to transform
     * @return the new mesh.
     */
    private PathMesh createPathMesh(MeshInstance meshInstance) {
        PathMesh newMesh = new PathMesh();
        Mesh oldMesh = meshInstance.getMesh();
        var matrix = meshInstance.getWorld();

        VertexProvider vertexProvider = oldMesh.getVertexProvider();
        var numVertexes = vertexProvider.provideCount();
        for(int i = 0; i < numVertexes; i+=3) {
            Point3d p0 = vertexProvider.provideVertex(i    );
            Point3d p1 = vertexProvider.provideVertex(i + 1);
            Point3d p2 = vertexProvider.provideVertex(i + 2);
            matrix.transform(p0);
            matrix.transform(p1);
            matrix.transform(p2);
            Vector3d n = vertexProvider.provideNormal(i);
            matrix.transform(n);
            newMesh.addTriangle(new PathTriangle(p0,p1,p2,n));
        }
        newMesh.buildOctree();
        return newMesh;
    }

    public int getNumberOfEmissiveTriangles() {
        return numEmissiveTriangles;
    }

    public RayHit getEmissiveSurface(int index) {
        for(var meshInstance : emissiveMeshes) {
            PathMesh worldMesh = getCachedMesh(meshInstance);
            if(worldMesh==null) continue;
            if(worldMesh.getTriangleCount() >= index) {
                index -= worldMesh.getTriangleCount();
                continue;
            }

            PathTriangle pt = worldMesh.getTriangle(index);
            if(pt==null) continue;
            return new RayHit(meshInstance, 0, pt.normal, pt.getRandomPointInside(), pt);
        }
        return null;
    }

    public RayHit getRandomEmissiveSurface() {
        for(var meshInstance : emissiveMeshes) {
            PathMesh worldMesh = getCachedMesh(meshInstance);
            if(worldMesh==null) continue;
            PathTriangle pt = worldMesh.getRandomTriangle();
            if(pt==null) continue;
            return new RayHit(meshInstance, 0, pt.normal, pt.getRandomPointInside(), pt);
        }
        return null;
    }
}
