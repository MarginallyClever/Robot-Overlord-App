package com.marginallyclever.convenience;

import javax.vecmath.Vector3f;

public class MathHelper {
	public static float lengthSquared(float dx,float dy,float dz) {
		return dx*dx+dy*dy+dz*dz;
	}
	
	
	public static float length(float dx,float dy,float dz) {
		return (float)Math.sqrt(lengthSquared(dx,dy,dz));
	}

	
	// round off to 3 decimal places
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
	 * @param angle the amount in radians to rotate
	 * @return the new vector
	 */
	static public Vector3f rotateAroundAxis(Vector3f vec,Vector3f axis,float angle) {
		float C = (float)Math.cos(angle);
		float S = (float)Math.sin(angle);
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
}
