package com.marginallyclever.robotOverlord.robots;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

/**
 * Spot Micro simulation.  Robot faces +
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DogRobot extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5916361555293772951L;

	private static final double BODY_WIDTH=12;
	private static final double BODY_LENGTH=8;
	private static final double BODY_HEIGHT=32;
	
	public class Leg {
		public Sixi3Bone shoulderA = new Sixi3Bone();
		public Sixi3Bone shoulderB = new Sixi3Bone();
		public Sixi3Bone elbow = new Sixi3Bone();
		public Sixi3Bone foot = new Sixi3Bone();
		public Vector3d toeTarget = new Vector3d();
		
		public Leg() {}
	}
	
	private Leg [] legs = new Leg[4];
	private MaterialEntity mat = new MaterialEntity();
	
	
	public DogRobot() {
		super();
		setName("Dog Robot");
		
		for(int i=0;i<4;++i) {
			legs[i] = new Leg();
		}

		setDHParameters();
		
		mat.setLit(true);
	}
	
	private void setDHParameters() {
		// robot faces +Z
		// r d a t min max file
		legs[0].shoulderA.set( BODY_WIDTH/2, BODY_HEIGHT/2, 0, 0, 360, -360, "");
		legs[0].shoulderB.set(0, 0, 90, -90, 360, -360, "");
		legs[0].elbow    .set(11.5, 0, 0, -45, 360, -360, "");
		legs[0].foot     .set(13, 0, 0,  90, 360, -360, "");

		legs[1].shoulderA.set(-BODY_WIDTH/2, BODY_HEIGHT/2, 0, 0, 360, -360, "");
		legs[1].shoulderB.set(0, 0, 90, -90, 360, -360, "");
		legs[1].elbow    .set(11.5, 0, 0, -45, 360, -360, "");
		legs[1].foot     .set(13, 0, 0,  90, 360, -360, "");

		legs[2].shoulderA.set(-BODY_WIDTH/2,-BODY_HEIGHT/2, 0, 0, 360, -360, "");
		legs[2].shoulderB.set(0, 0, 90, -90, 360, -360, "");
		legs[2].elbow    .set(11.5, 0, 0, -45, 360, -360, "");
		legs[2].foot     .set(13, 0, 0,  90, 360, -360, "");

		legs[3].shoulderA.set( BODY_WIDTH/2,-BODY_HEIGHT/2, 0, 0, 360, -360, "");
		legs[3].shoulderB.set(0, 0, 90, -90, 360, -360, "");
		legs[3].elbow    .set(11.5, 0, 0, -45, 360, -360, "");
		legs[3].foot     .set(13, 0, 0,  90, 360, -360, "");
	}
	
	
	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	
	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		//walk1();
		walk2();
		gradientDescent();

		drawCurrentDogPose(gl2);
		//drawAllShoulderFloorCircles(gl2);
		drawToeTarget(gl2);
		drawPointOnGroundUnderFeet(gl2);
	}
	
	
	// move the toes towards the toeTargets.
	private void gradientDescent() {
		double [] legAngles = new double[4];
		double EPSILON = 0.001;
		
		int k=0;
		for( Leg leg : legs ) {
			legAngles[0] = leg.shoulderA.theta;
			legAngles[1] = leg.shoulderB.theta;
			legAngles[2] = leg.elbow.theta;
			legAngles[3] = leg.foot.theta;
			
			double stepSize=10;
			for(int tries=0;tries<15;++tries) {
				int i;
				// work from toe to shoulder, seems to finish faster than shoulder to toe.
				for(i=legAngles.length-1;i>=0;--i) {
					if(partialDescent(leg,legAngles,i,stepSize,k)<EPSILON) break;
				}
				if(i>0) break;
				
				stepSize*=0.75;
			}
			k++;
			setLegToAngles(leg,legAngles);
		}
	}


	// wiggle leg joint 'i' to see which way gets a better score.
	private double partialDescent(Leg leg,double[] legAngles,int i,double stepSize,int k) {
		double startAngle = legAngles[i];
		double bestAngle = startAngle;
		double startScore = scoreLeg(leg,legAngles);
		double bestScore = startScore;

		legAngles[i] = startAngle-stepSize;
		double scoreNeg = scoreLeg(leg,legAngles);
		if(bestScore>scoreNeg) {
			bestScore = scoreNeg;
			bestAngle = legAngles[i];
		}
		
		legAngles[i] = startAngle+stepSize;
		double scorePos = scoreLeg(leg,legAngles);
		if(bestScore>scorePos) {
			bestScore = scorePos;
			bestAngle = legAngles[i];
		}
		
		legAngles[i] = bestAngle;
		return bestScore;
	}


	private double scoreLeg(Leg leg,double [] angles) {
		setLegToAngles(leg,angles);
		Vector3d fp = MatrixHelper.getPosition(getWorldMatrixOfToe(leg));
		fp.sub(leg.toeTarget);
		return fp.lengthSquared();
	}


	private void setLegToAngles(Leg leg, double[] angles) {
		leg.shoulderA.theta=angles[0];
		leg.shoulderB.theta=angles[1];
		leg.elbow.theta=angles[2];
		leg.foot.theta=angles[3];
		updateLegMatrixes(leg);
	}


	private void drawCurrentDogPose(GL2 gl2) {
		mat.render(gl2);
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);
		
		// torso
		double scale = 0.99;
		double width = (BODY_WIDTH/2)*scale;
		double height = (BODY_HEIGHT/2)*scale;
		double length = (BODY_LENGTH/2)*scale;
		PrimitiveSolids.drawBox(gl2, 
			new Point3d(-width,-length,-height),
			new Point3d( width, length, height));
		
		// legs
		boolean flag = OpenGLHelper.disableLightingStart(gl2);	
		for( Leg leg : legs ) drawLeg(gl2,leg);
		OpenGLHelper.disableLightingEnd(gl2, flag);

		gl2.glPopMatrix();
	}


	private void drawLeg(GL2 gl2, Leg leg) {
		gl2.glPushMatrix();
		drawLineTo(gl2,leg.shoulderA.pose,255,  0,  0);
		drawLineTo(gl2,leg.shoulderB.pose,  0,  0,  0);
		drawLineTo(gl2,leg.elbow.pose    ,  0,255,  0);
		drawLineTo(gl2,leg.foot.pose     ,  0,  0,255);
		gl2.glPopMatrix();
	}

	
	private void updateLegMatrixes(Leg leg) {
		leg.shoulderA.updateMatrix();
		leg.shoulderB.updateMatrix();
		leg.elbow.updateMatrix();
		leg.foot.updateMatrix();
	}

	
	@SuppressWarnings("unused")
	private void drawPointOnGroundUnderFeet(GL2 gl2) {
		for( Leg leg : legs ) {
			Vector3d fp = MatrixHelper.getPosition(getWorldMatrixOfToe(leg));
			
			gl2.glPushMatrix();
			gl2.glTranslated(fp.x,fp.y,0);
			PrimitiveSolids.drawSphere(gl2, 0.25);
			gl2.glPopMatrix();
		}
	}


	private Matrix4d getWorldMatrixOfToe(Leg leg) {
		Matrix4d m = new Matrix4d();
		m.set(getPose());
		m.mul(leg.shoulderA.pose);
		m.mul(leg.shoulderB.pose);
		m.mul(leg.elbow.pose);
		m.mul(leg.foot.pose);
		return m;
	}


	private void walk2() {
		double t = System.currentTimeMillis()*0.0025;
		
		for( Leg leg : legs ) {
			Vector3d fp = dropShoulderToFloor(leg);
			
			// find point in that circle that makes a good gait
			double as = Math.max(0,-Math.sin(t));
			double ac = Math.cos(t);
			t+=Math.PI*0.75;
			
			Vector3d forward = MatrixHelper.getZAxis(getPose());
			forward.scale(5*ac);
			forward.z+= as*3;
			// remember the point for later
			leg.toeTarget.add(forward,fp);
		}
	}

	
	@SuppressWarnings("unused")
	private void drawAllShoulderFloorCircles(GL2 gl2) {
		for( Leg leg : legs ) {
			Vector3d fp = dropShoulderToFloor(leg);
			drawShoulderFloorCircle(gl2,fp);
		}
	}

	private Vector3d dropShoulderToFloor(Leg leg) {
		Matrix4d m = new Matrix4d();
		m.set(getPose());
		m.mul(leg.shoulderA.pose);
		m.mul(leg.shoulderB.pose);
		Vector3d fp = MatrixHelper.getPosition(m);
		fp.z=0;

		return fp;
	}


	private void drawShoulderFloorCircle(GL2 gl2, Vector3d fp) {
		gl2.glPushMatrix();
		gl2.glTranslated(fp.x,fp.y,fp.z);
		PrimitiveSolids.drawCircleXY(gl2, 5, 20);
		gl2.glPopMatrix();
	}


	@SuppressWarnings("unused")
	private void drawToeTarget(GL2 gl2) {
		gl2.glColor3d(1, 0, 0);
		for( Leg leg : legs ) {
			gl2.glPushMatrix();
			gl2.glTranslated(leg.toeTarget.x, leg.toeTarget.y, leg.toeTarget.z);
			PrimitiveSolids.drawSphere(gl2, 0.5);
			gl2.glPopMatrix();
		}
	}


	@SuppressWarnings("unused")
	private void walk1() {
		setDHParameters();
		
		double t = System.currentTimeMillis()*0.0025;
		double as = Math.toDegrees(Math.sin(t));
		double ac = Math.toDegrees(Math.cos(t));

		legs[0].elbow.theta += as/4;
		legs[0].foot.theta += ac/4;
		updateLegMatrixes(legs[0]);

		t+=Math.PI/2;
		as = Math.toDegrees(Math.sin(t));
		ac = Math.toDegrees(Math.cos(t));
		legs[1].elbow.theta += as/4;
		legs[1].foot.theta += ac/4;
		updateLegMatrixes(legs[1]);

		t+=Math.PI/2;
		as = Math.toDegrees(Math.sin(t));
		ac = Math.toDegrees(Math.cos(t));
		legs[2].elbow.theta += as/4;
		legs[2].foot.theta += ac/4;
		updateLegMatrixes(legs[2]);

		t+=Math.PI/2;
		as = Math.toDegrees(Math.sin(t));
		ac = Math.toDegrees(Math.cos(t));
		legs[3].elbow.theta += as/4;
		legs[3].foot.theta += ac/4;
		updateLegMatrixes(legs[3]);
	}


	private void drawLineTo(GL2 gl2,Matrix4d m,double r,double g,double b) {
		Vector3d v = new Vector3d();
		m.get(v);
		gl2.glLineWidth(5);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(r,g,b);
		gl2.glVertex3d(0, 0, 0);
		gl2.glVertex3d(v.x,v.y,v.z);
		gl2.glEnd();
		gl2.glLineWidth(1);

		//MatrixHelper.drawMatrix(gl2,1);
		MatrixHelper.applyMatrix(gl2, m);
	}
}
