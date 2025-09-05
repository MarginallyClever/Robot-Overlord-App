package com.marginallyclever.ro3.texture;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.factories.Factory;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.factories.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * {@link TextureFactory} loads textures from files.
 */
public class TextureFactory extends Factory {
    private static final Logger logger = LoggerFactory.getLogger(TextureFactory.class);
    private final Map<String, Resource<TextureWithMetadata>> cache = new HashMap<>();

    public TextureFactory() {}

    /**
     * Load a texture from a file.
     * @param lifetime the lifetime of the texture.
     * @param filename the file to load.
     * @return the texture, or null if the file could not be loaded.
     */
    public TextureWithMetadata get(Lifetime lifetime,String filename) {
        String absolutePath = FileHelper.getAbsolutePathOrFilename(filename);
        return cache.computeIfAbsent(absolutePath, _->
                new Resource<>(loadTexture(absolutePath), lifetime)
        ).item();
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
        cache.values().forEach(e -> e.item().unload(gl) );
    }

    public List<String> getAllSourcesForExport() {
        return getResources(Lifetime.SCENE).stream().map(TextureWithMetadata::getSource).toList();
    }

    public List<TextureWithMetadata> getResources(Lifetime lifetime) {
        return cache.values().stream()
                .filter(r -> r.lifetime() == lifetime)
                .map(Resource::item)
                .toList();
    }

    public List<TextureWithMetadata> getAllResources() {
        return cache.values().stream()
                .map(Resource::item)
                .toList();
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
    public void removeSceneResources() {
        var list = Registry.toBeUnloaded;
        synchronized (list) {
            // move scene resources to Registry.toBeUnloaded so they can be unloaded in the GL thread.
            cache.values().stream()
                    .filter(r -> r.lifetime() == Lifetime.SCENE)
                    .map(Resource::item)
                    .forEach(list::add);
        }

        cache.values().removeIf(r -> r.lifetime() == Lifetime.SCENE);
    }
}
