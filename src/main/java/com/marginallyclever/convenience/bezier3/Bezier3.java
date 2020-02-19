package com.marginallyclever.convenience.bezier3;

import javax.vecmath.Vector3f;

/**
 * 3D Bezier curve implementation.  All points are in the same space - p1 and p2 are not relative to p0 and p3, respectively.
 * @author Dan Royer
 * See https://en.wikipedia.org/wiki/B%C3%A9zier_curve
 *
 */
public class Bezier3 {
	public Vector3f p0 = new Vector3f();
	public Vector3f p1 = new Vector3f();
	public Vector3f p2 = new Vector3f();
	public Vector3f p3 = new Vector3f();
	
	
	/**
	 * interpolate along the path.  This code is slow, intuitive, and it works.
	 * @param i 0...1 inclusive
	 * @return the interpolated value along the path.
	 */
	public Vector3f interpolate(float i) {
		Vector3f pa = interpolate(p0,p1,i);
		Vector3f pb = interpolate(p1,p2,i);
		Vector3f pc = interpolate(p2,p3,i);
		
		Vector3f pab = interpolate(pa,pb,i);
		Vector3f pbc = interpolate(pb,pc,i);

		Vector3f result = interpolate(pab,pbc,i);
		return result;
	}
	
	/**
	 * interpolate between two Vector3f
	 * @param a
	 * @param b
	 * @param i
	 * @return
	 */
	protected Vector3f interpolate(Vector3f a,Vector3f b,float i) {
		Vector3f c = new Vector3f(b);
		c.sub(a);
		c.scale(i);
		c.add(a);
		return c;
	}
}
