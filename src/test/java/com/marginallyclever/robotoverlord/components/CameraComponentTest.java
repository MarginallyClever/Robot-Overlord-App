package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.entity.Entity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

public class CameraComponentTest {
    @Test
    public void saveAndLoad() throws Exception {
        Entity entity = new Entity();
        CameraComponent a = new CameraComponent();
        CameraComponent b = new CameraComponent();
        entity.addComponent(a);

        ComponentTest.saveAndLoad(a,b);

        a.setOrbitDistance(a.getOrbitDistance()+10.0);
        ComponentTest.saveAndLoad(a,b);
    }

    @Test
    public void changingOrbitDistanceDoesNotChangeOrbitPoint() {
        Entity entity = new Entity();
        CameraComponent a = new CameraComponent();
        entity.addComponent(a);

        Vector3d beforeOP = a.getOrbitPoint();
        a.setOrbitDistance(a.getOrbitDistance()+10.0);
        Vector3d afterOP = a.getOrbitPoint();
        Assertions.assertEquals(beforeOP,afterOP);
    }

    @Test
    public void testSetPanTilt() {
        CameraComponent a = new CameraComponent();
        a.setPan(30);
        a.setTilt(40);
        Assertions.assertEquals(30, a.getPan(), 0.0001);
        Assertions.assertEquals(40, a.getTilt(), 0.0001);
    }

    @Test
    public void testToFromPanTilt() {
        CameraComponent a = new CameraComponent();
        Matrix3d m = a.buildPanTiltMatrix(30,40);
        double [] v = a.getPanTiltFromMatrix(m);
        Assertions.assertEquals(30,v[0],0.0001);
        Assertions.assertEquals(40,v[1],0.0001);
    }

    @Test
    public void testLookAt() {
        Entity mainCamera = new Entity("Main Camera");
        CameraComponent camera = new CameraComponent();
        mainCamera.addComponent(camera);
        PoseComponent pose = mainCamera.getComponent(PoseComponent.class);
        pose.setPosition(new Vector3d(-40,-40,30));
        camera.lookAt(new Vector3d(0,0,0));

        Matrix4d cameraPose = mainCamera.getComponent(PoseComponent.class).getWorld();
        Vector3d zAxis = MatrixHelper.getZAxis(cameraPose);
        Assertions.assertEquals(zAxis.x,-0.6246,0.01);
        Assertions.assertEquals(zAxis.y,-0.6246,0.01);
        Assertions.assertEquals(zAxis.z,0.469,0.01);

        Vector3d pos = MatrixHelper.getPosition(cameraPose);
        Assertions.assertEquals(pos.x,-40,0.01);
        Assertions.assertEquals(pos.y,-40,0.01);
        Assertions.assertEquals(pos.z,30,0.01);

        Vector3d orbitPoint = camera.getOrbitPoint();
        Assertions.assertEquals(orbitPoint.x,0,0.01);
        Assertions.assertEquals(orbitPoint.y,0,0.01);
        Assertions.assertEquals(orbitPoint.z,0,0.01);
    }
}
