package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.ComponentTest;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class DHComponentTest {

    @Test
    public void saveAndLoad() throws Exception {
        DHComponent a = new DHComponent();
        DHComponent b = new DHComponent();
        ComponentTest.saveAndLoad(a,b);

        a.setD(1);
        ComponentTest.saveAndLoad(a,b);

        a.setR(2);
        ComponentTest.saveAndLoad(a,b);

        a.setAlpha(3);
        ComponentTest.saveAndLoad(a,b);

        a.setThetaMin(4);
        ComponentTest.saveAndLoad(a,b);

        a.setThetaMax(5);
        ComponentTest.saveAndLoad(a,b);

        a.setTheta(4.5);
        ComponentTest.saveAndLoad(a,b);

        a.setThetaHome(7);
        ComponentTest.saveAndLoad(a,b);
    }
}
