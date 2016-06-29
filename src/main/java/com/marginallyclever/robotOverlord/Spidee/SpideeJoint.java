package com.marginallyclever.robotOverlord.Spidee;

import java.io.Serializable;

import com.jogamp.opengl.GL2;

public class SpideeJoint extends SpideeLocation implements Serializable {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 5055658323708862756L;

	public static final int ANGLE_HISTORY_LENGTH = 30*3;
	
	  float angle;
	  int last_angle;
	  int servo_address;

	  // thresholds so we don't grind & damage servos.
	  int angle_max;
	  int angle_min;
	  // adjust for real world inaccuracies
	  float scale;
	  float zero;

	  float [] angle_history = new float[ANGLE_HISTORY_LENGTH];
	  
	  void Draw(GL2 gl2,float scale) {

		  gl2.glPushMatrix();
		  gl2.glTranslatef(pos.x,pos.y,pos.z);

		  // axies
		  gl2.glPushMatrix();
		  gl2.glScalef(scale,scale,scale);
		  gl2.glBegin(GL2.GL_LINES);
		  gl2.glColor3f(1,0,0);  gl2.glVertex3f(0,0,0);  gl2.glVertex3f(forward.x,forward.y,forward.z);
		  gl2.glColor3f(0,1,0);  gl2.glVertex3f(0,0,0);  gl2.glVertex3f(up     .x,up     .y,up     .z);
		  gl2.glColor3f(0,0,1);  gl2.glVertex3f(0,0,0);  gl2.glVertex3f(left   .x,left   .y,left   .z);
		  gl2.glEnd();
		  gl2.glPopMatrix();
		  
		  // planar arcs
		  gl2.glBegin(GL2.GL_LINE_STRIP);
		  gl2.glColor3f(1,0,1);
		  int k;
		  for(k=0;k<=16;++k) {
		    float js=(float)Math.sin(k*Math.PI/16f) * scale;
		    float jc=(float)Math.cos(k*Math.PI/16f) * scale;
		    gl2.glVertex3f( left.x * jc + forward.x * js,
				    		left.y * jc + forward.y * js,
				    		left.z * jc + forward.z * js);
		  }
		  gl2.glEnd();

		  gl2.glBegin(GL2.GL_LINE_STRIP);
		  gl2.glColor3f(1,1,0);  
		  for(k=0;k<=16;++k) {
		    float js=(float)Math.sin(k*Math.PI/16.0f) * scale;
		    float jc=(float)Math.cos(k*Math.PI/16.0f) * scale;
		    gl2.glVertex3f( up.x * jc + forward.x * js,
				    		up.y * jc + forward.y * js,
				    		up.z * jc + forward.z * js);
		  }
		  gl2.glEnd();

		  gl2.glPopMatrix();
	  }
}
