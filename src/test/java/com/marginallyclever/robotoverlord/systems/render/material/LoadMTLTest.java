package com.marginallyclever.robotoverlord.systems.render.material;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Objects;

public class LoadMTLTest {
    @Test
    public void testLoadMTL() {
        MaterialComponent material = new MaterialComponent();
        MaterialLoader loadMTL = new LoadMTL();
        try (BufferedInputStream stream = new BufferedInputStream(Objects.requireNonNull(this.getClass().getResourceAsStream("torso.mtl")))) {
            loadMTL.load(stream, material);
        }
        catch (Exception e) {
            Assertions.fail();
        }

        Assertions.assertArrayEquals(new double[]{1.0, 1.0, 1.0, 1.0}, material.getAmbientColor());
        Assertions.assertArrayEquals(new double[]{0.002063, 0.00206, 0.0, 1.0}, material.getDiffuseColor());
        Assertions.assertArrayEquals(new double[]{0.002063, 0.00206, 0.0, 1.0}, material.getDiffuseColor());
        Assertions.assertArrayEquals(new double[]{0.5, 0.5, 0.5, 1.0}, material.getSpecularColor());
        Assertions.assertTrue(material.getTextureFilename().endsWith("sixi.png"));
    }

    @Test
    public void testLoadOBJWithMTL() {
        Entity entity = new Entity();
        File resource = new File(Objects.requireNonNull(this.getClass().getResource("torso.obj")).getFile());
        Assertions.assertNotNull(resource);
        MeshFromFile mesh = new MeshFromFile(resource.getAbsolutePath());
        entity.addComponent(mesh);
        MaterialComponent material = entity.getComponent(MaterialComponent.class);
        Assertions.assertNotNull(material);
        Assertions.assertArrayEquals(new double[]{0.002063,0.002060,0.000000, 1.0}, material.getDiffuseColor());
    }
}
