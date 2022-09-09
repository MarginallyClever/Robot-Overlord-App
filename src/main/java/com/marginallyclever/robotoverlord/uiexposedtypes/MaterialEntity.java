package com.marginallyclever.robotoverlord.uiexposedtypes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import java.io.Serial;


/**
 * Material properties (surface finish, color, texture, etc) of something displayed in the world.
 * @author Dan Royer
 *
 */
@Deprecated
public class MaterialEntity extends Entity {
	@Serial
	private static final long serialVersionUID = 6239607540529931426L;
	private final ColorEntity ambient    = new ColorEntity("Ambient" ,1,1,1,1);
	private final ColorEntity diffuse    = new ColorEntity("Diffuse" ,1,1,1,1);
	private final ColorEntity specular   = new ColorEntity("Specular",1,1,1,1);
	private final ColorEntity emission   = new ColorEntity("Emission",0,0,0,1);
	private final IntEntity shininess    = new IntEntity("Shininess",10);
	private final BooleanEntity isLit    = new BooleanEntity("Lit",true);
	private final TextureEntity texture  = new TextureEntity();
		
	public MaterialEntity() {
		super();
		this.setName("Material");

		addEntity(isLit);
		addEntity(diffuse);
		addEntity(specular);
		addEntity(emission);
		addEntity(ambient);
		addEntity(shininess);
		addEntity(texture);
	}
	
	@Override
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

	/**
	 *
	 * @param r 0...1
	 * @param g 0...1
	 * @param b 0...1
	 * @param a 0...1
	 */
	public void setDiffuseColor(double r,double g,double b,double a) {
		diffuse.set(r,g,b,a);
	}

	/**
	 *
	 * @param r 0...1
	 * @param g 0...1
	 * @param b 0...1
	 * @param a 0...1
	 */
	public void setSpecularColor(double r,double g,double b,double a) {
		specular.set(r,g,b,a);
	}

	/**
	 *
	 * @param r 0...1
	 * @param g 0...1
	 * @param b 0...1
	 * @param a 0...1
	 */
	public void setEmissionColor(double r,double g,double b,double a) {
		emission.set(r,g,b,a);
	}

	/**
	 *
	 * @param r 0...1
	 * @param g 0...1
	 * @param b 0...1
	 * @param a 0...1
	 */
	public void setAmbientColor(double r,double g,double b,double a) {
		ambient.set(r,g,b,a);
	}
    
	public double[] getDiffuseColor() {
		return diffuse.getDoubleArray();
	}

	public double[] getAmbientColor() {
		return ambient.getDoubleArray();
	}
	
	public double[] getSpecular() {
		return specular.getDoubleArray();
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
		texture.getView(view);

		view.popStack();
	}
}
