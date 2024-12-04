package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;


class MaterialTest {
    @Test
    void testGetTexture() {
        Material material = new Material();
        TextureWithMetadata texture = new TextureWithMetadata(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB), "test");
        material.setDiffuseTexture(texture);
        assertEquals(texture, material.getDiffuseTexture());
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