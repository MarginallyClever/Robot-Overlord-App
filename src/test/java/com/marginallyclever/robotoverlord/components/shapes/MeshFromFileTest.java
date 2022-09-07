package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.ComponentTest;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import org.junit.jupiter.api.Test;

public class MeshFromFileTest {
    @Test
    public void saveAndLoad() throws Exception {
        MeshFromFile a = new MeshFromFile();
        MeshFromFile b = new MeshFromFile();
        a.setFilename("/Sixi3b/j0.obj");
        ComponentTest.saveAndLoad(a,b);
        System.out.println(a);
    }
}
