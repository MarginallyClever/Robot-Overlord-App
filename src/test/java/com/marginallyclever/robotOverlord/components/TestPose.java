package com.marginallyclever.robotOverlord.components;

import com.marginallyclever.robotOverlord.Entity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;
import java.io.*;

public class TestPose {
    @Test
    public void addPoseToEntity() {
        Entity e = new Entity();
        e.addComponent(new Pose());
        Assertions.assertInstanceOf(Pose.class,e.getComponent(0));
    }

    @Test
    public void movePose() {
        Pose p = new Pose();
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
        Pose p0 = new Pose();
        root.addComponent(p0);

        Entity e0 = new Entity();
        Pose p1 = new Pose();
        e0.addComponent(p1);

        root.addChild(e0);

        p0.setPosition(new Vector3d(1,0,0));
        p1.setPosition(new Vector3d(-2,0,0));
        Vector3d sum = new Vector3d();
        p1.getWorld().get(sum);
        Assertions.assertEquals(new Vector3d(-1,0,0),sum);
    }
/*
    @Test
    public void serializePose() throws IOException {
        File f = File.createTempFile("pose-serialize",null);

        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        Pose p0 = new Pose();
        p0.save(writer);

        BufferedReader reader = new BufferedReader(new FileReader(f));
        Pose p1 = new Pose();
        p1.load(reader);

        Assertions.assertEquals(p0.toString(),p1.toString());
    }*/
}
