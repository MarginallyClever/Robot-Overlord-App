package com.marginallyclever.convenience.helpers;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.glu.GLU;
import com.marginallyclever.ro3.apps.viewport.ShaderProgram;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A collection of static methods to help with OpenGL.
 *
 */
public class OpenGLHelper {
	private static final IntBuffer depthFunc = IntBuffer.allocate(1);
	private static final FloatBuffer lineWidth = FloatBuffer.allocate(1);

	public static int drawAtopEverythingStart(GL3 gl) {
		gl.glGetIntegerv(GL3.GL_DEPTH_FUNC, depthFunc);
		gl.glDepthFunc(GL3.GL_ALWAYS);
		return depthFunc.get();
	}

    /**
     * Check for OpenGL errors and log them.  Note that the error state is no reset on every gl call, so
     * you may want to investigate further up the call stack to find the real source of the error.
     * @param gl3 the GL3 context
     * @param logger the logger to use
     */
	public static void checkGLError(GL3 gl3,org.slf4j.Logger logger) {
		int err = gl3.glGetError();
		if(err != GL.GL_NO_ERROR) {
			GLU glu = GLU.createGLU(gl3);
			logger.error("GL error {}: {}", err, glu.gluErrorString(err));

            if (!gl3.getContext().isCurrent()) {
                logger.error("OpenGL context is not current at glBindVertexArray");
                throw new IllegalStateException("No active OpenGL context");
            }
        }
	}

	public static void drawAtopEverythingEnd(GL3 gl, int previousState) {
		gl.glDepthFunc(previousState);
	}

	public static float setLineWidth(GL3 gl,float newWidth) {
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

    /**
     * Check the status of a shader or program.
     *
     * @param gl    The OpenGL context
     * @param id    The shader or program id
     * @param param The parameter to check
     * @return true if the status is OK
     */
    public static boolean checkStatus(GL3 gl, int id, int param) {
        int[] result = new int[]{GL3.GL_FALSE};
        if (param == GL3.GL_COMPILE_STATUS) {
            gl.glGetShaderiv(id, param, result, 0);
        } else {
            gl.glGetProgramiv(id, param, result, 0);
        }
        return result[0] != GL3.GL_FALSE;
    }
}
