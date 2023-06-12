package com.marginallyclever.convenience;

import com.jogamp.opengl.GL3;

import javax.vecmath.Vector3d;

/**
 * A cylinder with a radius and two end points.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class Cylinder implements BoundingVolume {
	private final Vector3d p1 = new Vector3d(0,0,0);
	private final Vector3d p2 = new Vector3d(0,0,0);
	private final Vector3d n = new Vector3d(0,0,0);
	private final Vector3d f = new Vector3d(0,0,0);
	private final Vector3d r = new Vector3d(0,0,0);
	private float radius=1.0f;
	
	public void SetP1(Vector3d src) {
		p1.set(src);
		UpdateVectors();
	}
	public void SetP2(Vector3d src) {
		p2.set(src);
		UpdateVectors();
	}
	
	public void setRadius(float r) {
		radius = r;
	}
	public float getRadius() {
		return radius;
	}
	
	public Vector3d GetP1() {
		return p1;
	}
	
	public Vector3d GetP2() {
		return p2;
	}
	public Vector3d GetN() {
		return n;
	}
	public Vector3d GetF() {
		return f;
	}
	public Vector3d GetR() {
		return r;
	}
	
	public void UpdateVectors() {
		n.set(p2);
		n.sub(p1);
		n.normalize();
		
		if(n.x > n.y) {
			if(n.x > n.z) {
				// x major
				f.z=n.z;
				f.y=n.x;
				f.x=n.y;
			} else {
				// z major
				f.z=n.y;
				f.y=n.z;
				f.x=n.x;
			}
		} else {
			if(n.y > n.z) {
				// y major
				f.z=n.z;
				f.y=n.x;
				f.x=n.y;
			} else {
				// z major
				f.z=n.y;
				f.y=n.z;
				f.x=n.x;
			}
		}
		r.cross(f, n);
		r.normalize();
		f.cross(n, r);
		f.normalize();
	}
}
