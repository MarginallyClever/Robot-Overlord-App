package com.marginallyclever.convenience.bezier3;

import javax.vecmath.Vector3d;

/**
 * 3D Bezier curve implementation.  All points are in the same space - p1 and p2 are not relative to p0 and p3, respectively.
 * See <a href="https://en.wikipedia.org/wiki/B%C3%A9zier_curve">Wikipedia</a>
 * @author Dan Royer
 */
public class Bezier3 {
	public Vector3d p0 = new Vector3d();
	public Vector3d p1 = new Vector3d();
	public Vector3d p2 = new Vector3d();
	public Vector3d p3 = new Vector3d();
	
	public Bezier3() {
		super();
	}
	
	/**
	 * interpolate along the path.  This code is slow, intuitive, and it works.
	 * @param i 0...1 inclusive
	 * @return the interpolated {@link Vector3d} along the path.
	 */
	public Vector3d interpolate(double i) {
		Vector3d pa = interpolate(p0,p1,i);
		Vector3d pb = interpolate(p1,p2,i);
		Vector3d pc = interpolate(p2,p3,i);
		
		Vector3d pab = interpolate(pa,pb,i);
		Vector3d pbc = interpolate(pb,pc,i);

		Vector3d result = interpolate(pab,pbc,i);
		return result;
	}
	
	/**
	 * Interpolate between two {@link Vector3d} points.
	 * @param a from here
	 * @param b to here
	 * @param i a value from 0...1, inclusive.
	 * @return the interpolated {@link Vector3d} between a and b.
	 */
	private Vector3d interpolate(Vector3d a,Vector3d b,double i) {
		Vector3d c = new Vector3d(b);
		c.sub(a);
		c.scale(i);
		c.add(a);
		return c;
	}
}
