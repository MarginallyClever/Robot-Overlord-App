package com.marginallyclever.robotOverlord.robots.dog;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3Bone;

public class DogLeg {
	public Sixi3Bone shoulderA = new Sixi3Bone();
	public Sixi3Bone shoulderB = new Sixi3Bone();
	public Sixi3Bone elbow = new Sixi3Bone();
	public Sixi3Bone foot = new Sixi3Bone();
	
	// actual toe location
	public Vector3d toe = new Vector3d();
	// desired toe location
	public Vector3d toeTarget = new Vector3d();
	// desired toe location, second order
	public Vector3d toeTarget2 = new Vector3d();
	
	public double [] idealStandingAngles = new double[4];
	
	public DogLeg() {}

	public void captureAngles(double[] legAngles) {
		legAngles[0]=shoulderA.theta;
		legAngles[1]=shoulderB.theta;
		legAngles[2]=elbow.theta;
		legAngles[3]=foot.theta;
	}
	
	public void render(GL2 gl2) {
		gl2.glPushMatrix();		
		drawLineTo(gl2,shoulderA.pose,255,  0,  0);
		drawLineTo(gl2,shoulderB.pose,  0,  0,  0);
		drawLineTo(gl2,elbow.pose    ,  0,255,  0);
		drawLineTo(gl2,foot.pose     ,  0,  0,255);
		gl2.glPopMatrix();
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
		
		MatrixHelper.applyMatrix(gl2,m);
	}
}