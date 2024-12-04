package com.marginallyclever.ro3.mesh.load;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoadOBJTest {
    @Test
    public void testGetEnglishName() {
        LoadOBJ loadOBJ = new LoadOBJ();
        assert(loadOBJ.getEnglishName().equals("Wavefront Object File (OBJ)"));
    }

    @Test
    public void testGetValidExtensions() {
        LoadOBJ loadOBJ = new LoadOBJ();
        assert(loadOBJ.getValidExtensions().length == 1);
        assert(loadOBJ.getValidExtensions()[0].equals("obj"));
    }

    @Test
    public void testLoadMaterialLibrary() throws Exception {
        LoadOBJ loadOBJ = new LoadOBJ();
        var map = loadOBJ.loadMaterialLibrary("src/test/resources/com/marginallyclever/ro3/mesh/load/A0.mtl");
        assert(map.size() == 10);
        var a = map.get("Prism_Opaque_2");
        Assertions.assertNotNull(a);
        Assertions.assertArrayEquals(new float[] {0.792157f, 0.819608f, 0.933333f, 1.0f},a.diffuse,1e-4f);
    }
}
