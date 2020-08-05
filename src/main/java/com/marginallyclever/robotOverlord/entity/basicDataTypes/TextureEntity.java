package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class TextureEntity extends StringEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5488195728478812737L;

	// TODO load textures from a texture pool to eliminate duplicates?  See ModelEntity for example.
	
	private transient Texture texture;
	private transient boolean textureDirty;
	
	public TextureEntity() {
		super();
		setName("Texture");
	}
	
	public TextureEntity(String fileName) {
		super(fileName);
		setName("Texture");
		textureDirty=false;
	}

	public void render(GL2 gl2) {
		if(textureDirty) {
			// texture has changed, load the new texture.
			if(t == null || t.length()==0) texture = null;
			else {
				try {
					texture = TextureIO.newTexture(FileAccess.open(t), false, t.substring(t.lastIndexOf('.')+1));
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

	@Override
	public void set(String s) {
		super.set(s);
		textureDirty=true;
	}
	
	@Override
	public void getView(ViewPanel view) {
		//TODO Swing elements like FileFilter should not be mentioned outside of the view.
		ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
		// supported file formats
		filters.add(new FileNameExtensionFilter("PNG", "png"));
		filters.add(new FileNameExtensionFilter("BMP", "bmp"));
		filters.add(new FileNameExtensionFilter("JPEG", "jpeg","jpg"));
		filters.add(new FileNameExtensionFilter("TGA", "tga"));
		
		view.addFilename(this,filters);
	}
}
