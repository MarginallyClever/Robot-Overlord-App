package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.apps.viewport.TextureLayerIndex;
import com.marginallyclever.ro3.texture.TextureFactory;
import com.marginallyclever.ro3.texture.TextureWithMetadata;
import org.junit.jupiter.api.Test;

import java.awt.*;
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

    @Test
    void setAndGetAllTextures() {
        Material before = new Material();
        for (var ti : TextureLayerIndex.values()) {
            TextureWithMetadata texture = new TextureWithMetadata(
                    new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB),
                    "src/test/resources/com/marginallyclever/ro3/node/nodes/" + ti.getName()+".jpg");
            before.setTexture(ti.getIndex(), texture);
        }

        Material after = new Material();
        after.fromJSON(before.toJSON());
        for (var ti : TextureLayerIndex.values()) {
            var beforeTexture = before.getTexture(ti.getIndex());
            var afterTexture = after.getTexture(ti.getIndex());
            assert beforeTexture == null || (afterTexture != null);
            assertNotNull(beforeTexture);
            assert(pathsEndTheSame(beforeTexture.getSource(), afterTexture.getSource()));
        }
    }

    /**
     * Check if two paths end the same, ignoring differences in absolute path.  It does not matter which path is longer.
     * @param a the first path.
     * @param b the second path.
     * @return true if the paths end the same.
     */
    private boolean pathsEndTheSame(String a, String b) {
        String [] aParts = a.replace("\\","/").split("/");
        String [] bParts = b.replace("\\","/").split("/");
        int aLen = aParts.length;
        int bLen = bParts.length;
        int minLen = Math.min(aLen, bLen);
        for (int i = 1; i <= minLen; i++) {
            if (!aParts[aLen - i].equals(bParts[bLen - i])) {
                return false;
            }
        }
        return true;

    }
}