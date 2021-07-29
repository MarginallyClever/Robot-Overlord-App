package com.marginallyclever.robotOverlord.robots.dog;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;

public class DogLeg {
	private PoseEntity myParent;
	private Sixi3Bone shoulderA = new Sixi3Bone();
	private Sixi3Bone shoulderB = new Sixi3Bone();
	private Sixi3Bone elbow = new Sixi3Bone();
	private Sixi3Bone foot = new Sixi3Bone();

	// actual toe location
	public Vector3d toe = new Vector3d();
	// desired toe location
	public Vector3d toeTarget = new Vector3d();
	// desired toe location, second order
	public Vector3d toeTarget2 = new Vector3d();

	private double[] idealStandingAngles = new double[4];

	public DogLeg(PoseEntity parent, double r, double d) {
		super();
		myParent = parent;
		setDHParametersForLeg(r, d);
	}

	private void setDHParametersForLeg(double r, double d) {
		shoulderA.set(r, d, 0, 0, 360, -360, "");
		shoulderB.set(0, 0, 90, -90, 360, -360, "");
		elbow.set(11.5, 0, 0, -45, 0, -180, "");
		foot.set(13, 0, 0, 90, 360, -360, "");
		refreshMatrixes();
		captureAngles(idealStandingAngles);
		// toeTarget2.set(toe);
		// toeTarget.set(toe);
	}

	public void captureAngles(double[] legAngles) {
		legAngles[0] = shoulderA.theta;
		legAngles[1] = shoulderB.theta;
		legAngles[2] = elbow.theta;
		legAngles[3] = foot.theta;
	}

	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		drawLineTo(gl2, shoulderA.pose, 255, 0, 0);
		drawLineTo(gl2, shoulderB.pose, 0, 0, 0);
		drawLineTo(gl2, elbow.pose, 0, 255, 0);
		drawLineTo(gl2, foot.pose, 0, 0, 255);
		gl2.glPopMatrix();
	}

	private void drawLineTo(GL2 gl2, Matrix4d m, double r, double g, double b) {
		Vector3d v = new Vector3d();
		m.get(v);

		gl2.glLineWidth(5);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(r, g, b);
		gl2.glVertex3d(0, 0, 0);
		gl2.glVertex3d(v.x, v.y, v.z);
		gl2.glEnd();
		gl2.glLineWidth(1);

		MatrixHelper.applyMatrix(gl2, m);
	}

	public boolean isTouchingTheFloor() {
		return toe.z < 0.001;
	}

	public void setAngles(double[] angles) {
		shoulderA.setAngleWRTLimits(angles[0]);
		shoulderB.setAngleWRTLimits(angles[1]);
		elbow.setAngleWRTLimits(angles[2]);
		foot.setAngleWRTLimits(angles[3]);
		refreshMatrixes();
	}

	public void refreshMatrixes() {
		shoulderA.updateMatrix();
		shoulderB.updateMatrix();
		elbow.updateMatrix();
		foot.updateMatrix();
		toe.set(MatrixHelper.getPosition(getWorldMatrixOfToe()));
	}

	public Matrix4d getWorldMatrixOfToe() {
		Matrix4d m = myParent == null ? MatrixHelper.createIdentityMatrix4() : myParent.getPose();
		m.mul(shoulderA.pose);
		m.mul(shoulderB.pose);
		m.mul(elbow.pose);
		m.mul(foot.pose);
		return m;
	}

	public double[] getAngles() {
		double[] legAngles = new double[4];
		legAngles[0] = shoulderA.theta;
		legAngles[1] = shoulderB.theta;
		legAngles[2] = elbow.theta;
		legAngles[3] = foot.theta;
		return legAngles;
	}

	public void setIdealStandingAngles() {
		setAngles(idealStandingAngles);
	}

	public void relaxShoulder() {
		shoulderA.theta = idealStandingAngles[0];
	}

	public Vector3d getPointOnFloorUnderShoulder() {
		Matrix4d m = myParent == null ? MatrixHelper.createIdentityMatrix4() : myParent.getPose();
		m.mul(shoulderA.pose);
		m.mul(shoulderB.pose);
		Vector3d fp = MatrixHelper.getPosition(m);
		fp.z=0;

		return fp;
	}
}