package com.marginallyclever.robotOverlord;

import java.nio.FloatBuffer;

import com.jogamp.opengl.GL2;

public class Matrix4x4 {
	private float[] m = new float[16];
	private FloatBuffer floatBuffer=FloatBuffer.allocate(16);
	
	public Matrix4x4() {}

	public void init() {
		int i;
		for(i=0;i<16;++i) {
			m[0]=0;
		}
		m[0]=m[5]=m[10]=m[15]=1;
	}
	
	public void render(GL2 gl2) {
		int i;
		for(i=0;i<16;++i) {
			floatBuffer.put(i, m[i]);
		}

	    gl2.glMultMatrixf(floatBuffer);
	}
}
