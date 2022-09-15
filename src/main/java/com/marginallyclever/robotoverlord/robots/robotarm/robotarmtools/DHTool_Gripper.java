package com.marginallyclever.robotoverlord.robots.robotarm.robotarmtools;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.convenience.memento.MementoOriginator;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.DHComponent;
import com.marginallyclever.robotoverlord.entities.PoseEntity;
import com.marginallyclever.robotoverlord.swinginterface.InputManager;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.StringTokenizer;


/**
 * @author Dan Royer
 */
@Deprecated
public class DHTool_Gripper extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1544140469103301389L;
	public static final double ANGLE_MAX=55;
	public static final double ANGLE_MIN=10;
	
	protected DHComponent[] subComponents = new DHComponent[6];

	protected double gripperServoAngle;
	protected double interpolatePoseT;
	protected double startT,endT;

	protected transient boolean wasGripping;
	protected transient PoseEntity subjectBeingHeld;
	
	public DHTool_Gripper() {
		super();/*
		setLetter("T");
		setName("Gripper");
		
		gripperServoAngle=90;
		interpolatePoseT=1;
		startT=endT=gripperServoAngle;
		
		setShapeFilename("/robots/Sixi2/beerGripper/base.stl");
		shapeEntity.setShapeScale(0.1f);
		shapeEntity.setShapeOrigin(-1,0,4.15);
		shapeEntity.setShapeRotation(0,180,90);
		

		Matrix3d r = new Matrix3d();
		r.setIdentity();
		r.rotX(Math.toRadians(180));
		Matrix3d r2 = new Matrix3d();
		r2.setIdentity();
		r2.rotZ(Math.toRadians(90));
		r.mul(r2);
		this.setRotation(r);
		
		// 4 bars
		addComponent(subComponents[0]=new DHComponent());
		addComponent(subComponents[1]=new DHComponent());
		addComponent(subComponents[2]=new DHComponent());
		addComponent(subComponents[3]=new DHComponent());
		subComponents[0].setShapeFilename("/robots/Sixi2/beerGripper/linkage.stl");
		subComponents[0].setShapeScale(0.1);
		subComponents[1].set(subComponents[0]);
		subComponents[2].set(subComponents[0]);
		subComponents[3].set(subComponents[0]);
		subComponents[0].setPosition(new Vector3d(2.7/2, 0, 4.1));
		subComponents[1].setPosition(new Vector3d(1.1/2, 0, 5.9575));
		subComponents[2].setPosition(new Vector3d(-2.7/2, 0, 4.1));
		subComponents[3].setPosition(new Vector3d(-1.1/2, 0, 5.9575));
		
		// 2 finger tips
		addComponent(subComponents[4]=new DHComponent());
		subComponents[4].setShapeFilename("/robots/Sixi2/beerGripper/finger.stl");
		subComponents[4].setShapeScale(0.1);
		addComponent(subComponents[5]=new DHComponent());
		subComponents[5].set(subComponents[4]);
		
		wasGripping=false;
		*/
	}
	
	/**
	 * Read HID device to move target pose.  Currently hard-coded to PS4 joystick values. 
	 * @return true if targetPose changes.
	 */
	public boolean directDrive() {/*
		boolean isDirty=false;
		final double scaleGrip=1.8;
		
		if(InputManager.isOn(InputManager.Source.STICK_CIRCLE) && !wasGripping) {
			wasGripping=true;
			Matrix4d poseWorld = getPoseWorld();
			// grab release
			if(subjectBeingHeld==null) {
				//Log.message("Grab");
				// Get the object at the targetPos.
				Vector3d target = new Vector3d();
				poseWorld.get(target);
				List<PoseEntity> list = this.getWorld().findPhysicalObjectsNear(target, 10);
				if(!list.isEmpty()) {
					subjectBeingHeld = list.get(0);
					// A new subject has been acquired.
					// The subject is being held by the gripper.  Subtract the gripper's world pose from the subject's world pose.
					Matrix4d m = subjectBeingHeld.getPose();
					Matrix4d iposeWorld = new Matrix4d(poseWorld);
					iposeWorld.invert();
					m.mul(iposeWorld);
					subjectBeingHeld.setPose(m);
				}
			} else {
				//Log.message("Release");
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

		*/
        return false;
	}
}
