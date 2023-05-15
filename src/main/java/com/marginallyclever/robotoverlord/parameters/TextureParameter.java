package com.marginallyclever.robotoverlord.parameters;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A texture file name.  Loads the texture when needed from a pool to reduce duplication.
 * @author Dan Royer
 */
public class TextureParameter extends StringParameter {
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

	public void render(GL2 gl2) {
		if(textureDirty) loadNewTexture();

	    if(texture==null) {
			gl2.glDisable(GL2.GL_TEXTURE_2D);
	    } else {
			gl2.glEnable(GL2.GL_TEXTURE_2D);
	    	texture.bind(gl2);
	    }
	}

	private void loadNewTexture() {
		if(t == null || t.length()==0) {
			texture = null;
			textureDirty=false;
			return;
		}

		try {
			texture = getTextureFromPool(t);
		} catch(IOException e) {
			//e.printStackTrace();
			logger.error("I can't load "+t);
		}
		textureDirty=false;
	}

	private static Texture getTextureFromPool(String filename) throws IOException {
		Texture t = texturePool.get(filename);
		if(t==null) {
			t = TextureIO.newTexture(FileAccess.open(filename), false, filename.substring(filename.lastIndexOf('.')+1));
			texturePool.put(filename, t);
		}
		return t; 
	}
	
	public static void drainPool() {
		texturePool.clear();
	}
	
	public static void forceReload(String filename) {
		texturePool.remove(filename);
	}
	
	@Override
	public void set(String s) {
		if(s != null && s.equals(t)) return;
		if(s==null && t==null) return;

		super.set(s);
		textureDirty=true;
	}

	public Texture getTexture() {
		return texture;
	}
}
