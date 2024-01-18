package com.marginallyclever.ro3.node.nodes;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.marginallyclever.convenience.helpers.BigMatrixHelper;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.ro3.Registry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class CameraTest {
    @BeforeAll
    public static void setup() {
        Registry.start();
    }

    @Test
    public void testPanTiltInverses() {
        Camera camera = Registry.getActiveCamera();

        for (int pan = -180; pan <= 180; pan += 10) {
            for (int tilt = -90; tilt <= 90; tilt += 10) {
                double[] before = new double[]{pan, tilt};
                Matrix3d panTiltMatrix = camera.buildPanTiltMatrix(before);
                Matrix4d matrix = new Matrix4d(panTiltMatrix, new Vector3d(), 1.0);
                double[] after = camera.getPanTiltFromMatrix(matrix);
                //System.out.println("before="+before[0]+","+before[1]+" after="+after[0]+","+after[1]);
                Assertions.assertArrayEquals(before, after, 0.01);
            }
        }
    }

    @Test
    public void getSetOrbitRadius() {
        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        camera.setOrbitRadius(1.0);
        Assertions.assertEquals(1.0, camera.getOrbitRadius(), 0.01);
        camera.setOrbitRadius(2.0);
        Assertions.assertEquals(2.0, camera.getOrbitRadius(), 0.01);
    }

    @Test
    public void getSetFOVY() {
        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        camera.setFovY(1.0);
        Assertions.assertEquals(1.0, camera.getFovY(), 0.01);
        camera.setFovY(2.0);
        Assertions.assertEquals(2.0, camera.getFovY(), 0.01);
    }

    @Test
    public void getSetNearZ() {
        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        camera.setNearZ(1.0);
        Assertions.assertEquals(1.0, camera.getNearZ(), 0.01);
        camera.setNearZ(2.0);
        Assertions.assertEquals(2.0, camera.getNearZ(), 0.01);
    }

    @Test
    public void getSetFarZ() {
        Camera camera = Registry.getActiveCamera();
        assert camera != null;
        camera.setFarZ(1.0);
        Assertions.assertEquals(1.0, camera.getFarZ(), 0.01);
        camera.setFarZ(2.0);
        Assertions.assertEquals(2.0, camera.getFarZ(), 0.01);
    }

    @Test
    public void pedestal() {
        Camera camera = new Camera();
        var p0 = camera.getPosition();
        camera.pedestal(1.0);
        var p1 = camera.getPosition();
        Assertions.assertEquals(p0.x, p1.x, 0.01);
        Assertions.assertEquals(p0.y + 1.0, p1.y, 0.01);
        Assertions.assertEquals(p0.z, p1.z, 0.01);
    }

    @Test
    public void truck() {
        Camera camera = new Camera();
        var p0 = camera.getPosition();
        camera.truck(1.0);
        var p1 = camera.getPosition();
        Assertions.assertEquals(p0.x + 1.0, p1.x, 0.01);
        Assertions.assertEquals(p0.y, p1.y, 0.01);
        Assertions.assertEquals(p0.z, p1.z, 0.01);
    }

    @Test
    public void dolly() {
        Camera camera = new Camera();
        var p0 = camera.getPosition();
        camera.dolly(1.0);
        var p1 = camera.getPosition();
        Assertions.assertEquals(p0.x, p1.x, 0.01);
        Assertions.assertEquals(p0.y, p1.y, 0.01);
        Assertions.assertEquals(p0.z + 1.0, p1.z, 0.01);
    }

    @Test
    public void tilt() {
        Camera camera = new Camera();
        var p0 = camera.getPanTiltFromMatrix(camera.getLocal());
        camera.tilt(1.0);
        var p1 = camera.getPanTiltFromMatrix(camera.getLocal());
        Assertions.assertEquals(p0[0], p1[0], 0.01);
        Assertions.assertEquals(p0[1] + 1.0, p1[1], 0.01);
    }

    @Test
    public void pan() {
        Camera camera = new Camera();
        var p0 = camera.getPanTiltFromMatrix(camera.getLocal());
        camera.tilt(-90.0);
        camera.pan(90.0);
        var p1 = camera.getPanTiltFromMatrix(camera.getLocal());
        Assertions.assertEquals(p0[0], p1[0], 0.01);
        Assertions.assertEquals(p0[1] - 90, p1[1], 0.01);
    }

    @Test
    public void panTilt() {
        Camera camera = new Camera();
        camera.panTilt(90.0, 90.0);
        var p1 = camera.getPanTiltFromMatrix(camera.getLocal());
        Assertions.assertEquals(90.0, p1[0], 0.01);
        Assertions.assertEquals(90.0, p1[1], 0.01);
    }

    @Test
    public void roll() {
        Camera camera = new Camera();
        var p0 = MatrixHelper.getXAxis(camera.getLocal());
        camera.roll(90.0);
        var p1 = MatrixHelper.getXAxis(camera.getLocal());
        Assertions.assertEquals(p0.x-1, p1.x, 0.01);
        Assertions.assertEquals(p0.y+1, p1.y, 0.01);
        Assertions.assertEquals(p0.z, p1.z, 0.01);
    }

    @Test
    public void orbit() {
        Camera camera = new Camera();
        var p0 = camera.getPanTiltFromMatrix(camera.getLocal());
        camera.getLocal().rotX(Math.toRadians(90));
        camera.orbit(90.0,-90);
        var p1 = camera.getPanTiltFromMatrix(camera.getLocal());
        Assertions.assertEquals(p0[0] + 90.0, p1[0], 0.01);
        Assertions.assertEquals(p0[1], p1[1], 0.01);
    }

    @Test
    public void getViewMatrix() {
        Camera camera = new Camera();
        Matrix4d m = camera.getViewMatrix();
        Assertions.assertArrayEquals(BigMatrixHelper.matrix4dToArray(m),
                BigMatrixHelper.matrix4dToArray(MatrixHelper.createIdentityMatrix4()), 0.01);
    }

    @Test
    public void getProjectionMatrix() {
        Camera camera = new Camera();
        Matrix4d m = camera.getChosenProjectionMatrix(800,600);
        Matrix4d p = new Matrix4d(
                1.299038105676658, 0.0, 0.0, 0.0,
                0.0, 1.7320508075688772, 0.0, 0.0,
                0.0, 0.0, -1.0002000200020002, -1.0,
                0.0, 0.0, -2.0020002000200020002, 0.0
        );
        Assertions.assertArrayEquals(
                BigMatrixHelper.matrix4dToArray(m),
                BigMatrixHelper.matrix4dToArray(p), 0.01);
    }

    @Test
    public void getOrthographicMatrix() {
        Camera camera = new Camera();
        camera.setDrawOrthographic(true);
        Assertions.assertTrue(camera.getDrawOrthographic());
        Matrix4d m = camera.getChosenProjectionMatrix(800,600);
        Matrix4d p = new Matrix4d(
                0.15,0.0,0.0,0.0,
                0.0,0.2,0.0,0.0,
                0.0,0.0,-0.0020002000200020002,0.0,
                0.0,0.0,-1.0020002000200020002,1.0
        );
        Assertions.assertArrayEquals(
                BigMatrixHelper.matrix4dToArray(m),
                BigMatrixHelper.matrix4dToArray(p), 0.01);
    }

    @Test
    public void canRotate() {
        Camera camera = new Camera();
        camera.setCanRotate(false);
        var p0 = camera.getPanTiltFromMatrix(camera.getLocal());
        camera.panTilt(90,90);
        var p1 = camera.getPanTiltFromMatrix(camera.getLocal());
        Assertions.assertEquals(p0[0], p1[0], 0.01);
        Assertions.assertEquals(p0[1], p1[1], 0.01);
    }

    @Test
    public void canTranslate() {
        Camera camera = new Camera();
        camera.setCanTranslate(false);
        var p0 = camera.getPosition();
        camera.truck(1.0);
        camera.pedestal(1.0);
        camera.dolly(1.0);
        camera.orbit(1.0,1.0);
        camera.orbitDolly(2.0);
        var p1 = camera.getPosition();
        Assertions.assertEquals(p0.x, p1.x, 0.01);
        Assertions.assertEquals(p0.y, p1.y, 0.01);
        Assertions.assertEquals(p0.z, p1.z, 0.01);
    }

    @Test
    public void json() {
        Camera a = new Camera();
        a.setDrawOrthographic(true);
        a.setNearZ(1.0);
        a.setFarZ(2.0);
        a.setFovY(3.0);
        a.setOrbitRadius(4.0);
        a.panTilt(5.0,6.0);
        a.truck(7.0);
        a.setCanRotate(false);
        a.setCanTranslate(false);
        Camera b = new Camera();
        b.fromJSON(a.toJSON());
        Assertions.assertEquals(a.getDrawOrthographic(),b.getDrawOrthographic());
        Assertions.assertEquals(a.getNearZ(),b.getNearZ(),0.01);
        Assertions.assertEquals(a.getFarZ(),b.getFarZ(),0.01);
        Assertions.assertEquals(a.getFovY(),b.getFovY(),0.01);
        Assertions.assertEquals(a.getOrbitRadius(),b.getOrbitRadius(),0.01);
        Assertions.assertArrayEquals(a.getPanTiltFromMatrix(a.getLocal()),b.getPanTiltFromMatrix(b.getLocal()),0.01);
        Assertions.assertEquals(a.getPosition(),b.getPosition());
        Assertions.assertEquals(a.getCanRotate(),b.getCanRotate());
        Assertions.assertEquals(a.getCanTranslate(),b.getCanTranslate());
    }
}
