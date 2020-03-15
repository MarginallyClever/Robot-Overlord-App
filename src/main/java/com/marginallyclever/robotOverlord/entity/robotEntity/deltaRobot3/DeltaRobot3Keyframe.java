package com.marginallyclever.robotOverlord.entity.robotEntity.deltaRobot3;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.entity.robotEntity.RobotKeyframe;


/**
 * MotionState captures the physical state of a robot at a moment in time.
 * @author Dan Royer
 *
 */
@Deprecated
public class DeltaRobot3Keyframe implements RobotKeyframe {
	// angle of rotation
	public DeltaRobot3Arm arms[];

	// Relative to base
	public Vector3d fingerPosition = new Vector3d(0,0,0);

	// base orientation, affects entire arm
	public Vector3d base = new Vector3d();  // relative to world
	public Vector3d base_forward = new Vector3d();
	public Vector3d base_up = new Vector3d();
	public Vector3d base_right = new Vector3d();

	// rotating entire robot
	public double basePan=0;
	public double baseTilt=0;


	public DeltaRobot3Keyframe() {
		arms = new DeltaRobot3Arm[DeltaRobot3.NUM_ARMS];
		int i;
		for(i=0;i<DeltaRobot3.NUM_ARMS;++i) {
			arms[i] = new DeltaRobot3Arm();
		}
	}

	public void set(DeltaRobot3Keyframe other) {
		fingerPosition.set(other.fingerPosition);
		int i;
		for(i=0;i<DeltaRobot3.NUM_ARMS;++i) {
			arms[i].set(other.arms[i]);
		}
		base.set(other.base);
		base_forward.set(other.base_forward);
		base_up.set(other.base_up);
		base_right.set(other.base_right);
		basePan = other.basePan;
		baseTilt = other.baseTilt;
	}

	@Override
	public void interpolate(RobotKeyframe a, RobotKeyframe b, double t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(GL2 gl2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renderInterpolation(GL2 gl2, RobotKeyframe arg1) {
		// TODO Auto-generated method stub
		
	}
};
