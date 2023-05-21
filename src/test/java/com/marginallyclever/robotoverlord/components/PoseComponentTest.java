package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class PoseComponentTest {
    @Test
    public void addPoseToEntity() {
        Entity e = new Entity();
        e.addComponent(new PoseComponent());
        Assertions.assertInstanceOf(PoseComponent.class,e.getComponent(0));
    }

    @Test
    public void movePose() {
        PoseComponent p = new PoseComponent();
        Assertions.assertEquals(new Vector3d(0,0,0),p.getPosition());

        Vector3d a = p.getPosition();
        Vector3d xPlus1 = new Vector3d(1,0,0);
        a.add(xPlus1);
        p.setPosition(a);
        Assertions.assertEquals(xPlus1,p.getPosition());

        p.setPosition(new Vector3d(0,0,0));
        Vector3d before = new Vector3d(Math.toRadians(5),Math.toRadians(10),Math.toRadians(15));
        p.setRotation(before);
        Vector3d after = p.getRotation();
        Assertions.assertEquals(before,after);
    }

    @Test
    public void testNestedPosePosition() {
        Entity root = new Entity();
        Entity e0 = new Entity();
        EntityManager entityManager = new EntityManager();
        entityManager.addEntityToParent(e0,root);

        PoseComponent p0 = root.getComponent(PoseComponent.class);
        PoseComponent p1 = e0.getComponent(PoseComponent.class);

        p0.setPosition(new Vector3d(1, 0, 0));
        p1.setPosition(new Vector3d(2, 0, 0));
        Vector3d sumPosition = new Vector3d();
        p1.getWorld().get(sumPosition);
        Assertions.assertEquals(new Vector3d(3, 0, 0), sumPosition);
    }

    @Test
    public void testNestedPoseRotation() {
        EntityManager entityManager = new EntityManager();
        Entity root = new Entity();
        Entity e0 = new Entity();
        entityManager.addEntityToParent(e0,root);

        PoseComponent p0 = root.getComponent(PoseComponent.class);
        PoseComponent p1 = e0.getComponent(PoseComponent.class);

        Matrix3d a1 = new Matrix3d();
        Matrix3d b1 = new Matrix3d();
        Matrix3d c1 = new Matrix3d();
        a1.rotX(Math.toRadians(10));
        b1.rotY(Math.toRadians(20));
        c1.mul(a1,b1);

        p0.setLocalMatrix3(a1);
        p1.setLocalMatrix3(b1);
        Matrix4d sumMatrixes = p1.getWorld();
        Matrix3d sumRotation = new Matrix3d();
        sumMatrixes.get(sumRotation);
        Assertions.assertEquals(c1,sumRotation);
    }

    @Test
    public void saveAndLoad() throws Exception {
        PoseComponent a = new PoseComponent();
        PoseComponent b = new PoseComponent();
        ComponentTest.saveAndLoad(a,b);

        a.setPosition(new Vector3d(1,2,3));
        ComponentTest.saveAndLoad(a,b);

        a.setRotation(new Vector3d(4,5,6));
        ComponentTest.saveAndLoad(a,b);

        a.setScale(new Vector3d(7,8,9));
        ComponentTest.saveAndLoad(a,b);
    }
}
