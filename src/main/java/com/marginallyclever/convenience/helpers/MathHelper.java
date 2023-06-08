package com.marginallyclever.convenience.helpers;

import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * Math methods.
 * @author Dan Royer
 *
 */
public class MathHelper {
	public final static double EPSILON = 0.00001f;
	public final static double TWOPI = Math.PI*2;	

	/**
	 * @param dx x component
	 * @param dy y component
	 * @param dz z component
	 * @return Square of length of vector (dx,dy,dz) 
	 */
	@Deprecated
	public static double lengthSquared(double dx,double dy,double dz) {
		return dx*dx+dy*dy+dz*dz;
	}
	
	
	/**
	 * @param dx x component
	 * @param dy y component
	 * @param dz z component
	 * @return Length of vector (dx,dy,dz) 
	 */
	@Deprecated
	public static double length(double dx,double dy,double dz) {
		return (float)Math.sqrt(lengthSquared(dx,dy,dz));
	}
	

	/**
	 * @param dx x component
	 * @param dy y component
	 * @return Square of length of vector (dx,dy) 
	 */
	@Deprecated
	public static double lengthSquared(double dx,double dy) {
		return dx*dx+dy*dy;
	}
	
	
	/**
	 * @param dx x component
	 * @param dy y component
	 * @return Length of vector (dx,dy) 
	 */
	@Deprecated
	public static double length(double dx,double dy) {
		return (float)Math.sqrt(lengthSquared(dx,dy));
	}


	/**
	 * Round a double off to 3 decimal places.
	 * @param v a value
	 * @return Value rounded off to 3 decimal places
	 */
	@Deprecated
	public static double roundOff3(double v) {
		float SCALE = 1000.0f;

		return Math.round(v*SCALE)/SCALE;
	}


	/**
	 * Rotate the point xyz around the line passing through abc with direction uvw
	 * http://inside.mines.edu/~gmurray/ArbitraryAxisRotation/ArbitraryAxisRotation.html
	 * Special case where abc=0
	 * @param vec the vector to rotate
	 * @param axis the axis around which to rotate
	 * @param radians the angle in radians to rotate
	 * @return the new vector
	 */
	@Deprecated
	static public Vector3d rotateAroundAxis(Vector3d vec,Vector3d axis,double radians) {
		double C = Math.cos(radians);
		double S = Math.sin(radians);
		double x = vec.x;
		double y = vec.y;
		double z = vec.z;
		double u = axis.x;
		double v = axis.y;
		double w = axis.z;
		
		// (a*( v*v + w*w) - u*(b*v + c*w - u*x - v*y - w*z))(1.0-C)+x*C+(-c*v + b*w - w*y + v*z)*S
		// (b*( u*u + w*w) - v*(a*v + c*w - u*x - v*y - w*z))(1.0-C)+y*C+( c*u - a*w + w*x - u*z)*S
		// (c*( u*u + v*v) - w*(a*v + b*v - u*x - v*y - w*z))(1.0-C)+z*C+(-b*u + a*v - v*x + u*y)*S
		// but a=b=c=0 so
		// x' = ( -u*(- u*x - v*y - w*z)) * (1.0-C) + x*C + ( - w*y + v*z)*S
		// y' = ( -v*(- u*x - v*y - w*z)) * (1.0-C) + y*C + ( + w*x - u*z)*S
		// z' = ( -w*(- u*x - v*y - w*z)) * (1.0-C) + z*C + ( - v*x + u*y)*S
		
		double a = (-u*x - v*y - w*z);

		return new Vector3d( (-u*a) * (1.0f-C) + x*C + ( -w*y + v*z)*S,
							 (-v*a) * (1.0f-C) + y*C + (  w*x - u*z)*S,
							 (-w*a) * (1.0f-C) + z*C + ( -v*x + u*y)*S);
	}
	
	/**
	 * Same as capRotationDegrees(double arg0,0)
	 * @param arg0
	 * @return adjusted value
	 */
	@Deprecated
	static public double wrapRadians(double arg0) {
		return wrapRadians(arg0,0);
	}

	/**
	 * Prevent angle arg0 from leaving the range centerPoint-PI...centerPoint+PI.  outside that range it wraps, like a modulus.
	 * @param arg0
	 * @return adjusted value
	 */
	@Deprecated
	static public double wrapRadians(double arg0,double centerPoint) {
		arg0 -= centerPoint-Math.PI;
		arg0 = ((arg0 % TWOPI) + TWOPI ) % TWOPI;
		arg0 += centerPoint-Math.PI;
		return arg0;
	}
	
	/**
	 * Same as capRotationDegrees(double arg0,0)
	 * @param arg0
	 * @return adjusted value
	 */
	@Deprecated
	static public double wrapDegrees(double arg0) {
		return wrapDegrees(arg0,0);
	}

	/**
	 * Prevent angle arg0 from leaving the range centerPoint-180...centerPoint+180.  outside that range it wraps, like a modulus.
	 * @param arg0
	 * @return adjusted value
	 */
	@Deprecated
	static public double wrapDegrees(double arg0,double centerPoint) {
		arg0 -= centerPoint-180;
		arg0 = ((arg0 % 360) + 360 ) % 360;
		arg0 += centerPoint-180;
		return arg0;
	}
	
	/**
	 * greatest common divider
	 * @param a
	 * @param b
	 * @return greatest common divider
	 */
	@Deprecated
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
	@Deprecated
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

	@Deprecated
	static public Vector3d getNewRandomInRange(int xRadius, int yRadius, int zRadius) {
		double x=Math.random()*xRadius*2 - xRadius;
		double y=Math.random()*yRadius*2 - yRadius;
		double z=Math.random()*zRadius*2 - zRadius;
		return new Vector3d(x,y,z);
	}


	/**
	 * Scale start to 0, end to 1, and x.  Returns the new value of x.
	 * @param start
	 * @param end
	 * @param x
	 * @return where x sits between start and end.  value may be outside range 0...1
	 */
	@Deprecated
	public static double getUnitInRange(double start, double end, double x) {
		double range = end-start;
		double p = x-start;
		return p/range;
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
