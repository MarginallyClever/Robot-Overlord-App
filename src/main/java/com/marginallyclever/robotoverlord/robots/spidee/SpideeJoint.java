package com.marginallyclever.robotoverlord.robots.spidee;


import com.jogamp.opengl.GL2;

@Deprecated
public class SpideeJoint extends SpideeLocation {
	public static final int ANGLE_HISTORY_LENGTH = (30*3);

	double angle;
	int lastAngle;
	int servoAddress;

	// thresholds so we don't grind & damage servos.
	int angleMax;
	int angleMin;
	// adjust for real world inaccuracies
	double scale;
	double zero;

	double [] angleHistory = new double[ANGLE_HISTORY_LENGTH];

	void draw(GL2 gl2, double scale) {

		gl2.glPushMatrix();
		gl2.glTranslated(pos.x,pos.y,pos.z);

		// axies
		gl2.glPushMatrix();
		gl2.glScaled(scale,scale,scale);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(1,0,0);  gl2.glVertex3d(0,0,0);  gl2.glVertex3d(forward.x,forward.y,forward.z);
		gl2.glColor3f(0,1,0);  gl2.glVertex3d(0,0,0);  gl2.glVertex3d(up     .x,up     .y,up     .z);
		gl2.glColor3f(0,0,1);  gl2.glVertex3d(0,0,0);  gl2.glVertex3d(left   .x,left   .y,left   .z);
		gl2.glEnd();
		gl2.glPopMatrix();

		// planar arcs
		gl2.glBegin(GL2.GL_LINE_STRIP);
		gl2.glColor3f(1,0,1);
		int k;
		for(k=0;k<=16;++k) {
			double js=Math.sin(k*Math.PI/16f) * scale;
			double jc=Math.cos(k*Math.PI/16f) * scale;
			gl2.glVertex3d( left.x * jc + forward.x * js,
					left.y * jc + forward.y * js,
					left.z * jc + forward.z * js);
		}
		gl2.glEnd();

		gl2.glBegin(GL2.GL_LINE_STRIP);
		gl2.glColor3f(1,1,0);  
		for(k=0;k<=16;++k) {
			double js=Math.sin(k*Math.PI/16.0f) * scale;
			double jc=Math.cos(k*Math.PI/16.0f) * scale;
			gl2.glVertex3d( up.x * jc + forward.x * js,
					up.y * jc + forward.y * js,
					up.z * jc + forward.z * js);
		}
		gl2.glEnd();

		gl2.glPopMatrix();
	}

	public void set(SpideeJoint panJoint) {
		forward.set(panJoint.forward);
		up.set(panJoint.up);
		left.set(panJoint.left);
		angle = panJoint.angle;
		lastAngle = panJoint.lastAngle;
	}
}
