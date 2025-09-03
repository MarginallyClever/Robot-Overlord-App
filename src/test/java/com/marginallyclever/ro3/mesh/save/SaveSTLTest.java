package com.marginallyclever.ro3.mesh.save;

import com.marginallyclever.ro3.mesh.proceduralmesh.Box;
import com.marginallyclever.ro3.mesh.proceduralmesh.Sphere;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveSTLTest {
    @Test
    @Disabled
    public void saveSphere() throws IOException {
        var sphere = new Sphere(10);
        var saver =  new SaveSTL();
        File temp = new File("sphereTest.stl");//File.createTempFile("temp",".stl");
        try(var outputStream = new FileOutputStream(temp)) {
            saver.save(outputStream,sphere);
        }
    }

    @Test
    @Disabled
    public void saveBox()  throws IOException {
        var box = new Box(10,10,10);
        var saver =  new SaveSTL();
        File temp = new File("boxTest.stl");
        try(var outputStream = new FileOutputStream(temp)) {
            saver.save(outputStream,box);
        }
    }
}
