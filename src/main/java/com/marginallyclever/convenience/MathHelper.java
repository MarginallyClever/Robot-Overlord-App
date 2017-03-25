package com.marginallyclever.convenience;

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
}
