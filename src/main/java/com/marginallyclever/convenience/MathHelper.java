package com.marginallyclever.convenience;

import javax.vecmath.Vector3f;

/**
 * Math methods.
 * @author Dan Royer
 *
 */
public class MathHelper {
	public final static float EPSILON = 0.00001f;
	
	/**
	 * @return Square of length of vector (dx,dy,dz) 
	 */
	public static float lengthSquared(float dx,float dy,float dz) {
		return dx*dx+dy*dy+dz*dz;
	}
	
	
	/**
	 * @return Length of vector (dx,dy,dz) 
	 */
	public static float length(float dx,float dy,float dz) {
		return (float)Math.sqrt(lengthSquared(dx,dy,dz));
	}

	
	/**
	 * Round a float off to 3 decimal places.
	 * @param v a value
	 * @return Value rounded off to 3 decimal places
	 */
	public static float roundOff3(float v) {
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
	static public Vector3f rotateAroundAxis(Vector3f vec,Vector3f axis,float radians) {
		float C = (float)Math.cos(radians);
		float S = (float)Math.sin(radians);
		float x = vec.x;
		float y = vec.y;
		float z = vec.z;
		float u = axis.x;
		float v = axis.y;
		float w = axis.z;
		
		// (a*( v*v + w*w) - u*(b*v + c*w - u*x - v*y - w*z))(1.0-C)+x*C+(-c*v + b*w - w*y + v*z)*S
		// (b*( u*u + w*w) - v*(a*v + c*w - u*x - v*y - w*z))(1.0-C)+y*C+( c*u - a*w + w*x - u*z)*S
		// (c*( u*u + v*v) - w*(a*v + b*v - u*x - v*y - w*z))(1.0-C)+z*C+(-b*u + a*v - v*x + u*y)*S
		// but a=b=c=0 so
		// x' = ( -u*(- u*x - v*y - w*z)) * (1.0-C) + x*C + ( - w*y + v*z)*S
		// y' = ( -v*(- u*x - v*y - w*z)) * (1.0-C) + y*C + ( + w*x - u*z)*S
		// z' = ( -w*(- u*x - v*y - w*z)) * (1.0-C) + z*C + ( - v*x + u*y)*S
		
		float a = (-u*x - v*y - w*z);

		return new Vector3f( (-u*a) * (1.0f-C) + x*C + ( -w*y + v*z)*S,
							 (-v*a) * (1.0f-C) + y*C + (  w*x - u*z)*S,
							 (-w*a) * (1.0f-C) + z*C + ( -v*x + u*y)*S);
	}
	
	/**
	 * Prevent angle arg0 from leaving the range 0...2PI.  outside that range it wraps, like a modulus.
	 * @param arg0
	 * @return adjusted value
	 */
	static public double capRotationRadians(double arg0) {
		final double limit = Math.PI*2.0;
		while(arg0<0    ) arg0 += limit;
		while(arg0>limit) arg0 -= limit;
		return arg0;
	}
	
	/**
	 * Prevent angle arg0 from leaving the range 0...360.  outside that range it wraps, like a modulus.
	 * @param arg0
	 * @return adjusted value
	 */
	static public double capRotationDegrees(double arg0) {
		while(arg0<0  ) arg0 += 360;
		while(arg0>360) arg0 -= 360;
		return arg0;
	}
	
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

	
	// for floats
	static public float interpolate(float a,float b,double t) {
		return (b-a)*(float)t + a;
	}

	// for doubles
	static public double interpolate(double a,double b,double t) {
		return (b-a)*t + a;
	}

	
	// this is a lerp.  for normals you'd want a slerp
	static public Vector3f interpolate(Vector3f a,Vector3f b,double t) {
		Vector3f n = new Vector3f(b);
		n.sub(a);
		n.scale((float)t);
		n.add(a);
		
		return n;
	}
	
	// https://en.wikipedia.org/wiki/Slerp
	static public Vector3f slerp(Vector3f a,Vector3f b,double t) {
		
		// Dot product - the cosine of the angle between 2 vectors.
		float dot = a.dot(b);
		//dot = Math.min(Math.max(dot, -1.0f), 1.0f);
		if(dot<0) {
			b.scale(-1);
			dot = -dot;
		}
		if(Math.abs(dot)>=1-MathHelper.EPSILON) {
			Vector3f n = new Vector3f(b);
			n.sub(a);
			n.scale((float)t);
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
		
		//Vector3f n b - a*dot;
		a.scale((float)s0);
		b.scale((float)s1);
		Vector3f n = new Vector3f(a);
		n.add(b);
		n.normalize();
		
		return n;
	}
}
