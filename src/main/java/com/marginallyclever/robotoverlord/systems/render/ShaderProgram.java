package com.marginallyclever.robotoverlord.systems.render;

import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL2GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
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

    public ShaderProgram(GL2GL3 gl, String[] vertexCode, String[] fragmentCode) {
        vertexShaderId = loadShader(gl, GL2GL3.GL_VERTEX_SHADER, vertexCode,"vertex");
        fragmentShaderId = loadShader(gl, GL2GL3.GL_FRAGMENT_SHADER, fragmentCode,"fragment");
        programId = gl.glCreateProgram();
        gl.glAttachShader(programId, vertexShaderId);
        gl.glAttachShader(programId, fragmentShaderId);
        gl.glLinkProgram(programId);
        if (!checkStatus(gl, programId, GL2GL3.GL_LINK_STATUS)) {
            showProgramError(gl, "Failed to link shader program: ");
        } else {
            gl.glValidateProgram(programId);
            if (!checkStatus(gl, programId, GL2GL3.GL_VALIDATE_STATUS)) {
                showProgramError(gl, "Failed to validate shader program: ");
            }
        }
    }

    private void showProgramError(GL2GL3 gl, String message) {
        int[] logLength = new int[1];
        gl.glGetProgramiv(programId, GL2GL3.GL_INFO_LOG_LENGTH, logLength, 0);

        byte[] log = new byte[logLength[0]];
        gl.glGetProgramInfoLog(programId, logLength[0], null, 0, log, 0);

        System.err.println(message + new String(log));
        logger.error(message + new String(log));
    }

    private int loadShader(GL2GL3 gl, int type, String[] shaderCode,String name) {
        int shaderId = gl.glCreateShader(type);
        gl.glShaderSource(shaderId, shaderCode.length, shaderCode, null, 0);
        gl.glCompileShader(shaderId);
        if (!checkStatus(gl, shaderId, GL2GL3.GL_COMPILE_STATUS)) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shaderId, GL2GL3.GL_INFO_LOG_LENGTH, logLength, 0);

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
    private boolean checkStatus(GL2GL3 gl, int id, int param) {
        int[] result = new int[]{GL2GL3.GL_FALSE};
        if (param == GL2GL3.GL_COMPILE_STATUS) {
            gl.glGetShaderiv(id, param, result, 0);
        } else {
            gl.glGetProgramiv(id, param, result, 0);
        }
        return result[0] != GL2GL3.GL_FALSE;
    }

    public void use(GL2GL3 gl2) {
        gl2.glUseProgram(programId);
    }

    public void delete(GL2GL3 gl) {
        gl.glDetachShader(programId, vertexShaderId);
        gl.glDetachShader(programId, fragmentShaderId);
        gl.glDeleteShader(vertexShaderId);
        gl.glDeleteShader(fragmentShaderId);
        gl.glDeleteProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }

    public int getUniformLocation(GL2GL3 gl2, String name) {
        return gl2.glGetUniformLocation(programId, name);
    }

    public void set1f(GL2GL3 gl2, String name, float v0) {
        gl2.glUniform1f(getUniformLocation(gl2, name), v0);
    }

    public void set2f(GL2GL3 gl2, String name, float v0, float v1) {
        gl2.glUniform2f(getUniformLocation(gl2, name), v0, v1);
    }

    public void set3f(GL2GL3 gl2, String name, float v0, float v1, float v2) {
        gl2.glUniform3f(getUniformLocation(gl2, name), v0, v1, v2);
    }

    public void set4f(GL2GL3 gl2, String name, float v0, float v1, float v2, float v3) {
        gl2.glUniform4f(getUniformLocation(gl2, name), v0, v1, v2, v3);
    }

    public void setVector3d(GL2GL3 gl2, String name, Vector3d v) {
        gl2.glUniform3f(getUniformLocation(gl2, name), (float) v.x, (float) v.y, (float) v.z);
    }

    public void setMatrix4d(GL2GL3 gl2, String name, Matrix4d matrix4d) {
        gl2.glUniformMatrix4fv(getUniformLocation(gl2, name), 1, false, MatrixHelper.matrixToFloatBuffer(matrix4d));
    }

    public void set1i(GL2GL3 gl2, String name, int b) {
        gl2.glUniform1i(getUniformLocation(gl2, name), b );
    }
}