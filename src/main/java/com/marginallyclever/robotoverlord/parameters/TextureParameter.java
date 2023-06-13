package com.marginallyclever.robotoverlord.parameters;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.helpers.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * A texture file name.  Loads the texture when needed from a pool to reduce duplication.
 * @author Dan Royer
 */
public class TextureParameter extends FilenameParameter {
	private static final Logger logger = LoggerFactory.getLogger(TextureParameter.class);

	// supported file formats
	private static final List<FileFilter> filters = List.of(
		new FileNameExtensionFilter("PNG", "png"),
		new FileNameExtensionFilter("BMP", "bmp"),
		new FileNameExtensionFilter("JPEG", "jpeg","jpg"),
		new FileNameExtensionFilter("TGA", "tga")
	);

	private static final HashMap<String,Texture> texturePool = new HashMap<>();
	
	private transient Texture texture;
	private transient boolean textureDirty;

	public TextureParameter(String name,String fileName) {
		super(name,fileName);
		textureDirty=true;
	}

	public static List<FileFilter> getFilters() {
		return filters;
	}

	public void render(GL3 gl) {
		if(textureDirty) loadTexture();

	    if(texture==null) {
			gl.glDisable(GL3.GL_TEXTURE_2D);
	    } else {
			gl.glEnable(GL3.GL_TEXTURE_2D);
	    	texture.bind(gl);
	    }
	}

	private void loadTexture() {
		String value = get();
		if(value == null || value.length()==0) {
			texture = null;
			textureDirty=false;
			return;
		}

		texture = texturePool.get(value);
		if(texture==null) {
			try {
				texture = TextureIO.newTexture(FileHelper.open(value), false, value.substring(value.lastIndexOf('.')+1));
				texturePool.put(value, texture);
			} catch (IOException e) {
				//e.printStackTrace();
				logger.error("Failed to load {}", value,e);
			}
		}
		textureDirty=false;
	}

	public static void drainPool() {
		texturePool.clear();
	}
	
	public static void forceReload(String filename) {
		texturePool.remove(filename);
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
}
