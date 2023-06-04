package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.RayHit;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.List;

public class RayPickSystemTest {
    private static EntityManager em;
    private static RayPickSystem rps;
    private static Entity entityA, entityB, entityC;

    @BeforeAll
    public static void setup() {
        em = new EntityManager();
        rps = new RayPickSystem(em);

        // arrange boxes b a c in a line

        entityA = new Entity("a");
        entityA.addComponent(new Box());
        em.addEntityToParent(entityA,em.getRoot());

        // behind a
        entityB = new Entity("b");
        entityB.addComponent(new Box());
        em.addEntityToParent(entityB,em.getRoot());
        entityB.getComponent(PoseComponent.class).setPosition(new Vector3d(-10,0,0));

        // behind ray
        entityC = new Entity("c");
        entityC.addComponent(new Box());
        em.addEntityToParent(entityC,em.getRoot());
        entityC.getComponent(PoseComponent.class).setPosition(new Vector3d(20,0,0));
    }

    /**
     * Test that the ray picks up the nearest object.
     * Test that the ray *can* pick up every object in its path.
     */
    @Test
    public void basic() {
        Ray ray = new Ray();
        ray.setOrigin(new Point3d(10,0,0));
        ray.setDirection(new Vector3d(-1,0,0));
        RayHit firstHit = rps.getFirstHit(ray);

        Assertions.assertNotNull(firstHit);
        Assertions.assertNotNull(firstHit.target);
        Assertions.assertEquals(entityA,firstHit.target.getEntity());
        Assertions.assertEquals(9.5,firstHit.distance,0.0001);

        List<RayHit> list = rps.findRayIntersections(ray);
        Assertions.assertEquals(2,list.size());
    }

    /**
     * Test that the ray is stopped by the max distance.
     * Test that the ray does not pick up anything in the negative direction.
     */
    @Test
    public void maxDistance() {
        Ray ray = new Ray(
                new Point3d(10,0,0),
                new Vector3d(-1,0,0),
                12);
        List<RayHit> list = rps.findRayIntersections(ray);

        Assertions.assertEquals(1,list.size());
        Assertions.assertEquals(9.5,list.get(0).distance,0.0001);
        Assertions.assertEquals(entityA,list.get(0).target.getEntity());
    }
}
