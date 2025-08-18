package com.marginallyclever.convenience;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * {@link Ray} is a starting point and a direction.
 */
public class Ray {
	private final Point3d origin;
	private final Vector3d direction;
	private final double maxDistance;
	private final Vector3d inverseDirection = new Vector3d();

	public Ray() {
		this(new Point3d(),new Vector3d(0,0,1),Double.MAX_VALUE);
	}

	/**
	 * Calls {@link Ray#Ray(Point3d, Vector3d, double)} with the maximum ray distance.
	 */
	public Ray(Point3d origin, Vector3d direction) {
		this(origin,direction,Double.MAX_VALUE);
	}

	/**
	 *
	 * @param origin the camera position.
	 * @param direction unit length venctor from camera through viewport and into scene.
	 * @param maxDistance the limit to test for ray intersections.
	 */
	public Ray(Point3d origin, Vector3d direction, double maxDistance) {
		this.origin = new Point3d(origin);
		this.direction = new Vector3d(direction);
		this.maxDistance = maxDistance;
		updateInverseDirection();
	}

	private void updateInverseDirection() {
		double ix = direction.x ==0 ? 0 : 1.0/direction.x;
		double iy = direction.y ==0 ? 0 : 1.0/direction.y;
		double iz = direction.z ==0 ? 0 : 1.0/direction.z;
		inverseDirection.set(ix,iy,iz);
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

	public Point3d getOrigin() {
		return origin;
	}

	public Vector3d getDirection() {
		return direction;
	}

	public double getMaxDistance() {
		return maxDistance;
	}

	/**
	 * Set this ray to be a copy of another ray.  this = matrix.transform(from)
	 * @param matrix the local transform
	 * @param from the ray to copy
	 */
    public void transform(Matrix4d matrix,Ray from) {
		this.origin.set(from.origin);
		this.direction.set(from.direction);
		matrix.transform(origin);
		matrix.transform(direction);
		updateInverseDirection();
    }

	public Vector3d getInverseDirection() {
		return inverseDirection;
	}
}
