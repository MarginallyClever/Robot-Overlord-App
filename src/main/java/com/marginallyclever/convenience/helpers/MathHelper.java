package com.marginallyclever.convenience.helpers;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * Math methods.
 *
 */
public class MathHelper {
	public final static double EPSILON = 0.00001f;
	public final static double TWOPI = Math.PI*2;	

	/**
	 * greatest common divider
	 * @param a
	 * @param b
	 * @return greatest common divider
	 */
	static public long gcd(long a, long b) {
		long temp;
	    while (b > 0) {
	        temp = b;
	        b = a % b; // % is remainder
	        a = temp;
	    }
	    return a;
	}
	
	/**
	 * least common multiplier
	 * @param a
	 * @param b
	 * @return least common multiplier
	 */
	static public long lcm(long a, long b) {
	    return a * (b / gcd(a, b));
	}

	
	/**
	 * interpolate from a to b
	 * @param a
	 * @param b
	 * @param t [0...1]
	 * @return a + (b-a)*t
	 */
	static public float interpolate(float a,float b,double t) {
		return (b-a)*(float)t + a;
	}

	/**
	 * interpolate from a to b
	 * @param a
	 * @param b
	 * @param t [0...1]
	 * @return a + (b-a)*t
	 */
	static public double interpolate(double a,double b,double t) {
		return (b-a)*t + a;
	}

	
	/**
	 * interpolate from start to end
	 * @param start
	 * @param end
	 * @param t [0...1]
	 * @return
	 */
	static public Vector3d interpolate(Vector3d start,Vector3d end,double t) {
		Vector3d n = new Vector3d(end);
		n.sub(start);
		n.scale((float)t);
		n.add(start);
		
		return n;
	}

	/**
	 * interpolate from start to end
	 * @param start
	 * @param end
	 * @param t [0...1]
	 * @return
	 */
	static public Point3d interpolate(Point3d start, Point3d end, double t) {
		Point3d n = new Point3d(end);
		n.sub(start);
		n.scale((float)t);
		n.add(start);

		return n;
	}

	/**
	 * <a href="https://en.wikipedia.org/wiki/Slerp">Spherical linear interpolation</a> between two vectors.
	 *
	 * @param a start vector
	 * @param b end vector
	 * @param t [0...1]
	 * @return interpolated vector
	 */
	static public Vector3d slerp(Vector3d a,Vector3d b,double t) {
		
		// Dot product - the cosine of the angle between 2 vectors.
		double dot = a.dot(b);
		//dot = Math.min(Math.max(dot, -1.0f), 1.0f);
		if(dot<0) {
			b.scale(-1);
			dot = -dot;
		}
		if(Math.abs(dot)>=1-MathHelper.EPSILON) {
			Vector3d n = new Vector3d(b);
			n.sub(a);
			n.scale(t);
			n.add(a);
			n.normalize();
			
			return n;
		}
		
		// Acos(dot) returns the angle between start and end,
		// And multiplying that by percent returns the angle between
		// start and the final result.
		double theta_0 = Math.acos(dot);
		double theta = theta_0*t;
		double sin_theta = Math.sin(theta);
		double sin_theta_0 = Math.sin(theta_0);
		double s0 = Math.cos(theta) - dot * sin_theta / sin_theta_0;
		double s1 = sin_theta / sin_theta_0;
		
		//Vector3d n b - a*dot;
		a.scale(s0);
		b.scale(s1);
		Vector3d n = new Vector3d(a);
		n.add(b);
		n.normalize();
		
		return n;
	}
	
	/**
	 * Convert quaternion 'q' to euler radian angles roll, pitch, yaw
	 * @param q
	 * @return
	 */
	static public double [] quatToEuler(Quat4d q) {
		double roll, pitch, yaw;
		
	    // roll (x-axis rotation)
	    double sinr_cosp = 2 * (q.w * q.x + q.y * q.z);
	    double cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y);
	    roll = Math.atan2(sinr_cosp, cosr_cosp);

	    // pitch (y-axis rotation)
	    double sinp = 2 * (q.w * q.y - q.z * q.x);
	    if (Math.abs(sinp) >= 1)
	        pitch = Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
	    else
	        pitch = Math.asin(sinp);

	    // yaw (z-axis rotation)
	    double siny_cosp = 2 * (q.w * q.z + q.x * q.y);
	    double cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z);
	    yaw = Math.atan2(siny_cosp, cosy_cosp);

	    return new double[] { roll, pitch, yaw };
	}

	/**
	 * compare two vectors
	 * @param a	the first vector
	 * @param b the second vector
	 * @param v tolerance
	 * @return true if the absolute difference on every axis is less than v
	 */
    public static boolean equals(Vector3d a, Vector3d b, double v) {
		return ( Math.abs(a.x - b.x) <= v) &&
		       ( Math.abs(a.y - b.y) <= v) &&
			   ( Math.abs(a.z - b.z) <= v);
	}
}
