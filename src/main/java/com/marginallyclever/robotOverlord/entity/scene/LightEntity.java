package com.marginallyclever.robotOverlord.entity.scene;

import java.util.ArrayList;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * See also https://learnopengl.com/Lighting/Light-casters
 * @author Dan Royer
 * @since 1.6.0
 */
public class LightEntity extends PoseEntity {
	private final static ColorRGB [] presetBlack = {
		new ColorRGB(0,0,0),  // ambient
		new ColorRGB(0,0,0),  // specular
		new ColorRGB(0,0,0),  // diffuse
	};
	
	private final static ColorRGB [] presetNoon = {
		new ColorRGB(   0,   0,   0),
		new ColorRGB( 255, 255, 251),
		new ColorRGB(   1,   1,   1),
	};
		
	private final static ColorRGB [] presetMetalHalide = {
		new ColorRGB(   0,   0,   0),
		new ColorRGB( 242, 252, 255),
		new ColorRGB(   0,   0,   0),
	};
		
	private final static String [] presetNames = {
		"custom/unknown",
		"Noon",
		"Metal halide",
		"Black",
	};
	
	public int lightIndex=0;
	
	private float[] position={0,0,1,0};
	private float[] spotDirection={0,0,1};

	public BooleanEntity enabled = new BooleanEntity("On",true);
	public BooleanEntity isDirectional = new BooleanEntity("Spotlight",false);

	public IntEntity preset = new IntEntity("Preset",0);
	public ColorEntity diffuse = new ColorEntity("Diffuse" ,0,0,0,1);
	public ColorEntity specular= new ColorEntity("Specular",0,0,0,1);
	public ColorEntity ambient = new ColorEntity("Ambient" ,0,0,0,1);
	
	public DoubleEntity cutoff = new DoubleEntity("Spot cone (0...90)",180);
	public DoubleEntity exponent = new DoubleEntity("Spot Exponent",0);
	
	public DoubleEntity attenuationConstant = new DoubleEntity("Constant attenuation",1.0);
	public DoubleEntity attenuationLinear = new DoubleEntity("Linear attenuation",0.014);
	public DoubleEntity attenuationQuadratic = new DoubleEntity("Quadratic attenuation",0.0007);
	
	
	public LightEntity() {
		super();
		setName("Light");
		addChild(enabled);
		addChild(diffuse);
		addChild(specular);
		addChild(ambient);
		
		addChild(isDirectional);
		addChild(cutoff);
		addChild(exponent);

		addChild(attenuationConstant);
		addChild(attenuationLinear);
		addChild(attenuationQuadratic);
	}

	public void setupLight(GL2 gl2) {
		int i = GL2.GL_LIGHT0+lightIndex;
		if(!enabled.get()) {
			gl2.glDisable(i);
			return;
		}
		gl2.glEnable(i);
		
		position[0]=(float)poseWorld.m03;
		position[1]=(float)poseWorld.m13;
		position[2]=(float)poseWorld.m23;
		position[3]=isDirectional.get()?1:0;
		gl2.glLightfv(i, GL2.GL_POSITION, position,0);
		
	    gl2.glLightfv(i, GL2.GL_AMBIENT, ambient.getFloatArray(),0);
	    gl2.glLightfv(i, GL2.GL_DIFFUSE, diffuse.getFloatArray(),0);
	    gl2.glLightfv(i, GL2.GL_SPECULAR, specular.getFloatArray(),0);

	    // z axis of the matrix is the light direction
	    spotDirection[0]=(float)poseWorld.m02;
	    spotDirection[1]=(float)poseWorld.m12;
	    spotDirection[2]=(float)poseWorld.m22;
		gl2.glLightfv(i, GL2.GL_SPOT_DIRECTION, spotDirection,0);
	    
	    gl2.glLightf(i, GL2.GL_SPOT_CUTOFF, cutoff.get().floatValue());
	    gl2.glLightf(i, GL2.GL_SPOT_EXPONENT, exponent.get().floatValue());
	    
	    // falloff/fade out
	    gl2.glLightf(i, GL2.GL_CONSTANT_ATTENUATION,attenuationConstant.get().floatValue());
	    gl2.glLightf(i, GL2.GL_LINEAR_ATTENUATION,attenuationLinear.get().floatValue());
	    gl2.glLightf(i, GL2.GL_QUADRATIC_ATTENUATION,attenuationQuadratic.get().floatValue());
	}
	
	// OpenGL camera: -Z=forward, +X=right, +Y=up
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
			PrimitiveSolids.drawStar(gl2, 10);
		gl2.glPopMatrix();
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

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Li", "Light");
		view.addComboBox(preset, presetNames);
		getViewOfChildren(view);
		view.popStack();
		
		super.getView(view);
	}
	
	public void setPreset(int i) {
		ColorRGB [] choice;
		
		switch(i) {
		case 1: choice = presetNoon;		break;
		case 2:	choice = presetMetalHalide; break;
		case 3: choice = presetBlack;		break;
		default: choice=null;
		}
		
		if(choice!=null) {
			ColorRGB c;
			c= choice[0];	this.setAmbient (c.red/255, c.green/255, c.blue/255, 1);
			c= choice[1];	this.setSpecular(c.red/255, c.green/255, c.blue/255, 1);
			c= choice[2];	this.setDiffuse (c.red/255, c.green/255, c.blue/255, 1);
		}
	}
}
