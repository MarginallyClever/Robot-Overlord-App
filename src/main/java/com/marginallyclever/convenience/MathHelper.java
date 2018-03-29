package com.marginallyclever.convenience;

import javax.vecmath.Vector3f;

/**
 * Math methods.
 * @author Dan Royer
 *
 */
public class MathHelper {
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
		final double limit = 360;
		while(arg0<0    ) arg0 += limit;
		while(arg0>limit) arg0 -= limit;
		return arg0;
	}
}
