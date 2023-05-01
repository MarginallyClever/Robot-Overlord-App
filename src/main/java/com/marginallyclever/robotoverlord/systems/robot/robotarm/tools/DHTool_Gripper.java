package com.marginallyclever.robotoverlord.systems.robot.robotarm.tools;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.DHComponent;


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
	protected transient Entity subjectBeingHeld;
	
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
	public boolean directDrive() {
        return false;
	}
}
