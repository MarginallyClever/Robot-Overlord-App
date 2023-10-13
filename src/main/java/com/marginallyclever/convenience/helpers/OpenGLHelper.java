package com.marginallyclever.convenience.helpers;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A collection of static methods to help with OpenGL.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class OpenGLHelper {
	public static int drawAtopEverythingStart(GL3 gl) {
		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl.glGetIntegerv(GL3.GL_DEPTH_FUNC, depthFunc);
		gl.glDepthFunc(GL3.GL_ALWAYS);
		return depthFunc.get();
	}

	public static void checkGLError(GL3 gl3,org.slf4j.Logger logger) {
		int err = gl3.glGetError();
		if(err != GL.GL_NO_ERROR) {
			GLU glu = new GLU();
			logger.error("GL error {}: {}", err, glu.gluErrorString(err));
		}
	}

	public static void drawAtopEverythingEnd(GL3 gl, int previousState) {
		gl.glDepthFunc(previousState);
	}

	public static float setLineWidth(GL3 gl,float newWidth) {
		FloatBuffer lineWidth = FloatBuffer.allocate(1);
		gl.glGetFloatv(GL3.GL_LINE_WIDTH, lineWidth);
		gl.glLineWidth(newWidth);
		return lineWidth.get(0);
	}

	public static boolean disableTextureStart(GL3 gl) {
		boolean b = gl.glIsEnabled(GL3.GL_TEXTURE_2D);
		gl.glDisable(GL3.GL_TEXTURE_2D);
		return b;
	}
	
	public static void disableTextureEnd(GL3 gl,boolean oldState) {
		if(oldState) gl.glEnable(GL3.GL_TEXTURE_2D);
	}
}
