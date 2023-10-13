package com.marginallyclever.robotoverlord.parameters;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.robotoverlord.renderpanel.TextureFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A texture file name.  Loads the texture when needed from a pool to reduce duplication.
 * @author Dan Royer
 */
public class TextureParameter extends FilenameParameter {
	public static final HashMap<String, Texture> texturePool = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(TextureParameter.class);

	// supported file formats
	private static final List<FileFilter> filters = List.of(
		new FileNameExtensionFilter("PNG", "png"),
		new FileNameExtensionFilter("BMP", "bmp"),
		new FileNameExtensionFilter("JPEG", "jpeg","jpg"),
		new FileNameExtensionFilter("TGA", "tga")
	);

	private transient Texture texture;
	private transient boolean textureDirty;

	public TextureParameter(String name,String fileName) {
		super(name,fileName);
		textureDirty=true;
	}

	public static List<FileFilter> getFilters() {
		return filters;
	}

	public static Texture createTexture(String filename) {
		Texture texture = texturePool.get(filename);
		if (texture == null) {
			texture = loadTextureFromFile(filename);
			if (texture != null)
				texturePool.put(filename, texture);
		}
		return texture;
	}

	private static Texture loadTextureFromFile(String filename) {
		Texture t = null;

		try {
			t = TextureIO.newTexture(FileHelper.open(filename), false, filename.substring(filename.lastIndexOf('.') + 1));
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error("Failed to load {}", filename, e);
		}

		return t;
	}

	public static void unloadAll(GL3 gl) {
		for (Texture t : texturePool.values()) {
			t.destroy(gl);
		}
	}

	public static void loadAll() {
		Set<String> keys = texturePool.keySet();
		texturePool.clear();
		for (String key : keys) {
			Texture t = TextureParameter.createTexture(key);
			texturePool.put(key, t);
		}
	}

	public void render(GL3 gl) {
		if(textureDirty) {
			unloadTexture(gl);
			loadTexture(gl);
		}

	    if(texture==null) {
			gl.glDisable(GL3.GL_TEXTURE_2D);
	    } else {
			gl.glEnable(GL3.GL_TEXTURE_2D);
	    	texture.bind(gl);
	    }
	}

	private void unloadTexture(GL3 gl) {
		if(texture!=null) {
			texture.destroy(gl);
			texture=null;
		}
	}

	public void loadTexture(GL3 gl) {
		unloadTexture(gl);

		String value = get();
		if(value != null && !value.isEmpty()) {
			texture = TextureParameter.createTexture(value);
			if(texture != null) textureDirty = false;
		}
	}

	@Override
	public void set(String s) {
		String value = get();
		if(s != null && s.equals(value)) return;
		if(s==null && value ==null) return;
		super.set(s);
		textureDirty=true;
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture t) {
		texture = t;
		textureDirty=false;
	}
}
