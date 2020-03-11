package com.marginallyclever.robotOverlord.engine.dhRobot.tools;

import java.util.List;
import java.util.StringTokenizer;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHTool;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.uiElements.InputManager;


/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 */
public class DHTool_Gripper extends DHTool {
	/**
	 * A PhysicalObject, if any, being held by the tool.  Assumes only one object can be held.
	 */
	private transient PhysicalObject subjectBeingHeld;
	
	private double gripperServoAngle;
	public static final double ANGLE_MAX=55;
	public static final double ANGLE_MIN=10;
	
	private double interpolatePoseT;
	private double startT,endT;
	
	private DHLink[] subComponents = new DHLink[6];
	
	private transient boolean wasGripping;
	
	public DHTool_Gripper() {
		super();
		setLetter("T");
		setName("Gripper");
		refreshPoseMatrix();
		
		gripperServoAngle=90;
		interpolatePoseT=1;
		startT=endT=gripperServoAngle;
		
		setModelFilename("/Sixi2/beerGripper/base.stl");
		setModelScale(0.1f);
		setModelOrigin(-1,0,4.15);
		setModelRotation(0,180,90);
		

		Matrix3d r = new Matrix3d();
		r.setIdentity();
		r.rotX(Math.toRadians(180));
		Matrix3d r2 = new Matrix3d();
		r2.setIdentity();
		r2.rotZ(Math.toRadians(90));
		r.mul(r2);
		this.setRotation(r);
		
		// 4 bars
		addChild(subComponents[0]=new DHLink());
		addChild(subComponents[1]=new DHLink());
		addChild(subComponents[2]=new DHLink());
		addChild(subComponents[3]=new DHLink());
		subComponents[0].setModelFilename("/Sixi2/beerGripper/linkage.stl");
		subComponents[0].setModelScale(0.1f);
		subComponents[1].set(subComponents[0]);
		subComponents[2].set(subComponents[0]);
		subComponents[3].set(subComponents[0]);
		subComponents[0].setPosition(new Vector3d(2.7/2, 0, 4.1));
		subComponents[1].setPosition(new Vector3d(1.1/2, 0, 5.9575));
		subComponents[2].setPosition(new Vector3d(-2.7/2, 0, 4.1));
		subComponents[3].setPosition(new Vector3d(-1.1/2, 0, 5.9575));
		
		// 2 finger tips
		addChild(subComponents[4]=new DHLink());
		subComponents[4].setModelFilename("/Sixi2/beerGripper/finger.stl");
		subComponents[4].setModelScale(0.1f);
		addChild(subComponents[5]=new DHLink());
		subComponents[5].set(subComponents[4]);
		
		wasGripping=false;
	}
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
/*
		material.render(gl2);
		
		gl2.glPushMatrix();
			gl2.glRotated(90, 0, 0, 1);
		
			double v = -180-this.gripperServoAngle;
		
			gl2.glPushMatrix();
			gl2.glRotated(v, 0, 1, 0);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glRotated(v, 0, 1, 0);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glRotated(-v, 0, 1, 0);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glRotated(-v, 0, 1, 0);
			gl2.glPopMatrix();

			double c=Math.cos(Math.toRadians(v));
			double s=Math.sin(Math.toRadians(v));
			gl2.glPushMatrix();
			gl2.glTranslated(-2.7/2-s*4.1, 0, 4.1+c*4.1);
			gl2.glScaled(1,1,-1);
			//gl2.glTranslated(s*4.1, 2.7/2, 4.1+c*4.1);
			finger.render(gl2);
			gl2.glPopMatrix();
			
			gl2.glPushMatrix();
			gl2.glTranslated(2.7/2+s*4.1, 0, 4.1+c*4.1);
			gl2.glScaled(-1,1,-1);
			finger.render(gl2);
			gl2.glPopMatrix();
		
		gl2.glPopMatrix();
		*/
	}

	/**
	 * Read HID device to move target pose.  Currently hard-coded to PS4 joystick values. 
	 * @return true if targetPose changes.
	 */
	@Override
	public boolean directDrive() {
		boolean isDirty=false;
		final double scaleGrip=1.8;
		
		if(InputManager.isOn(InputManager.Source.STICK_CIRCLE) && !wasGripping) {
			wasGripping=true;
			// grab release
			if(subjectBeingHeld==null) {
				//System.out.println("Grab");
				// Get the object at the targetPos.
				Vector3d target = new Vector3d();
				this.poseWorld.get(target);
				List<PhysicalObject> list = this.getWorld().findPhysicalObjectsNear(target, 10);
				if(!list.isEmpty()) {
					subjectBeingHeld = list.get(0);
					// A new subject has been acquired.
					// The subject is being held by the gripper.  Subtract the gripper's world pose from the subject's world pose.
					Matrix4d m = subjectBeingHeld.getPose();
					Matrix4d ipc = (Matrix4d)poseWorld.clone();
					ipc.invert();
					m.mul(ipc);
					subjectBeingHeld.setPose(m);
				}
			} else {
				//System.out.println("Release");
				// The subject is being held relative to the gripper.  Add the gripper's world pose to the subject's pose.
				Matrix4d m = subjectBeingHeld.getPose();
				m.mul(poseWorld);
				subjectBeingHeld.setPose(m);
				// forget the subject.
				subjectBeingHeld=null;
			}
		}
		if(InputManager.isOff(InputManager.Source.STICK_CIRCLE)) wasGripping=false;
		
        if(InputManager.isOn(InputManager.Source.STICK_OPTIONS)) {
			if(gripperServoAngle<ANGLE_MAX) {
				gripperServoAngle+=scaleGrip;
				if(gripperServoAngle>ANGLE_MAX) gripperServoAngle=ANGLE_MAX;
				isDirty=true;
			}
        }
        if(InputManager.isOn(InputManager.Source.STICK_SHARE)) {
			if(gripperServoAngle>ANGLE_MIN) {
				gripperServoAngle-=scaleGrip;
				if(gripperServoAngle<ANGLE_MIN) gripperServoAngle=ANGLE_MIN;
				isDirty=true;
			}
        }

        return isDirty;
	}

	@Override
	public String getCommand() {
		return getLetter()+StringHelper.formatDouble(this.gripperServoAngle);
	}
	
	@Override
	public void sendCommand(String str) {
		StringTokenizer tok = new StringTokenizer(str);
		while(tok.hasMoreTokens()) {
			String token = tok.nextToken();
			try {
				if(token.startsWith("T")) {
					startT = gripperServoAngle;
					endT = Double.parseDouble(token.substring(1));
				}
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
		}
		gripperServoAngle=endT;
		interpolatePoseT=0;
	}
	
	@Override
	public void interpolate(double dt) {
		super.interpolate(dt);
		
		if(interpolatePoseT<1) {
			interpolatePoseT+=dt;
			if(interpolatePoseT>=1) {
				interpolatePoseT=1;
			}
			gripperServoAngle=((endT-startT)*interpolatePoseT + startT);
			refreshPoseMatrix();
		}
	}
	
	@Override
	public double getAdjustableValue() {
		return gripperServoAngle;
	}
	
	@Override
	public void setAdjustableValue(double v) {
		v = Math.max(Math.min(v, rangeMax), rangeMin);
		gripperServoAngle=v;
	}
}
