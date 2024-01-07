package com.marginallyclever.ro3.raypicking;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.MeshInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A system for finding the nearest {@link MeshInstance} that collides with a ray.
 */
public class RayPickSystem {
    private static final Logger logger = LoggerFactory.getLogger(RayPickSystem.class);

    public RayPickSystem() {
        super();
    }

    /**
     * Traverse the scene Entities and find the nearest {@link MeshInstance} that collides with the ray.
     * This computes all intersections and then sorts them by distance, so it is a good idea to keey the ray length
     * short.
     * @param ray the ray to test.
     * @return the nearest {@link RayHit} by the ray, or null if no entity was hit.
     */
    public RayHit getFirstHit(Ray ray) {
        //
        List<RayHit> rayHits = findRayIntersections(ray);
        if(rayHits.isEmpty()) return null;
        rayHits.sort(Comparator.comparingDouble(RayHit::distance));
        return rayHits.get(0);
    }

    /**
     * Traverse the scene {@link Node}s and find all the {@link MeshInstance}s that collide with the ray.
     * @param ray the ray to test.
     * @return all {@link RayHit} by the ray.  It may be an empty list.
     */
    public List<RayHit> findRayIntersections(Ray ray) {
        List<RayHit> rayHits = new ArrayList<>();

        Queue<Node> toTest = new LinkedList<>();
        toTest.add(Registry.getScene());
        while(!toTest.isEmpty()) {
            Node node = toTest.remove();
            toTest.addAll(node.getChildren());

            if(node instanceof MeshInstance mesh) {
                RayHit hit = mesh.intersect(ray);
                if (hit != null) rayHits.add(hit);
            }
        }
        return rayHits;
    }
}
