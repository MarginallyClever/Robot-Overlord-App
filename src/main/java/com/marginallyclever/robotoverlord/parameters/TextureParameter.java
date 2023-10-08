package com.marginallyclever.robotoverlord.parameters;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.marginallyclever.robotoverlord.renderpanel.TextureFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
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
			texture = TextureFactory.createTexture(value);
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
