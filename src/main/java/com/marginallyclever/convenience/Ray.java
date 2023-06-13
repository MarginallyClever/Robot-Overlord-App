package com.marginallyclever.convenience;

import com.jogamp.opengl.GL3;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * 
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Ray {
	private final Point3d origin = new Point3d();
	private final Vector3d direction = new Vector3d();
	private double maxDistance;

	public Ray() {
		this(new Point3d(),new Vector3d(0,0,1),Double.MAX_VALUE);
	}

	public Ray(Point3d origin,Vector3d direction) {
		this(origin,direction,Double.MAX_VALUE);
	}

	public Ray(Point3d origin,Vector3d direction,double maxDistance) {
		this.origin.set(origin);
		this.direction.set(direction);
		this.maxDistance = maxDistance;
	}

	public Ray(Ray r) {
		this.origin.set(r.origin);
		this.direction.set(r.direction);
		this.maxDistance = r.maxDistance;
	}

	/**
	 * @param direction the direction of this ray.  cannot be a zero vector.
	 * @throws IllegalArgumentException if direction is too small
	 */
	public void setDirection(Vector3d direction) throws IllegalArgumentException {
		if(direction.lengthSquared()<0.0001) {
			throw new IllegalArgumentException("direction is too small");
		}
		this.direction.set(direction);
		this.direction.normalize();
	}

	public Vector3d getDirection() {
		return direction;
	}

	public void setOrigin(Point3d origin) {
		this.origin.set(origin);
	}

	public Point3d getOrigin() {
		return origin;
	}

	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
	}

	public double getMaxDistance() {
		return maxDistance;
	}

	/**
	 * @return start + direction * t
	 */
	public Vector3d getPoint(double t) {
		return new Vector3d(
				origin.x+direction.x*t,
				origin.y+direction.y*t,
				origin.z+direction.z*t);
	}
}
