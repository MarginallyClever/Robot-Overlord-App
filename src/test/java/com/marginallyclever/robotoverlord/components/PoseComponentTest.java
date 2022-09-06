package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.ComponentTest;
import com.marginallyclever.robotoverlord.Entity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
    public void testNestedPoses() {
        Entity root = new Entity();
        PoseComponent p0 = new PoseComponent();
        root.addComponent(p0);

        Entity e0 = new Entity();
        PoseComponent p1 = new PoseComponent();
        e0.addComponent(p1);

        root.addChild(e0);

        p0.setPosition(new Vector3d(1,0,0));
        p1.setPosition(new Vector3d(-2,0,0));
        Vector3d sum = new Vector3d();
        p1.getWorld().get(sum);
        Assertions.assertEquals(new Vector3d(-1,0,0),sum);
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
