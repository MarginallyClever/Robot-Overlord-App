package com.marginallyclever.convenience;

import com.jogamp.opengl.GL2;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * 
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Ray {
	public Point3d start = new Point3d();
	public Vector3d direction = new Vector3d();
	
	public void render(GL2 gl2) {
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(1,0,0); 
		gl2.glVertex3d(start.x, start.y, start.z);
		gl2.glColor3d(0,1,0);
		gl2.glVertex3d(
				start.x+direction.x*50,
				start.y+direction.y*50, 
				start.z+direction.z*50);
	}
	
	/**
	 * @return start + direction * t
	 */
	public Vector3d getPoint(double t) {
		return new Vector3d(
				start.x+direction.x*t,
				start.y+direction.y*t,
				start.z+direction.z*t);
	}
}
