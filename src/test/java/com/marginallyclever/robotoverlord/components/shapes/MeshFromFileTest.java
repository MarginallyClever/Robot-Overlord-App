package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.ComponentTest;
import org.junit.jupiter.api.Test;

public class MeshFromFileTest {
    @Test
    public void saveAndLoad() throws Exception {
        MeshFromFile a = new MeshFromFile();
        MeshFromFile b = new MeshFromFile();
        a.setFilename("/robots/Sixi3b/j0.obj");
        ComponentTest.saveAndLoad(a,b);
        System.out.println(a);
    }
}
