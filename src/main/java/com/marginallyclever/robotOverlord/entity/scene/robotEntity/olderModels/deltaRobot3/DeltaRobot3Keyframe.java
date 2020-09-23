package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels.deltaRobot3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.memento.Memento;


/**
 * Captures the physical state of a robot at a moment in time.
 * @author Dan Royer
 *
 */
@Deprecated
public class DeltaRobot3Keyframe implements Memento {
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
	public void save(OutputStream arg0) throws IOException {
		DataOutputStream out = new DataOutputStream(arg0);
		out.writeDouble(arms[0].angle);
		out.writeDouble(arms[1].angle);
		out.writeDouble(arms[2].angle);
	}

	@Override
	public void load(InputStream arg0) throws IOException {
		DataInputStream in = new DataInputStream(arg0);
		arms[0].angle = in.readDouble();
		arms[1].angle = in.readDouble();
		arms[2].angle = in.readDouble();
	}
};
