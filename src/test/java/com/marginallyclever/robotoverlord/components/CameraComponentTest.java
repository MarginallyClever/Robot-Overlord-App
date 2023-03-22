package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.ComponentTest;
import com.marginallyclever.robotoverlord.Entity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
