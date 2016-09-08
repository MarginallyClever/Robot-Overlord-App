package com.marginallyclever.robotOverlord.spidee;

import java.io.Serializable;

import com.jogamp.opengl.GL2;
import javax.vecmath.Vector3f;

public class SpideeLeg implements Serializable {
	  /**
	 * 
	 */
	private static final long serialVersionUID = -3631783587859663314L;
	String name;
	  int base_servo_address;
	  
	  SpideeJoint pan_joint = new SpideeJoint();
	  SpideeJoint tilt_joint = new SpideeJoint();
	  SpideeJoint knee_joint = new SpideeJoint();
	  SpideeJoint ankle_joint = new SpideeJoint();

	  Vector3f lpoc = new Vector3f();  // last point of contact on the ground
	  Vector3f npoc = new Vector3f();  // next point of contact on the ground

	  float facing_angle;   // angle relative to body in resting state

	  boolean active;
	  boolean on_ground;
	  
	  
	  void render(GL2 gl2,int color_index) {
		  float [] colors = {
		    1,0,0,
		    0,1,0,
		    0,0,1,
		    1,1,0,
		    0,1,1,
		    1,0,1
		  };

		  //*
		  gl2.glDisable(GL2.GL_LIGHTING);

		  gl2.glColor3f(colors[color_index*3],
				  colors[color_index*3+1],
				  colors[color_index*3+2]);
		  // last point of contact
		  gl2.glBegin(GL2.GL_LINE_LOOP);
		  gl2.glVertex3f(lpoc.x+0.5f, lpoc.y-0.5f, 0);
		  gl2.glVertex3f(lpoc.x+0.5f, lpoc.y+0.5f, 0);
		  gl2.glVertex3f(lpoc.x-0.5f, lpoc.y+0.5f, 0);
		  gl2.glVertex3f(lpoc.x-0.5f, lpoc.y-0.5f, 0);
		  gl2.glEnd();
		  gl2.glBegin(GL2.GL_LINES);
		  gl2.glVertex3f(npoc.x-1.0f, npoc.y, 0);
		  gl2.glVertex3f(npoc.x+1.0f, npoc.y, 0);
		  gl2.glVertex3f(npoc.x, npoc.y-1.0f, 0);
		  gl2.glVertex3f(npoc.x, npoc.y+1.0f, 0);
		  gl2.glEnd();

		  // next point of contact
		  gl2.glBegin(GL2.GL_LINE_LOOP);
		  gl2.glVertex3f(npoc.x+0.75f, npoc.y-0.75f, 0);
		  gl2.glVertex3f(npoc.x+0.75f, npoc.y+0.75f, 0);
		  gl2.glVertex3f(npoc.x-0.75f, npoc.y+0.75f, 0);
		  gl2.glVertex3f(npoc.x-0.75f, npoc.y-0.75f, 0);
		  gl2.glEnd();

		  gl2.glBegin(GL2.GL_LINES);
		  gl2.glVertex3f(ankle_joint.pos.x,ankle_joint.pos.y,ankle_joint.pos.z);
		  gl2.glVertex3f(ankle_joint.pos.x,ankle_joint.pos.y,0);
		  gl2.glEnd();

		  pan_joint.Draw(gl2,2);
		  tilt_joint.Draw(gl2,1);
		  knee_joint.Draw(gl2,3);

		  gl2.glEnable(GL2.GL_LIGHTING);
	  }
}
