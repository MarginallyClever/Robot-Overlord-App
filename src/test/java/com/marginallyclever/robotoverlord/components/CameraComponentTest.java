package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.ComponentTest;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;
import java.io.IOException;

public class CameraComponentTest {
    @Test
    public void saveAndLoad() throws Exception {
        CameraComponent a = new CameraComponent();
        CameraComponent b = new CameraComponent();
        ComponentTest.saveAndLoad(a,b);

        a.setZoom(a.getZoom()+10.0);
        ComponentTest.saveAndLoad(a,b);
    }
}
