package com.marginallyclever.robotOverlord.entity.basicDataTypes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;


/**
 * Material properties (surface finish, color, texture, etc) of something displayed in the world.
 * @author Dan Royer
 *
 */
public class MaterialEntity extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7313230310466105159L;
	
	private ColorEntity ambient    = new ColorEntity("Ambient" ,0.2,0.2,0.2,1.00);
	private ColorEntity diffuse    = new ColorEntity("Diffuse" ,0.8,0.8,0.8,1.00);
	private ColorEntity specular   = new ColorEntity("Specular",0.0,0.0,0.0,1.00);
	private ColorEntity emission   = new ColorEntity("Emission",0.0,0.0,0.0,1.00);
	private IntEntity shininess    = new IntEntity("Shininess",10);
	private BooleanEntity isLit    = new BooleanEntity("Lit",true);
	private TextureEntity texture  = new TextureEntity();
		
	public MaterialEntity() {
		super();
		this.setName("Material");

		addChild(isLit);
		addChild(diffuse);
		addChild(specular);
		addChild(emission);
		addChild(ambient);
		addChild(shininess);
		addChild(texture);
	}
	
	public void render(GL2 gl2) {
		gl2.glColor4d(diffuse.getR(),diffuse.getG(),diffuse.getB(),diffuse.getA());
		gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuse.getFloatArray(),0);
	    gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular.getFloatArray(),0);
	    gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emission.getFloatArray(),0);
	    gl2.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ambient.getFloatArray(),0);
	    gl2.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, shininess.get().floatValue());
	    gl2.glColorMaterial(GL2.GL_FRONT,GL2.GL_AMBIENT_AND_DIFFUSE );
	    
	    boolean isColorEnabled = gl2.glIsEnabled(GL2.GL_COLOR_MATERIAL);
		gl2.glDisable(GL2.GL_COLOR_MATERIAL);
		
		gl2.glShadeModel(GL2.GL_SMOOTH);
		
	    if(isLit()) gl2.glEnable(GL2.GL_LIGHTING);
	    else gl2.glDisable(GL2.GL_LIGHTING);

	    texture.render(gl2);
	    
	    if(isColorEnabled) gl2.glEnable(GL2.GL_COLOR_MATERIAL);
	}
	

	public void setShininess(int arg0) {
		arg0 = Math.min(Math.max(arg0, 0), 128);
		shininess.set(arg0);
	}
	public double getShininess() {
		return shininess.get();
	}
	
	public void setDiffuseColor(float r,float g,float b,float a) {
		diffuse.set(r,g,b,a);
	}
	
	public void setSpecularColor(float r,float g,float b,float a) {
		specular.set(r,g,b,a);
	}
	
	public void setEmissionColor(float r,float g,float b,float a) {
		emission.set(r,g,b,a);
	}
	

	public void setAmbientColor(float r,float g,float b,float a) {
		ambient.set(r,g,b,a);
	}
    
	public float[] getDiffuseColor() {
		return diffuse.getFloatArray();
	}

	public float[] getAmbientColor() {
		return ambient.getFloatArray();
	}
	
	public float[] getSpecular() {
		return specular.getFloatArray();
	}
	
	
	public void setTextureFilename(String arg0) {
		texture.set(arg0);
	}
	public String getTextureFilename() {
		return texture.get();
	}

	public boolean isLit() {
		return isLit.get();
	}

	public void setLit(boolean isLit) {
		this.isLit.set(isLit);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Ma","Material");
		view.add(isLit  );
		view.add(emission);
		view.add(ambient );
		view.add(diffuse );
		view.add(specular);
		view.addRange(shininess, 128, 0);
		view.add(texture);

		view.popStack();
	}
}
