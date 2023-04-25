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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A texture file name.  Loads the texture when needed from a pool to reduce duplication.
 * @author Dan Royer
 */
public class TextureParameter extends StringParameter {
	private static final Logger logger = LoggerFactory.getLogger(TextureParameter.class);

	private static final HashMap<String,Texture> texturePool = new HashMap<>();
	
	private transient Texture texture;
	private transient boolean textureDirty;
	
	public TextureParameter() {
		super("Texture","");
	}
	
	public TextureParameter(String fileName) {
		super("Texture",fileName);
		textureDirty=true;
	}

	@Override
	public void render(GL2 gl2) {
		if(textureDirty) {
			// texture has changed, load the new texture.
			if(t == null || t.length()==0) texture = null;
			else {
				try {
					texture=getTextureFromPool(t);
				} catch(IOException e) {
					//e.printStackTrace();
					logger.error("I can't load "+t);
				}
				textureDirty=false;
			}
		}
	    if(texture==null) {
			gl2.glDisable(GL2.GL_TEXTURE_2D);
	    } else {
			gl2.glEnable(GL2.GL_TEXTURE_2D);
	    	texture.bind(gl2);
	    }
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
		super.set(s);
		textureDirty=true;
	}

	public String getTextureFilename() {
		for (Map.Entry<String, Texture> entry : texturePool.entrySet()) {
			if (entry.getValue().equals(texture)) {
				return entry.getKey();
			}
		}
		throw new RuntimeException("Texture not found in pool.");
	}
}
