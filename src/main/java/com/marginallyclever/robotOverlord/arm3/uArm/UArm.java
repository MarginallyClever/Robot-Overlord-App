package com.marginallyclever.robotOverlord.arm3.uArm;

import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.arm3.Arm3;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;

public class UArm extends Arm3 {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient Model base = null;
	private transient Model shoulder = null;
	private transient Model bicep = null;
	private transient Model elbowHorn = null;
	private transient Model forearm = null;
	private transient Model wrist = null;
	private transient Model elbow = null;
	private transient Model wristTendon1 = null;
	private transient Model wristTendon2 = null;
	private transient Model forearmTendon = null;
	
	public UArm() {
		super(new UArmDimensions());

		base = ModelFactory.createModelFromFilename("/uArm/1.STL",0.1f);
		shoulder = ModelFactory.createModelFromFilename("/uArm/2.STL",0.1f);
		elbowHorn = ModelFactory.createModelFromFilename("/uArm/4.STL",0.1f);
		forearmTendon = ModelFactory.createModelFromFilename("/uArm/6.STL",0.1f);
		bicep = ModelFactory.createModelFromFilename("/uArm/3.STL",0.1f);
		forearm = ModelFactory.createModelFromFilename("/uArm/9.STL",0.1f);
		wrist = ModelFactory.createModelFromFilename("/uArm/10.STL",0.1f);
		wristTendon1 = ModelFactory.createModelFromFilename("/uArm/5.STL",0.1f);
		wristTendon2 = ModelFactory.createModelFromFilename("/uArm/8.STL",0.1f);
		elbow = ModelFactory.createModelFromFilename("/uArm/7.STL",0.1f);
		
		//bicep.adjustOrigin(0.6718f*2.54f, 0, -3.5625f*2.54f);
		//elbowHorn.adjustOrigin(0.6718f*2.54f, 0, -3.5625f*2.54f);
		//forearm.adjustOrigin(-4.0334f*2.54f,0,-8.3535f*2.54f);
		
		material.setDiffuseColor(1, 0.8f, 1, 1);
	}

	// TODO clean up all the magic number bullshit.
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		Vector3f p = this.getPosition();
		gl2.glTranslatef(p.x,p.y,p.z);

		// draw models
		material.setDiffuseColor(0.75f*247.0f/255.0f,
				0.75f*233.0f/255.0f,
				0.75f*215.0f/255.0f, 1);
		material.render(gl2);

		gl2.glTranslated(0, 0, 1.65);
		base.render(gl2);
		
		// shoulder
		gl2.glRotatef(this.motionNow.angleBase, 0,0,1);
		gl2.glPushMatrix();
		gl2.glRotatef(-90, 1,0,0);
		gl2.glTranslated(-8.05, -5.325, 2.75);
		shoulder.render(gl2);
		gl2.glPopMatrix();

		// bicep
		gl2.glPushMatrix();
		gl2.glRotatef(-90, 1,0,0);
		gl2.glTranslated(2.1,-7.9,2.0);
		gl2.glRotatef(-90+this.motionNow.angleShoulder, 0, 0, 1);
		bicep.render(gl2);
		gl2.glPopMatrix();

		// elbow motor
		gl2.glPushMatrix();
		gl2.glRotatef(90, 1,0,0);
		gl2.glTranslated(2.1,7.9,2.0);
		gl2.glRotatef(this.motionNow.angleElbow-90, 0, 0, 1);
		elbowHorn.render(gl2);
		gl2.glPopMatrix();

		// forearm
		double r;
		float fs,fc,foreX,foreY,foreZ;
		r = Math.toRadians(-90-this.motionNow.angleShoulder);
		fs = (float)Math.sin(r);
		fc = (float)Math.cos(r);
		foreX = -2.1f + fs*14.75f;
		foreY = 7.9f + fc*14.75f;
		foreZ = -0.850f;
		gl2.glPushMatrix();
		gl2.glRotatef(90, 1,0,0);
		gl2.glRotatef(180, 0,1,0);
		gl2.glTranslated(foreX,foreY,foreZ);
		gl2.glRotatef(180-this.motionNow.angleElbow, 0, 0, 1);
		forearm.render(gl2);
		gl2.glPopMatrix();

		// elbow
		gl2.glPushMatrix();
		gl2.glRotatef(90, 1,0,0);
		fs = (float)Math.sin(-r);
		fc = (float)Math.cos(-r);
		foreX = -2.1f + fs*14.75f;
		foreY = 7.9f + fc*14.75f;
		foreZ = -0.850f;
		gl2.glTranslated(foreX,
				foreY,
				foreZ);
		gl2.glTranslated(4.2, 2.4, -1.53);
		gl2.glRotatef(180, 0, 0, 1);
		elbow.render(gl2);
		gl2.glPopMatrix();
		
		
		gl2.glPushMatrix();
		gl2.glRotatef(90, 1,0,0);
		gl2.glRotatef(180, 0,1,0);
		fs = (float)Math.sin(r);
		fc = (float)Math.cos(r);
		foreX = -2.1f + fs*14.75f;
		foreY = 7.9f + fc*14.75f;
		foreZ = -0.79f;
		float ws,wc,wristX,wristY,wristZ;
		r = Math.toRadians(90+this.motionNow.angleElbow);
		ws = (float)Math.sin(r);
		wc = (float)Math.cos(r);
		wristX = ws*16.0f;
		wristY = wc*16.0f;
		wristZ = 2.0f;
		gl2.glTranslated(foreX+wristX,
						foreY+wristY,
						foreZ+wristZ);
		wrist.render(gl2);
		gl2.glPopMatrix();
		
		gl2.glPushMatrix();
		gl2.glRotatef(90, 1,0,0);
		r = Math.toRadians(90-this.motionNow.angleElbow);
		fs = (float)Math.sin(r);
		fc = (float)Math.cos(r);
		foreX = 2.1f + fs*5.4f;
		foreY = 7.9f + fc*5.4f;
		foreZ = -0.550f;
		gl2.glTranslated(foreX,
				foreY,
				foreZ);
		gl2.glRotatef(-180-this.motionNow.angleShoulder, 0,0,1);
		forearmTendon.render(gl2);
		gl2.glPopMatrix();
		
		gl2.glPushMatrix();
		gl2.glRotatef(90, 1,0,0);
		gl2.glTranslated(-1.34,10.275,-3.15);
		gl2.glRotatef(-180-this.motionNow.angleShoulder, 0,0,1);
		wristTendon1.render(gl2);
		gl2.glPopMatrix();
		
		gl2.glPushMatrix();
		gl2.glRotatef(90, 1,0,0);
		r = Math.toRadians(90+this.motionNow.angleShoulder);
		fs = (float)Math.sin(r);
		fc = (float)Math.cos(r);
		foreX = 5.5f + fs*14.75f;
		foreY = 10.3f + fc*14.75f;
		foreZ = -2.95f;
		gl2.glTranslated(foreX,
				foreY,
				foreZ);
		gl2.glRotatef(+this.motionNow.angleElbow, 0,0,1);
		wristTendon2.render(gl2);
		gl2.glPopMatrix();
		
		gl2.glPopMatrix();
		
		//super.render(gl2);
	}
	boolean once=false;
}
