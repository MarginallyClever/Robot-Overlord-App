package com.marginallyclever.robotOverlord.lines;

import javax.vecmath.Vector3f;

/**
 * 3D Bezier curve implementation
 * @author Dan Royer
 * @see https://en.wikipedia.org/wiki/B%C3%A9zier_curve
 *
 */
public class LineBezier {
	public Vector3f p0,p1,p2,p3;
	
	
	/**
	 * interpolate along the path
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
		Vector3f c = new Vector3f(a);
		c.sub(b);
		c.scale(i);
		c.add(b);
		return c;
	}
}
