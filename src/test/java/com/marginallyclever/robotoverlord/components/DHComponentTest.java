package com.marginallyclever.robotoverlord.components;

import org.junit.jupiter.api.Test;

public class DHComponentTest {

    @Test
    public void saveAndLoad() throws Exception {
        DHComponent a = new DHComponent();
        DHComponent b = new DHComponent();
        ComponentTest.saveAndLoad(a,b);

        a.setD(1);
        a.setR(2);
        a.setAlpha(3);
        a.setJointMin(4);
        a.setJointMax(7);
        a.setTheta(5);
        a.setJointHome(6);

        ComponentTest.saveAndLoad(a,b);
    }
}
