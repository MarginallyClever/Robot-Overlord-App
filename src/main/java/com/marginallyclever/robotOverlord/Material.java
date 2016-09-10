package com.marginallyclever.robotOverlord;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;

public class Material {
	public float[] diffuse		= {1.00f,1.00f,1.00f,1.00f};
	public float[] specular 	= {0.85f,0.85f,0.85f,1.00f};
	public float[] emission 	= {0.01f,0.01f,0.01f,1.00f};
	public float[] ambient		= {0.01f,0.01f,0.01f,1.00f};
	private float shininess		= 10.0f;
	private Texture texture     = null;
	private boolean isLit		= true;
	
	public Material() {}
	
	public void render(GL2 gl2) {
		gl2.glColor4f(diffuse[0],diffuse[1],diffuse[2],diffuse[3]);
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse,0);
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular,0);
	    gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, emission,0);
	    gl2.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
	    gl2.glColorMaterial(GL2.GL_FRONT_AND_BACK,GL2.GL_AMBIENT );
	    if(isLit()) gl2.glEnable(GL2.GL_LIGHTING);
	    else gl2.glDisable(GL2.GL_LIGHTING);
	    if(texture==null) {
			gl2.glDisable(GL2.GL_TEXTURE_2D);
	    } else {
			gl2.glEnable(GL2.GL_TEXTURE_2D);
	    	texture.bind(gl2);
	    }
	}
	

	public void setDiffuseColor(float r,float g,float b,float a) {
		diffuse[0]=r;
		diffuse[1]=g;
		diffuse[2]=b;
		diffuse[3]=a;
	}
	

	public void setSpecularColor(float r,float g,float b,float a) {
		specular[0]=r;
		specular[1]=g;
		specular[2]=b;
		specular[3]=a;
	}
	

	public void setEmissionColor(float r,float g,float b,float a) {
		emission[0]=r;
		emission[1]=g;
		emission[2]=b;
		emission[3]=a;
	}
	

	public void setAmbientColor(float r,float g,float b,float a) {
		ambient[0]=r;
		ambient[1]=g;
		ambient[2]=b;
		ambient[3]=a;
	}
	
	public void setTexture(Texture arg0) {
		texture = arg0;
	}
	public Texture getTexture() {
		return texture;
	}

	public boolean isLit() {
		return isLit;
	}

	public void setLit(boolean isLit) {
		this.isLit = isLit;
	}
}
