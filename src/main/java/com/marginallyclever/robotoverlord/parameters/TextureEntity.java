package com.marginallyclever.robotoverlord.parameters;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.io.Serial;
import java.util.*;

// TODO Load textures from a texture pool to eliminate duplicates?  See ShapeEntity for example.
public class TextureEntity extends StringEntity {
	@Serial
	private static final long serialVersionUID = -2104122122058199991L;
	private static final HashMap<String,Texture> texturePool = new HashMap<>();
	
	private transient Texture texture;
	private transient boolean textureDirty;
	
	public TextureEntity() {
		super();
		setName("Texture");
	}
	
	public TextureEntity(String fileName) {
		super(fileName);
		setName("Texture");
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
					Log.error("I can't load "+t);
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
	
	@Override
	public void getView(ViewPanel view) {
		//TODO Swing elements like FileFilter should not be mentioned outside of the view.
		ArrayList<FileFilter> filters = new ArrayList<>();
		// supported file formats
		filters.add(new FileNameExtensionFilter("PNG", "png"));
		filters.add(new FileNameExtensionFilter("BMP", "bmp"));
		filters.add(new FileNameExtensionFilter("JPEG", "jpeg","jpg"));
		filters.add(new FileNameExtensionFilter("TGA", "tga"));
		
		view.addFilename(this,filters);
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
