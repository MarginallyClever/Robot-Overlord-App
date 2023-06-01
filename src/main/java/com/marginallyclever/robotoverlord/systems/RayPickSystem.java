package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.RayHit;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;

import java.util.*;

/**
 * The {@link RayPickSystem} is separate from {@link com.marginallyclever.convenience.helpers.IntersectionHelper}
 * because it references {@link EntityManager}, which is outside the scope of the Convenience Library.
 *
 * @since 2.6.1
 * @author Dan Royer
 */
public class RayPickSystem {
    private final EntityManager entityManager;

    public RayPickSystem(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    /**
     * Traverse the scene Entities and find the nearest {@link ShapeComponent} that collides with the ray.
     * This computes all intersections and then sorts them by distance, so it is a good idea to keey the ray length
     * short.
     * @param ray the ray to test.
     * @return the nearest {@link RayHit} by the ray, or null if no entity was hit.
     */
    public RayHit getFirstHit(Ray ray) {
        //
        List<RayHit> rayHits = findRayIntersections(ray);
        if(rayHits.isEmpty()) return null;
        rayHits.sort(Comparator.comparingDouble(o -> o.distance));
        return rayHits.get(0);
    }

    /**
     * Traverse the scene Entities and find all the {@link ShapeComponent}s that collide with the ray.
     * @param ray the ray to test.
     * @return all {@link RayHit} by the ray.  May be an empty list.
     */
    public List<RayHit> findRayIntersections(Ray ray) {
        List<RayHit> rayHits = new ArrayList<>();

        Queue<Entity> toTest = new LinkedList<>(entityManager.getEntities());
        while(!toTest.isEmpty()) {
            Entity entity = toTest.remove();
            toTest.addAll(entity.getChildren());

            ShapeComponent shape = entity.getComponent(ShapeComponent.class);
            if(shape==null) continue;
            RayHit hit = shape.intersect(ray);
            if(hit!=null) rayHits.add(hit);
        }
        return rayHits;
    }
}
