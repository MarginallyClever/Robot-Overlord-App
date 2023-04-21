package com.marginallyclever.robotoverlord.parameters;

import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

public class Matrix4dEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        Matrix4d m = new Matrix4d();
        m.setRotation(new Quat4d(1,2,3,4));
        m.setTranslation(new Vector3d(5,6,7));

        Matrix4dEntity a = new Matrix4dEntity(m);
        Matrix4dEntity b = new Matrix4dEntity(new Matrix4d());
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
