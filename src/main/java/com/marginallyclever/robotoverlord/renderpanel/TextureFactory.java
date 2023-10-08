package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class TextureFactory {
    private static final Logger logger = LoggerFactory.getLogger(TextureFactory.class);
    private static final HashMap<String, Texture> texturePool = new HashMap<>();


    public static Texture createTexture(String filename) {
        Texture texture = TextureFactory.texturePool.get(filename);
        if (texture == null) {
            texture = TextureFactory.loadTextureFromFile(filename);
            if (texture != null) TextureFactory.texturePool.put(filename, texture);
        }
        return texture;
    }

    private static Texture loadTextureFromFile(String filename) {
        Texture t = null;

        try {
            t = TextureIO.newTexture(FileHelper.open(filename), false, filename.substring(filename.lastIndexOf('.')+1));
        } catch (IOException e) {
            //e.printStackTrace();
            logger.error("Failed to load {}", filename,e);
        }

        return t;
    }

    public static void unloadAll(GL3 gl) {
        for(Texture t : texturePool.values()) {
            t.destroy(gl);
        }
    }

    public static void loadAll() {
        Set<String> keys = texturePool.keySet();
        for(String key : keys) {
            Texture t = createTexture(key);
            texturePool.put(key, t);
        }
    }
}
