package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.ComponentTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MeshFromFileTest {
    @Test
    public void saveAndLoad() throws Exception {
        MeshFromFile a = new MeshFromFile();
        MeshFromFile b = new MeshFromFile();
        File resource = new File(this.getClass().getResource("torso.obj").getFile());
        Assertions.assertNotNull(resource);
        String path = resource.getAbsolutePath();
        a.setFilename(path);
        a.load();
        Assertions.assertNotNull(a.getModel());
        Assertions.assertNotEquals(0,a.getModel().getNumVertices());
        ComponentTest.saveAndLoad(a,b);
    }
}
