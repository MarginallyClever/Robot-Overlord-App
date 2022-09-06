package com.marginallyclever.robotoverlord.components.uiexposedtypes;

import com.marginallyclever.robotoverlord.AbstractEntityTest;
import com.marginallyclever.robotoverlord.uiexposedtypes.Vector3dEntity;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

public class Vector3dEntityTest {
    @Test
    public void saveAndLoad() throws Exception {
        Vector3dEntity a = new Vector3dEntity("a",new Vector3d(0.1,0.2,0.3));
        Vector3dEntity b = new Vector3dEntity("b",new Vector3d());
        AbstractEntityTest.saveAndLoad(a,b);
    }
}
