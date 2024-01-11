package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import static org.junit.jupiter.api.Assertions.*;

class PoseTest {
    @Test
    void testGetSetLocal() {
        Pose pose = new Pose();
        Matrix4d matrix = MatrixHelper.createIdentityMatrix4();
        pose.setLocal(matrix);
        assertEquals(matrix, pose.getLocal());
    }

    @Test
    void testGetSetWorld1() {
        Pose a = new Pose();
        Pose b = new Pose();
        a.addChild(b);
        Matrix4d matrix = MatrixHelper.createIdentityMatrix4();
        matrix.setTranslation(new Vector3d(1,2,3));
        a.setLocal(matrix);
        b.setLocal(matrix);
        matrix.setTranslation(new Vector3d(2,4,6));
        assertEquals(matrix, b.getWorld());
    }

    @Test
    void testGetSetWorld2() {
        Pose a = new Pose();
        Pose b = new Pose();
        a.addChild(b);
        Matrix4d matrix = MatrixHelper.createIdentityMatrix4();
        matrix.setTranslation(new Vector3d(1,2,3));
        a.setLocal(matrix);
        b.setWorld(matrix);
        assertEquals(matrix, b.getWorld());
        matrix.setTranslation(new Vector3d(0,0,0));
        assertEquals(matrix, b.getLocal());
    }

    @Test
    void testGetSetRotationEuler() {
        Pose pose = new Pose();
        Vector3d vector = new Vector3d(1.0, 2.0, 3.0);
        pose.setRotationEuler(vector, MatrixHelper.EulerSequence.YXZ);
        var result = pose.getRotationEuler(MatrixHelper.EulerSequence.YXZ);
        Assertions.assertEquals(vector.x, result.x,1e-4);
        Assertions.assertEquals(vector.y, result.y,1e-4);
        Assertions.assertEquals(vector.z, result.z,1e-4);
    }

    @Test
    void testGetSetPosition() {
        Pose pose = new Pose();
        Vector3d vector = new Vector3d(1.0, 2.0, 3.0);
        pose.setPosition(vector);
        var result = pose.getPosition();
        Assertions.assertEquals(vector.x, result.x,1e-4);
        Assertions.assertEquals(vector.y, result.y,1e-4);
        Assertions.assertEquals(vector.z, result.z,1e-4);
    }
}