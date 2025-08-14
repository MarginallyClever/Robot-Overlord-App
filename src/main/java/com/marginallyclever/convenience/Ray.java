package com.marginallyclever.convenience;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * {@link Ray} is a starting point and a direction.
 */
public record Ray(Point3d origin, Vector3d direction,double maxDistance) {
	public Ray() {
		this(new Point3d(),new Vector3d(0,0,1),Double.MAX_VALUE);
	}

	public Ray(Point3d origin, Vector3d direction) {
		this(origin,direction,Double.MAX_VALUE);
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
    }
}
