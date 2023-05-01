package com.marginallyclever.robotoverlord.systems.robot.robotarm.robotarm.robotarmtools;

import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.convenience.memento.MementoDoubleArray;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.robots.PoseEntity;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;


/**
 * @author Dan Royer
 */
@Deprecated
public class Sixi2LinearGripper extends Entity {
	// A PhysicalObject, if any, being held by the tool.  Assumes only one object can be held.\
	private transient PoseEntity subjectBeingHeld;
	
	public IntParameter angleMax = new IntParameter("Max open",135);
	public IntParameter angleMin = new IntParameter("Min closed",100);
	public IntParameter angleNow = new IntParameter("Now",100);  // 0...100%
	
	private double interpolatePoseT;
	private double startT,endT;
	public Entity leftFinger;
	public Entity rightFinger;
		
	private transient boolean wasGripping;
	
	public Sixi2LinearGripper() {
		super();
	}
	/*
		setLetter("T");
		setName("Sixi2 Linear Gripper");
		
		interpolatePoseT=1;
		startT=endT=angleNow.get();
		
		setShapeFilename("/robots/Sixi2/linearGripper/gripperBase.obj");
		shapeEntity.getMaterial().setTextureFilename("/robots/Sixi2/sixi.png");
		shapeEntity.getMaterial().setDiffuseColor(1, 1, 1, 1);
		shapeEntity.getMaterial().setAmbientColor(1, 1, 1, 1);
		
		// 2 finger tips
		addEntity(leftFinger=new Entity());
		addEntity(rightFinger=new Entity());
		leftFinger.setName("Left finger");
		leftFinger.setShapeFilename("/robots/Sixi2/linearGripper/gripperLeft.obj");
		leftFinger.setTextureFilename("/robots/Sixi2/sixi.png");
		rightFinger.setName("Right finger");
		rightFinger.setShapeFilename("/robots/Sixi2/linearGripper/gripperRight.obj");
		rightFinger.setTextureFilename("/robots/Sixi2/sixi.png");

		leftFinger.flags = Entity.LinkAdjust.R;
		rightFinger.flags = Entity.LinkAdjust.R;
		
		addEntity(angleMax);
		addEntity(angleMin);
		addEntity(angleNow);
		
		wasGripping=false;
	}
	
	@Override
	public void update(double dt) {
		double now = angleNow.get();
		double v = (100.0-now)/100.0;
		double rangeMax = 2.65;
		double rangeMin = 0.1;
		double v2 = ( rangeMax-rangeMin ) * v + rangeMin;
		leftFinger.setAdjustableValue(v2);
		rightFinger.setAdjustableValue(-v2);
		super.update(dt);
	}
	*/

	/**
	 * Read HID device to move target pose.  Currently hard-coded to PS4 joystick values. 
	 * @return true if targetPose changes.
	 */
	public boolean directDrive() {
        return false;
	}

	@Deprecated
	public void getView(ComponentPanelFactory view) {
		view.addRange(angleNow, 100, 0);
		view.addRange(angleMax, 180, 0);
		view.addRange(angleMin, 180, 0);
	}

	public Memento getState() {
		MementoDoubleArray a = new MementoDoubleArray(1);
		a.values[0] = angleNow.get();
		return null;
	}

	public void setState(Memento arg0) {
		angleNow.set((int)((MementoDoubleArray)arg0).values[0]);
	}
}
