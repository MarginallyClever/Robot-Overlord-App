package com.marginallyclever.robotOverlord.robots.dog;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

public class DogLeg {
	private PoseEntity myParent;

	private Sixi3Bone shoulderA = new Sixi3Bone();
	private Sixi3Bone shoulderB = new Sixi3Bone();
	private Sixi3Bone elbow = new Sixi3Bone();
	private Sixi3Bone foot = new Sixi3Bone();

	private MaterialEntity matOnFloor = new MaterialEntity();
	private MaterialEntity matStepping = new MaterialEntity();

	private Vector3d toe = new Vector3d();
	private Vector3d gradientDescentTarget = new Vector3d();
	// desired toe location, second order
	public Vector3d toeTarget2 = new Vector3d();

	private double[] idealStandingAngles;

	public DogLeg(PoseEntity parent, double r, double d) {
		super();
		myParent = parent;
		setDHParameters(r, d);

		matOnFloor.setLit(true);
		matOnFloor.setDiffuseColor(1, 0, 0, 1);
		matStepping.setLit(true);
		matStepping.setDiffuseColor(1,1,1,1);
	}

	private void setDHParameters(double r, double d) {
		shoulderA.set(r, d, 0, 0, 360, -360, "");
		shoulderB.set(0, 0, 90, -90, 360, -360, "");
		elbow.set(11.5, 0, 0, -45, 0, -180, "");
		foot.set(13, 0, 0, 90, 360, -360, "");
		refreshMatrixes();
		idealStandingAngles = getAngles();
		gradientDescentTarget.set(toe);
		toeTarget2.set(toe);
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

	public boolean isToeTouchingTheFloor() {
		return toe.z < 0.01;
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

	public Matrix4d getWorldMatrixOfShoulder() {
		Matrix4d m = myParent == null ? MatrixHelper.createIdentityMatrix4() : myParent.getPose();
		m.mul(shoulderA.pose);
		m.mul(shoulderB.pose);
		return m;
	}

	public Vector3d getPointOnFloorUnderShoulder() {
		Vector3d fp = MatrixHelper.getPosition(getWorldMatrixOfShoulder());
		fp.z=0;
		return fp;
	}

	public Vector3d getPointOnFloorUnderToe() {
		Vector3d fp = MatrixHelper.getPosition(getWorldMatrixOfToe());
		fp.z=0;
		return fp;
	}
	
	public double getGradientDescentScore(double [] legAngles) {
		Vector3d fp = new Vector3d();
		fp.sub(gradientDescentTarget,toe);
		return fp.lengthSquared();
	}
	
	// Move the toes towards the toeTargets.
	public void gradientDescent(double epsilon) {		
		double [] legAngles = getAngles();
		
		double stepSize=10;
		for(int tries=0;tries<15;++tries) {
			int i;
			// work from toe to shoulder, seems to finish faster than shoulder to toe.
			for(i=legAngles.length-1;i>=0;--i) {
				if(partialDescent(legAngles,i,stepSize,epsilon)) break;
			}
			if(i>0) break;
			
			stepSize*=0.75;
		}
	}
	
	// Wiggle leg joint 'i' to see which way gets a better score.
	private boolean partialDescent(double[] legAngles,int i,double stepSize,double epsilon) {
		double startAngle = legAngles[i];
		double bestAngle = startAngle;

		double startScore = getGradientDescentScore(legAngles);
		double bestScore = startScore;
		if(bestScore < epsilon) return true;

		legAngles[i] = startAngle-stepSize;
		setAngles(legAngles);
		double scoreNeg = getGradientDescentScore(legAngles);
		if(bestScore>scoreNeg) {
			bestScore = scoreNeg;
			bestAngle = legAngles[i];
			if(bestScore < epsilon) return true;
		}
		
		legAngles[i] = startAngle+stepSize;
		setAngles(legAngles);
		double scorePos = getGradientDescentScore(legAngles);
		if(bestScore>scorePos) {
			bestScore = scorePos;
			bestAngle = legAngles[i];
			if(bestScore < epsilon) return true;
		}
		
		legAngles[i] = bestAngle;
		setAngles(legAngles);
		
		return false;
	}
	
	public void moveToeTargetSmoothly(double scale) {
		Vector3d v = new Vector3d();
		v.sub(toeTarget2,gradientDescentTarget);
		v.scale(scale);
		gradientDescentTarget.add(v);
	}


	public void drawToeTarget(GL2 gl2) {
		gl2.glPushMatrix();
		gl2.glTranslated(gradientDescentTarget.x, gradientDescentTarget.y, gradientDescentTarget.z);

		if(isToeTouchingTheFloor()) matStepping.render(gl2);
		else						matOnFloor.render(gl2);
		
		PrimitiveSolids.drawSphere(gl2, 0.5);
		gl2.glPopMatrix();
	}
	
}