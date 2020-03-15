package com.marginallyclever.robotOverlord.entity.textureEntity;

import java.io.IOException;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;

public class TextureEntity extends StringEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5488195728478812737L;

	private transient Texture texture;
	private transient boolean textureDirty;
	
	public TextureEntity() {
		super();
		setName("Texture");
	}
	
	public TextureEntity(String fileName) {
		super(fileName);
		setName("Texture");
	}

	public void render(GL2 gl2) {
		if(textureDirty) {
			// texture has changed, load the new texture.
			if(t == null || t.length()==0) texture = null;
			else {
				try {
					texture = TextureIO.newTexture(FileAccess.open(t), false, t.substring(t.lastIndexOf('.')+1));
					textureDirty=false;
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	    if(texture==null) {
			gl2.glDisable(GL2.GL_TEXTURE_2D);
	    } else {
			gl2.glEnable(GL2.GL_TEXTURE_2D);
	    	texture.bind(gl2);
	    }
	}
}
