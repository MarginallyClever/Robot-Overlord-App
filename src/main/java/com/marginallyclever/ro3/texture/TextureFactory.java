package com.marginallyclever.ro3.texture;

import com.jogamp.opengl.GLAutoDrawable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link TextureFactory} loads textures from files.
 */
public class TextureFactory {
    private static final Logger logger = LoggerFactory.getLogger(TextureFactory.class);
    private final List<TextureWithMetadata> textures = new ArrayList<>();

    public TextureFactory() {}

    /**
     * Load a texture from a file.
     * @param filename the file to load.
     * @return the texture, or null if the file could not be loaded.
     */
    public TextureWithMetadata load(String filename) {
        for(TextureWithMetadata t : textures) {
            if(t.getSource().equals(filename)) {
                return t;
            }
        }
        TextureWithMetadata t = loadTexture(filename);
        textures.add(t);
        return t;
    }

    private TextureWithMetadata loadTexture(String filename) {
        try {
            BufferedImage image = ImageIO.read(new File(filename));
            return new TextureWithMetadata(image, filename);
        } catch (IOException e) {
            logger.error("Failed to load from "+filename,e);
            return null;
        }
    }

    /**
     * Unloads all OpenGL textures so that they can be reloaded on the next call to {@link GLAutoDrawable#display()}.
     * Does not free the underlying {@link BufferedImage} data.
     */
    public void unloadAll() {
        for(TextureWithMetadata t : textures) {
            t.unload();
        }
    }
}