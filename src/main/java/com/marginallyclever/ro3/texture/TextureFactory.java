package com.marginallyclever.ro3.texture;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.ro3.factories.Factory;
import com.marginallyclever.ro3.factories.Resource;
import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link TextureFactory} loads textures from files.
 */
public class TextureFactory implements Factory {
    private static final Logger logger = LoggerFactory.getLogger(TextureFactory.class);
    private final ListWithEvents<TextureWithMetadata> texturePool = new ListWithEvents<>();

    public TextureFactory() {}

    /**
     * Load a texture from a file.
     * @param filename the file to load.
     * @return the texture, or null if the file could not be loaded.
     */
    public TextureWithMetadata get(String filename) {
        String absolutePath = FileHelper.getAbsolutePathOrFilename(filename);
        for(TextureWithMetadata t : texturePool.getList()) {
            if(t.getSource().equals(absolutePath)) {
                return t;
            }
        }
        TextureWithMetadata t = loadTexture(absolutePath);
        if(t!=null) texturePool.add(t);
        return t;
    }

    private TextureWithMetadata loadTexture(String filename) {
        try {
            BufferedImage image = ImageIO.read(FileHelper.open(filename));
            return new TextureWithMetadata(image, filename);
        } catch (IOException e) {
            logger.error("Failed to load from "+filename,e);
            return null;
        }
    }

    /**
     * <p>Unloads all OpenGL textures so that they can be reloaded on the next call to {@link GLAutoDrawable#display()}.
     * Does not free the underlying {@link BufferedImage} data.</p>
     * <p>if this is called from a thread that has no OpenGL context a {@link com.jogamp.opengl.GLException} will occur.</p>
     */
    public void unloadAll(GL3 gl) {
        for(TextureWithMetadata t : texturePool.getList()) {
            t.unload(gl);
        }
    }

    /**
     * @return a list of all the sources used to load textures.
     */
    public List<String> getAllSourcesForExport() {
        List<String> result = new ArrayList<>();
        for(TextureWithMetadata t : texturePool.getList()) {
            if(t.isDoNotExport()) continue;
            result.add(t.getSource());
        }
        return result;
    }

    /**
     * @return a list of all the textures loaded.
     */
    public ListWithEvents<TextureWithMetadata> getPool() {
        return texturePool;
    }

    public List<FileFilter> getAllExtensions() {
        List<FileFilter> result = new ArrayList<>();
        String[] suffixes = ImageIO.getReaderFileSuffixes();

        for (String suffix : suffixes) {
            FileFilter filter = new FileNameExtensionFilter(suffix + " files", suffix);
            result.add(filter);
        }

        return result;
    }

    @Override
    public void reset() {
        // FIXME Not calling unload() on each item is probably a video card memory leak.
        texturePool.removeAll();
    }
}
