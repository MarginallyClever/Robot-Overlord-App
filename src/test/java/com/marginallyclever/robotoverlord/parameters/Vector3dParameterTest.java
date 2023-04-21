package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class Vector3dParameterTest {
    @Test
    public void saveAndLoad() throws Exception {
        Vector3DParameter a = new Vector3DParameter("a",new Vector3d(0.1,0.2,0.3));
        Vector3DParameter b = new Vector3DParameter("b",new Vector3d());
        AbstractParameterTest.saveAndLoad(a,b);
    }
}
