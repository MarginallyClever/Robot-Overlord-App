package com.marginallyclever.robotOverlord.model;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;

public class VertexBufferObject {
	public final int NUM_BUFFERS = 3;
	public int [] VBO = new int[NUM_BUFFERS];
	
	public VertexBufferObject() {
		GL2 gl2 = GLContext.getCurrent().getGL().getGL2();

		int [] VBO = new int[NUM_BUFFERS];
		gl2.glGenBuffers(NUM_BUFFERS, VBO, 0);  // 2 = one for vertexes, one for normals
	}

	public void destroy() {
		GL2 gl2 = GLContext.getCurrent().getGL().getGL2();

		gl2.glDeleteBuffers(NUM_BUFFERS, VBO,0);
	}
}
