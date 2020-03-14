package com.marginallyclever.robotOverlord.entity.lightEntity;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.physicalEntity.PhysicalEntity;

public class LightEntity extends PhysicalEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int index=0;
	
	private float[] position={0,0,1,0};
	
	private float[] direction={0,0,1};

	public BooleanEntity enabled = new BooleanEntity("On",true);
	public ColorEntity diffuse = new ColorEntity("Diffuse" ,0,0,0,1);
	public ColorEntity specular= new ColorEntity("Specular",0,0,0,1);
	public ColorEntity ambient = new ColorEntity("Ambient" ,0,0,0,1);

	public BooleanEntity isDirectional = new BooleanEntity("Directional",false);
	
	public DoubleEntity cutoff = new DoubleEntity("Cutoff",180);
	public DoubleEntity exponent = new DoubleEntity("Exponent",0);
	public DoubleEntity attenuationConstant = new DoubleEntity("Constant attenuation",1.0);
	public DoubleEntity attenuationLinear = new DoubleEntity("Linear attenuation",0.0);
	public DoubleEntity attenuationQuadratic = new DoubleEntity("Quadratic attenuation",0);
	
	
	public LightEntity() {
		super();
		setName("Light");
		addChild(enabled);
		addChild(diffuse);
		addChild(specular);
		addChild(ambient);
		
		addChild(cutoff);
		addChild(exponent);

		addChild(attenuationConstant);
		addChild(attenuationLinear);
		addChild(attenuationQuadratic);
	}

	@Override
	public void render(GL2 gl2) {
		if(index==0) {
			ambient.set(2.55,2.55,2.55,0.0);
			System.out.println(ambient.toString());
			enabled.set(true);
		}
		if(index==1) {
			ambient.set(0.0,0.0,0.0,0.0);
			enabled.set(true);
		}
		
		int i = GL2.GL_LIGHT0+index;
		if(!enabled.get()) {
			gl2.glDisable(i);
			return;
		}
		gl2.glEnable(i);
		
		position[3]=isDirectional.get()?0:1;
		gl2.glLightfv(i, GL2.GL_POSITION, position,0);
	    gl2.glLightfv(i, GL2.GL_AMBIENT, ambient.getFloatArray(),0);
	    gl2.glLightfv(i, GL2.GL_DIFFUSE, diffuse.getFloatArray(),0);
	    gl2.glLightfv(i, GL2.GL_SPECULAR, specular.getFloatArray(),0);

		gl2.glLightfv(i, GL2.GL_SPOT_DIRECTION, direction,0);
	    
	    gl2.glLightf(i, GL2.GL_SPOT_CUTOFF, cutoff.get().floatValue());
	    gl2.glLightf(i, GL2.GL_SPOT_EXPONENT, exponent.get().floatValue());
	    
	    gl2.glLightf(i, GL2.GL_CONSTANT_ATTENUATION,attenuationConstant.get().floatValue());
	    gl2.glLightf(i, GL2.GL_LINEAR_ATTENUATION,attenuationLinear.get().floatValue());
	    gl2.glLightf(i, GL2.GL_QUADRATIC_ATTENUATION,attenuationQuadratic.get().floatValue());
		
		super.render(gl2);
	}

	public void setEnable(boolean arg0) {
		enabled.set(arg0);
	}

	public boolean getEnabled() {
		return enabled.get();
	}
	
	/**
	 * 
	 * @param arg0 true for directional light, false for point source light.
	 */
	public void setDirectional(boolean arg0) {
		isDirectional.set(arg0);
	}
	
	public boolean isDirectional() {
		return isDirectional.get();
	}
	
	
	@Override
	public void setPosition(Vector3d p) {
		super.setPosition(p);
		position[0] = (float)p.x;
		position[1] = (float)p.y;
		position[2] = (float)p.z;
	}
	
	public void setDiffuse(float r,float g,float b,float a) {
		diffuse.set(r,g,b,a);
	}
    
	public float[] getDiffuse() {
		return diffuse.getFloatArray();
	}

	public void setAmbient(float r,float g,float b,float a) {
		ambient.set(r,g,b,a);
	}

	public float[] getAmbient() {
		return ambient.getFloatArray();
	}

	public void setSpecular(float r,float g,float b,float a) {
		specular.set(r,g,b,a);
	}
	
	public float[] getSpecular() {
		return specular.getFloatArray();
	}

	/**
	 * 
	 * @return a list of cuboids, or null.
	 */
	public ArrayList<Cuboid> getCuboidList() {		
		// doesn't collide with anything, ever.
		return null;
	}
}
