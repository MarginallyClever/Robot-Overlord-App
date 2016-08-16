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

		base = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Base.stl",2.54f);
		shoulder = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Body.stl",2.54f);
		elbowHorn = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Shoulder.stl",2.54f);
		forearmTendon = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Arm_Back.stl",2.54f);
		bicep = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Arm_Base.stl",2.54f);
		forearm = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Forearm_Base.stl",2.54f);
		wrist = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Hand.stl",2.54f);
		wristTendon1 = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Arm_Front.stl",2.54f);
		wristTendon2 = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Forearm_Top.stl",2.54f);
		elbow = ModelFactory.createModelFromFilename("/LiteArm/LiteArm_Elbow.stl",2.54f);
		
		material.setDiffuseColor(1, 0.8f, 1, 1);
	}

	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		Vector3f p = this.getPosition();
		gl2.glTranslatef(p.x,p.y,p.z);

		// draw models
		material.setDiffuseColor(247.0f/255.0f,
				233.0f/255.0f,
				215.0f/255.0f, 1);
		material.render(gl2);
		
		base.render(gl2);
		gl2.glRotatef(this.motionNow.angleA+180, 0,0,1);
		shoulder.render(gl2);
		bicep.render(gl2);
		forearm.render(gl2);
		wrist.render(gl2);
		elbowHorn.render(gl2);
		//forearmTendon.render(gl2);
		//wristTendon1.render(gl2);
		//wristTendon2.render(gl2);
		//elbow.render(gl2);
		
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
}
