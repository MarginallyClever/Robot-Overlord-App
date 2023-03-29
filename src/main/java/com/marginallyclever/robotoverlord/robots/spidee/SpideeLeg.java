package com.marginallyclever.robotoverlord.robots.spidee;

import com.jogamp.opengl.GL2;

import javax.vecmath.Vector3d;

@Deprecated
public class SpideeLeg  {
	String name;
	  int base_servo_address;
	  
	  SpideeJoint shoulderPan = new SpideeJoint();
	  SpideeJoint shoulderTilt = new SpideeJoint();
	  SpideeJoint knee = new SpideeJoint();
	  SpideeJoint ankle = new SpideeJoint();

	  Vector3d lastPointOfContact = new Vector3d();  // last point of contact on the ground
	  Vector3d nextPointOfContact = new Vector3d();  // next point of contact on the ground

	  float facingAngle;   // angle relative to body in resting state

	  boolean active;
	  boolean onGround;
	  
	  
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
		  gl2.glVertex3d(lastPointOfContact.x+0.5f, lastPointOfContact.y-0.5f, 0);
		  gl2.glVertex3d(lastPointOfContact.x+0.5f, lastPointOfContact.y+0.5f, 0);
		  gl2.glVertex3d(lastPointOfContact.x-0.5f, lastPointOfContact.y+0.5f, 0);
		  gl2.glVertex3d(lastPointOfContact.x-0.5f, lastPointOfContact.y-0.5f, 0);
		  gl2.glEnd();
		  gl2.glBegin(GL2.GL_LINES);
		  gl2.glVertex3d(nextPointOfContact.x-1.0f, nextPointOfContact.y, 0);
		  gl2.glVertex3d(nextPointOfContact.x+1.0f, nextPointOfContact.y, 0);
		  gl2.glVertex3d(nextPointOfContact.x, nextPointOfContact.y-1.0f, 0);
		  gl2.glVertex3d(nextPointOfContact.x, nextPointOfContact.y+1.0f, 0);
		  gl2.glEnd();

		  // next point of contact
		  gl2.glBegin(GL2.GL_LINE_LOOP);
		  gl2.glVertex3d(nextPointOfContact.x+0.75f, nextPointOfContact.y-0.75f, 0);
		  gl2.glVertex3d(nextPointOfContact.x+0.75f, nextPointOfContact.y+0.75f, 0);
		  gl2.glVertex3d(nextPointOfContact.x-0.75f, nextPointOfContact.y+0.75f, 0);
		  gl2.glVertex3d(nextPointOfContact.x-0.75f, nextPointOfContact.y-0.75f, 0);
		  gl2.glEnd();

		  gl2.glBegin(GL2.GL_LINES);
		  gl2.glVertex3d(ankle.pos.x, ankle.pos.y, ankle.pos.z);
		  gl2.glVertex3d(ankle.pos.x, ankle.pos.y,0);
		  gl2.glEnd();

		  shoulderPan.draw(gl2,2);
		  shoulderTilt.draw(gl2,1);
		  knee.draw(gl2,3);

		  gl2.glEnable(GL2.GL_LIGHTING);
	  }
}
