package com.marginallyclever.convenience;

import com.jogamp.opengl.GL2;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * 
 * @author Dan Royer
 *
 */
public class Plane {
	public Point3d start = new Point3d();
	public Vector3d normal = new Vector3d();
	
	// build a plane from a point and a normal
	public Plane(Point3d p,Vector3d n) {
		start.set(p);
		normal.set(n);
	}
	
	// build a plane from three points.  normal is cross product of p1-p0 and p2-p0
	public Plane(Point3d p0,Point3d p1,Point3d p2) {
		Vector3d v1 = new Vector3d();
		v1.sub(p1,p0);
		Vector3d v2 = new Vector3d();
		v2.sub(p2,p0);
		normal.cross(v1, v2);
		normal.normalize();
		start.set(p0);
	}
	
	public void render(GL2 gl2) {
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(1,0,0); 
		gl2.glVertex3d(start.x, start.y, start.z);
		gl2.glColor3d(0,1,0);
		gl2.glVertex3d(
				start.x+normal.x,
				start.y+normal.y, 
				start.z+normal.z);
	}
	
	/**
	 * @return distance to plane from origin.  effectively normal.dot(start)
	 */
	public double distanceToPlane() {
		double d = start.x * normal.x
				 + start.y * normal.y
				 + start.z * normal.z;
		return d;
	}
}
