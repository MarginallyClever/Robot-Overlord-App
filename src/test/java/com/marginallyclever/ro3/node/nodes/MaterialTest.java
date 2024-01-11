package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import static org.junit.jupiter.api.Assertions.*;

class MaterialTest {
    @Test
    void testGetTexture() {
        Registry.start();
        Material material = new Material();
        TextureWithMetadata texture = Registry.textureFactory.load("src/test/resources/com/marginallyclever/ro3/apps/node/nodes/marlinrobotarm/SIXI3_BASE.png");
        material.setTexture(texture);
        assertEquals(texture, material.getTexture());
    }

    @Test
    void testGetSetDiffuseColor() {
        Material material = new Material();
        Color color = new Color(255, 0, 0);
        material.setDiffuseColor(color);
        assertEquals(color, material.getDiffuseColor());
    }

    @Test
    void testGetSetSpecularColor() {
        Material material = new Material();
        Color color = new Color(0, 255, 0);
        material.setSpecularColor(color);
        assertEquals(color, material.getSpecularColor());
    }

    @Test
    void testGetSetEmissionColor() {
        Material material = new Material();
        Color color = new Color(0, 0, 255);
        material.setEmissionColor(color);
        assertEquals(color, material.getEmissionColor());
    }

    @Test
    void testGetSetShininess() {
        Material material = new Material();
        int shininess = 50;
        material.setShininess(shininess);
        assertEquals(shininess, material.getShininess());
    }

    @Test
    void testIsSetLit() {
        Material material = new Material();
        material.setLit(true);
        assertTrue(material.isLit());
        material.setLit(false);
        assertFalse(material.isLit());
    }
}