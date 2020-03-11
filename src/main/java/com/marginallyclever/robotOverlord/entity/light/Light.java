package com.marginallyclever.robotOverlord.entity.light;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;

public class Light extends PhysicalObject {
	public int index=0;
	private boolean enabled=true;
	private float[] position={0,0,1,0};
	private float[] ambient ={0,0,0,1};
	private float[] diffuse ={0,0,0,1};
	private float[] specular={0,0,0,1};
	
	private float[] direction={0,0,1};
	
	private float cutoff=180;
	private float exponent=0;
	
	private float attenuationConstant=1;
	private float attenuationLinear=0;
	private float attenuationQuadratic=0;
	
	private LightPanel lightPanel;
	
	public Light() {
		super();
		setName("Light");
	}


	@Override
	public ArrayList<JPanel> getContextPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanels(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		lightPanel = new LightPanel(gui,this);
		list.add(lightPanel);
		
		return list;
	}
	

	@Override
	public void render(GL2 gl2) {
		int i = GL2.GL_LIGHT0+index;
		if(!enabled) {
			gl2.glDisable(i);
			return;
		}
		
		position[3]=(index==0)?0:1;
		
		attenuationConstant=(index==0)?0:0.5f;
		attenuationLinear=(index==0)?0:0.4f;
		
		gl2.glEnable(i);
		gl2.glLightfv(i, GL2.GL_POSITION, position,0);
	    gl2.glLightfv(i, GL2.GL_AMBIENT, ambient,0);
	    gl2.glLightfv(i, GL2.GL_DIFFUSE, diffuse,0);
	    gl2.glLightfv(i, GL2.GL_SPECULAR, specular,0);

		gl2.glLightfv(i, GL2.GL_SPOT_DIRECTION, direction,0);
	    
	    gl2.glLightf(i, GL2.GL_SPOT_EXPONENT, exponent);
	    gl2.glLightf(i, GL2. GL_SPOT_CUTOFF, cutoff);
	    gl2.glLightf(i, GL2.GL_SPOT_EXPONENT, exponent);
	    
	    gl2.glLightf(i, GL2.GL_CONSTANT_ATTENUATION,attenuationConstant);
	    gl2.glLightf(i, GL2.GL_LINEAR_ATTENUATION,attenuationLinear);
	    gl2.glLightf(i, GL2.GL_QUADRATIC_ATTENUATION,attenuationQuadratic);
    
	
		super.render(gl2);
	}

	public void setEnable(boolean arg0) {
		enabled=arg0;
	}

	public boolean getEnabled() {
		return enabled;
	}
	
	public boolean isDirectional() {
		return position[3]==0;
	}
	
	/**
	 * 
	 * @param arg0 true for directional light, false for point source light.
	 */
	public void setDirectional(boolean arg0) {
		position[3] = arg0 ? 0 : 1;
	}
	
	@Override
	public void setPosition(Vector3d p) {
		super.setPosition(p);
		position[0] = (float)p.x;
		position[1] = (float)p.y;
		position[2] = (float)p.z;
	}
	
	public void setDiffuse(float r,float g,float b,float a) {
		diffuse[0]=r;
		diffuse[1]=g;
		diffuse[2]=b;
		diffuse[3]=a;
		if(lightPanel!=null) lightPanel.updateFields();
	}

	public void setAmbient(float r,float g,float b,float a) {
		ambient[0]=r;
		ambient[1]=g;
		ambient[2]=b;
		ambient[3]=a;
		if(lightPanel!=null) lightPanel.updateFields();
	}

	public void setSpecular(float r,float g,float b,float a) {
		specular[0]=r;
		specular[1]=g;
		specular[2]=b;
		specular[3]=a;
		if(lightPanel!=null) lightPanel.updateFields();
	}
    
	public float[] getDiffuse() {
		return diffuse.clone();
	}

	public float[] getAmbient() {
		return ambient.clone();
	}
	
	public float[] getSpecular() {
		return specular.clone();
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
