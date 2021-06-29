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

/**
 * Spot Micro simulation.  Robot faces +
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class SpotMicro extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5916361555293772951L;

	public class Leg {
		public Sixi3Bone shoulderA = new Sixi3Bone();
		public Sixi3Bone shoulderB = new Sixi3Bone();
		public Sixi3Bone elbow = new Sixi3Bone();
		public Sixi3Bone foot = new Sixi3Bone();
		
		public Leg() {}
	}
	Leg [] legs = new Leg[4];
	
	
	public SpotMicro() {
		super();
		setName("Spot Micro");
		
		for(int i=0;i<4;++i) {
			legs[i] = new Leg();
		}
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	@Override
	public void render(GL2 gl2) {
		float BODY_WIDTH=12;
		float BODY_LENGTH=8;
		float BODY_HEIGHT=32;
		
		super.render(gl2);
		
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
		
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);
		PrimitiveSolids.drawBox(gl2, 
				new Point3d(-BODY_WIDTH/2,-BODY_LENGTH/2,-BODY_HEIGHT/2),
				new Point3d( BODY_WIDTH/2, BODY_LENGTH/2, BODY_HEIGHT/2));
		boolean flag = OpenGLHelper.disableLightingStart(gl2);
		//int wasAtop = OpenGLHelper.drawAtopEverythingStart(gl2);
		for( Leg leg : legs ) {
			leg.shoulderA.updateMatrix();
			leg.shoulderB.updateMatrix();
			leg.elbow.updateMatrix();
			leg.foot.updateMatrix();

			gl2.glPushMatrix();
			drawLineTo(gl2,leg.shoulderA.pose,255,  0,  0);
			drawLineTo(gl2,leg.shoulderB.pose,  0,  0,  0);
			drawLineTo(gl2,leg.elbow.pose    ,  0,255,  0);
			drawLineTo(gl2,leg.foot.pose     ,  0,  0,255);
			gl2.glPopMatrix();
		}
		//OpenGLHelper.drawAtopEverythingEnd(gl2, wasAtop);
		OpenGLHelper.disableLightingEnd(gl2, flag);
		gl2.glPopMatrix();
	}
	
	private void drawLineTo(GL2 gl2,Matrix4d m,double r,double g,double b) {
		Vector3d v = new Vector3d();
		m.get(v);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(r,g,b);
		gl2.glVertex3d(0, 0, 0);
		gl2.glVertex3d(v.x,v.y,v.z);
		gl2.glEnd();

		MatrixHelper.drawMatrix(gl2,1);
		MatrixHelper.applyMatrix(gl2, m);
	}
}
