package com.marginallyclever.convenience;

import com.jogamp.opengl.GL3;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * Plane and intersection functions.  A plane is defined by a point and a normal.
 * @author Dan Royer
 *
 */
public class Plane {
	public double distance;
	public Vector3d normal = new Vector3d();

	public Plane() {
		super();
		normal.set(0,0,1);
	}

	// build a plane from a point and a normal
	public Plane(Tuple3d point, Vector3d normal) {
		super();
		this.normal.set(normal);
		distance = distanceAlongNormal(point);
	}
	
	// build a plane from three points.  normal is cross product of p1-p0 and p2-p0
	public Plane(Point3d p0,Point3d p1,Point3d p2) {
		super();
		Vector3d v1 = new Vector3d();
		v1.sub(p1,p0);
		Vector3d v2 = new Vector3d();
		v2.sub(p2,p0);
		normal.cross(v1, v2);
		normal.normalize();
		distance = distanceAlongNormal(p0);
	}

	public Plane(Vector3d normal,double distance) {
		super();
		this.normal.set(normal);
		this.distance = distance;
	}
	
	/**
	 * @return distance to plane from origin.  effectively normal.dot(start)
	 */
	private double distanceAlongNormal(Tuple3d point) {
		return point.x * normal.x
			 + point.y * normal.y
			 + point.z * normal.z;
	}

	/**
	 * @param point the point to test
	 * @param epsilon how close to the plane is close enough
	 * @return true if the point is on the plane
	 */
	public boolean intersects(Point3d point, double epsilon) {
		return Math.abs(distanceAlongNormal(point) - distance) < epsilon;
	}

	/**
	 * @param point the point to test
	 * @return true if the point is within 0.0001 of the plane.
	 */
	public boolean intersects(Point3d point) {
		return intersects(point,0.0001);
	}

	/**
	 * finds the intersection of a ray and this plane.
	 * @param ray the ray to intersect.
	 * @return the distance along the ray to the intersection point.  NaN if no intersection.
	 */
	public double intersectDistance(Ray ray) {
		// if ray is orthogonal to the plane, no hit possible.
		double d2 = ray.getDirection().dot(normal);
		if(Math.abs(d2) < 0.0001) {
			return Double.NaN;
		}

		return (distance - distanceAlongNormal(ray.getOrigin())) / d2;
	}

	public boolean intersect(Ray r, Point3d intersection) {
		double d = intersectDistance(r);
		if(Double.isNaN(d)) return false;
		intersection.scaleAdd(d, r.getDirection(), r.getOrigin());
		return true;
	}

	/**
	 * set this plane to the same values as arg0
	 * @param arg0
	 */
	public void set(Plane arg0) {
		distance = arg0.distance;
		normal.set(arg0.normal);
	}

    public Vector3d getNormal() {
		return normal;
    }

	public double getDistance() {
		return distance;
	}

	public Tuple3d getPoint() {
		return new Point3d(normal.x*distance,normal.y*distance,normal.z*distance);
	}
}
