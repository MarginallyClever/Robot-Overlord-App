package com.marginallyclever.robotoverlord;
import com.jogamp.opengl.GL2;
import java.util.Arrays;

public class ShaderProgram {
    private final int programId;
    private final int vertexShaderId;
    private final int fragmentShaderId;

    public ShaderProgram(GL2 gl, String[] vertexCode, String[] fragmentCode) {
        vertexShaderId = loadShader(gl, GL2.GL_VERTEX_SHADER, vertexCode);
        fragmentShaderId = loadShader(gl, GL2.GL_FRAGMENT_SHADER, fragmentCode);
        programId = gl.glCreateProgram();
        gl.glAttachShader(programId, vertexShaderId);
        gl.glAttachShader(programId, fragmentShaderId);
        gl.glLinkProgram(programId);
        if (!checkStatus(gl, programId, GL2.GL_LINK_STATUS)) {
            showProgramError(gl,"Failed to link shader program: ");
        } else {
            gl.glValidateProgram(programId);
            if (!checkStatus(gl, programId, GL2.GL_VALIDATE_STATUS)) {
                showProgramError(gl, "Failed to validate shader program: ");
            }
        }
    }

    private void showProgramError(GL2 gl, String message) {
        int[] logLength = new int[1];
        gl.glGetProgramiv(programId, GL2.GL_INFO_LOG_LENGTH, logLength, 0);

        byte[] log = new byte[logLength[0]];
        gl.glGetProgramInfoLog(programId, logLength[0], null, 0, log, 0);

        System.err.println(message+ new String(log));
    }

    private int loadShader(GL2 gl, int type, String[] shaderCode) {
        int shaderId = gl.glCreateShader(type);
        gl.glShaderSource(shaderId, shaderCode.length, shaderCode, null, 0);
        gl.glCompileShader(shaderId);
        if (!checkStatus(gl, shaderId, GL2.GL_COMPILE_STATUS)) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shaderId, GL2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shaderId, logLength[0], null, 0, log, 0);

            System.err.println("Failed to compile shader code: "+ new String(log));
        }
        return shaderId;
    }

    /**
     * Check the status of a shader or program.
     * @param gl    The OpenGL context
     * @param id    The shader or program id
     * @param param The parameter to check
     * @return true if the status is OK
     */
    private boolean checkStatus(GL2 gl, int id, int param) {
        int[] result = new int[]{GL2.GL_FALSE};
        if (param == GL2.GL_COMPILE_STATUS) {
            gl.glGetShaderiv(id, param, result, 0);
        } else {
            gl.glGetProgramiv(id, param, result, 0);
        }
        return result[0] != GL2.GL_FALSE;
    }

    public void use(GL2 gl2) {
        gl2.glUseProgram(programId);
    }

    public void delete(GL2 gl) {
        gl.glDetachShader(programId, vertexShaderId);
        gl.glDetachShader(programId, fragmentShaderId);
        gl.glDeleteShader(vertexShaderId);
        gl.glDeleteShader(fragmentShaderId);
        gl.glDeleteProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }

    public int getUniformLocation(GL2 gl, String name) {
        return gl.glGetUniformLocation(programId, name);
    }
}
