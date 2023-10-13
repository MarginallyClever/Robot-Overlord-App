package com.marginallyclever.robotoverlord.systems.render;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * A wrapper for vertex and fragment shader pairs that provides a simple interface for setting uniforms.
 *
 * @author Dan Royer
 * @since 2.5.9
 */
public class ShaderProgram {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgram.class);
    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;

    public ShaderProgram(GL3 gl, String[] vertexCode, String[] fragmentCode) {
        vertexShaderId = loadShader(gl, GL3.GL_VERTEX_SHADER, vertexCode,"vertex");
        fragmentShaderId = loadShader(gl, GL3.GL_FRAGMENT_SHADER, fragmentCode,"fragment");
        programId = gl.glCreateProgram();
        gl.glAttachShader(programId, vertexShaderId);
        gl.glAttachShader(programId, fragmentShaderId);
        gl.glLinkProgram(programId);
        if (!checkStatus(gl, programId, GL3.GL_LINK_STATUS)) {
            showProgramError(gl, "Failed to link shader program: ");
        } else {
            gl.glValidateProgram(programId);
            if (!checkStatus(gl, programId, GL3.GL_VALIDATE_STATUS)) {
                showProgramError(gl, "Failed to validate shader program: ");
            }
        }
    }

    private void showProgramError(GL3 gl, String message) {
        int[] logLength = new int[1];
        gl.glGetProgramiv(programId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

        byte[] log = new byte[logLength[0]];
        gl.glGetProgramInfoLog(programId, logLength[0], null, 0, log, 0);

        System.err.println(message + new String(log));
        logger.error(message + new String(log));
    }

    private int loadShader(GL3 gl, int type, String[] shaderCode, String name) {
        int shaderId = gl.glCreateShader(type);
        gl.glShaderSource(shaderId, shaderCode.length, shaderCode, null, 0);
        gl.glCompileShader(shaderId);
        if (!checkStatus(gl, shaderId, GL3.GL_COMPILE_STATUS)) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shaderId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shaderId, logLength[0], null, 0, log, 0);

            System.err.println("Failed to compile "+name+" shader code: " + new String(log));
            logger.error("Failed to compile "+name+" shader code: " + new String(log));
        }
        return shaderId;
    }

    /**
     * Check the status of a shader or program.
     *
     * @param gl    The OpenGL context
     * @param id    The shader or program id
     * @param param The parameter to check
     * @return true if the status is OK
     */
    private boolean checkStatus(GL3 gl, int id, int param) {
        int[] result = new int[]{GL3.GL_FALSE};
        if (param == GL3.GL_COMPILE_STATUS) {
            gl.glGetShaderiv(id, param, result, 0);
        } else {
            gl.glGetProgramiv(id, param, result, 0);
        }
        return result[0] != GL3.GL_FALSE;
    }

    public void use(GL3 gl) {
        gl.glUseProgram(programId);
    }

    public void delete(GL3 gl) {
        gl.glDetachShader(programId, vertexShaderId);
        gl.glDetachShader(programId, fragmentShaderId);
        gl.glDeleteShader(vertexShaderId);
        gl.glDeleteShader(fragmentShaderId);
        gl.glDeleteProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }

    public int getUniformLocation(GL3 gl, String name) {
        return gl.glGetUniformLocation(programId, name);
    }

    public void set1f(GL3 gl, String name, float v0) {
        gl.glUniform1f(getUniformLocation(gl, name), v0);
    }

    public void set2f(GL3 gl, String name, float v0, float v1) {
        gl.glUniform2f(getUniformLocation(gl, name), v0, v1);
    }

    public void set3f(GL3 gl, String name, float v0, float v1, float v2) {
        int location = getUniformLocation(gl, name);
        if(location==-1) return;
        gl.glUniform3f(location, v0, v1, v2);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void set4f(GL3 gl, String name, float v0, float v1, float v2, float v3) {
        int location = getUniformLocation(gl, name);
        if(location==-1) return;
        gl.glUniform4f(location, v0, v1, v2, v3);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void setVector3d(GL3 gl, String name, Vector3d v) {
        int location = getUniformLocation(gl, name);
        if(location==-1) return;
        gl.glUniform3f(location, (float) v.x, (float) v.y, (float) v.z);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void setMatrix4d(GL3 gl, String name, Matrix4d matrix4d) {
        int location = getUniformLocation(gl, name);
        if(location==-1) return;
        gl.glUniformMatrix4fv(location, 1, false, MatrixHelper.matrixToFloatBuffer(matrix4d));
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void set1i(GL3 gl, String name, int b) {
        int location = getUniformLocation(gl, name);
        if(location==-1) return;
        gl.glUniform1i(location, b);
        OpenGLHelper.checkGLError(gl,logger);
    }
}