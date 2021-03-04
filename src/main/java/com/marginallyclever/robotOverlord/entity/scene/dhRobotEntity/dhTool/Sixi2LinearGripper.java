package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool;

import java.util.List;
import java.util.StringTokenizer;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.convenience.memento.MementoDoubleArray;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;


/**
 * @author Dan Royer
 */
public class Sixi2LinearGripper extends DHTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2785349705816889040L;

	/**
	 * A PhysicalObject, if any, being held by the tool.  Assumes only one object can be held.
	 */
	private transient PoseEntity subjectBeingHeld;
	
	public IntEntity angleMax = new IntEntity("Max open",135);
	public IntEntity angleMin = new IntEntity("Min closed",100);
	public IntEntity angleNow = new IntEntity("Now",100);  // 0...100%
	
	private double interpolatePoseT;
	private double startT,endT;
	public DHLink leftFinger;
	public DHLink rightFinger;
		
	private transient boolean wasGripping;
	
	public Sixi2LinearGripper() {
		super();
		setLetter("T");
		setName("Sixi2 Linear Gripper");
		refreshDHMatrix();
		
		interpolatePoseT=1;
		startT=endT=angleNow.get();
		
		setShapeFilename("/Sixi2/linearGripper/gripperBase.obj");
		shapeEntity.getMaterial().setTextureFilename("/Sixi2/sixi.png");
		shapeEntity.getMaterial().setDiffuseColor(1, 1, 1, 1);
		shapeEntity.getMaterial().setAmbientColor(1, 1, 1, 1);
		
		// 2 finger tips
		addChild(leftFinger=new DHLink());
		addChild(rightFinger=new DHLink());
		leftFinger.setName("Left finger");
		leftFinger.setShapeFilename("/Sixi2/linearGripper/gripperLeft.obj");
		leftFinger.setTextureFilename("/Sixi2/sixi.png");
		rightFinger.setName("Right finger");
		rightFinger.setShapeFilename("/Sixi2/linearGripper/gripperRight.obj");
		rightFinger.setTextureFilename("/Sixi2/sixi.png");

		leftFinger.flags = DHLink.LinkAdjust.R;
		rightFinger.flags = DHLink.LinkAdjust.R;
		
		addChild(angleMax);
		addChild(angleMin);
		addChild(angleNow);
		
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
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		super.render(gl2);
		gl2.glPopMatrix();
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
			Matrix4d poseWorld = new Matrix4d();
			getPoseWorld(poseWorld);
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
					Matrix4d iPoseWorld = new Matrix4d(poseWorld);
					iPoseWorld.invert();
					m.mul(iPoseWorld);
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
			if(angleNow.get()<angleMax.get()) {
				angleNow.set((int)(angleNow.get()+scaleGrip));
				if(angleNow.get()>angleMax.get()) angleNow.set(angleMax.get());
				isDirty=true;
			}
        }
        if(InputManager.isOn(InputManager.Source.STICK_SHARE)) {
			if(angleNow.get()>angleMin.get()) {
				angleNow.set((int)(angleNow.get()-scaleGrip));
				if(angleNow.get()<angleMin.get()) angleNow.set(angleMin.get());
				isDirty=true;
			}
        }

        return isDirty;
	}

	@Override
	public String getCommand() {
		return getLetter()+angleNow.get();
	}
	
	@Override
	public void sendCommand(String str) {
		StringTokenizer tok = new StringTokenizer(str);
		while(tok.hasMoreTokens()) {
			String token = tok.nextToken();
			try {
				if(token.startsWith("T")) {
					startT = angleNow.get();
					endT = StringHelper.parseNumber(token.substring(1));
				}
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
		}
		angleNow.set((int)endT);
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
			angleNow.set((int)((endT-startT)*interpolatePoseT + startT));
			refreshDHMatrix();
		}
	}
	
	@Override
	public double getAdjustableValue() {
		return angleNow.get();
	}
	
	@Override
	public void setAdjustableValue(double v) {
		v = Math.max(Math.min(v, rangeMax.get()), rangeMin.get());
		angleNow.set((int)v);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Gr", "Gripper");
		view.addRange(angleNow, 100, 0);
		view.addRange(angleMax, 180, 0);
		view.addRange(angleMin, 180, 0);
		view.popStack();
		super.getView(view);
	}

	@Override
	public Memento getState() {
		MementoDoubleArray a = new MementoDoubleArray(1);
		a.values[0] = angleNow.get();
		return null;
	}

	@Override
	public void setState(Memento arg0) {
		angleNow.set((int)((MementoDoubleArray)arg0).values[0]);
	}
}
