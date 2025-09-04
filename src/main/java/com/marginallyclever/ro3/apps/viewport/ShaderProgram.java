package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>{@link ShaderProgram} is a wrapper for shader programs made of several {@link Shader}s.  It also provides a
 * simple interface for setting uniforms.</p>
 */
public class ShaderProgram {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgram.class);
    private int programId = -1;
    private final List<Shader> shaders = new ArrayList<>();
    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private final FloatBuffer matrixBuffer = FloatBuffer.allocate(16);

    /**
     * Package private constructor.  Use {@link ShaderProgramFactory} to create instances.
     * @param shaders the list of shaders to link into a program
     */
    ShaderProgram(List<Shader> shaders) {
        super();
        this.shaders.addAll(shaders);
    }

    private void load(GL3 gl) {
        programId = gl.glCreateProgram();

        for( Shader shader : shaders ) {
            shader.load(gl);
            gl.glAttachShader(programId, shader.getShaderId());
            OpenGLHelper.checkGLError(gl,logger);
        }
        gl.glLinkProgram(programId);
        if (!OpenGLHelper.checkStatus(gl, programId, GL3.GL_LINK_STATUS)) {
            showProgramError(gl,"Failed to link shader program.");
            programId=-1;
            return;
        }
        gl.glValidateProgram(programId);
        if (!OpenGLHelper.checkStatus(gl, programId, GL3.GL_VALIDATE_STATUS)) {
            showProgramError(gl,"Failed to validate shader program.");
            programId=-1;
        }
    }

    public void showProgramError(GL3 gl, String message) {
        int[] logLength = new int[1];
        gl.glGetProgramiv(programId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
        byte[] log = new byte[logLength[0]];
        gl.glGetProgramInfoLog(programId, logLength[0], null, 0, log, 0);
        logger.error(message + new String(log));
    }


    public void use(GL3 gl) {
        if(programId == -1) {
            load(gl);
        }
        gl.glUseProgram(programId);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void unload(GL3 gl) {
        if(programId == -1) return; // not loaded
        for( Shader shader : shaders ) {
            if(shader.getShaderId() == -1) continue; // not loaded
            gl.glDetachShader(programId, shader.getShaderId());
            shader.unload(gl);
        }
        gl.glDeleteProgram(programId);
        programId = -1;
    }

    public int getProgramId() {
        return programId;
    }

    public int getUniformLocation(GL3 gl, String name) {
        Integer result = uniformLocations.get(name);
        if(result == null) {
            result = gl.glGetUniformLocation(programId, name);
            if(result==-1) throw new InvalidParameterException("Could not find uniform "+name);
            uniformLocations.put(name,result);
        }
        return result;
    }

    public void set1f(GL3 gl, String name, float v0) {
        gl.glUniform1f(getUniformLocation(gl, name), v0);
    }

    public void set2f(GL3 gl, String name, float v0, float v1) {
        gl.glUniform2f(getUniformLocation(gl, name), v0, v1);
    }

    public void set3f(GL3 gl, String name, float v0, float v1, float v2) {
        gl.glUniform3f(getUniformLocation(gl, name), v0, v1, v2);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void set4f(GL3 gl, String name, float v0, float v1, float v2, float v3) {
        gl.glUniform4f(getUniformLocation(gl, name), v0, v1, v2, v3);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void setVector3d(GL3 gl, String name, Vector3d value) {
        gl.glUniform3f(getUniformLocation(gl, name), (float) value.x, (float) value.y, (float) value.z);
        OpenGLHelper.checkGLError(gl,logger);
    }

    private FloatBuffer matrixToFloatBuffer(Matrix4d m) {
        matrixBuffer.put( (float)m.m00 );
        matrixBuffer.put( (float)m.m01 );
        matrixBuffer.put( (float)m.m02 );
        matrixBuffer.put( (float)m.m03 );

        matrixBuffer.put( (float)m.m10 );
        matrixBuffer.put( (float)m.m11 );
        matrixBuffer.put( (float)m.m12 );
        matrixBuffer.put( (float)m.m13 );

        matrixBuffer.put( (float)m.m20 );
        matrixBuffer.put( (float)m.m21 );
        matrixBuffer.put( (float)m.m22 );
        matrixBuffer.put( (float)m.m23 );

        matrixBuffer.put( (float)m.m30 );
        matrixBuffer.put( (float)m.m31 );
        matrixBuffer.put( (float)m.m32 );
        matrixBuffer.put( (float)m.m33 );
        matrixBuffer.rewind();

        return matrixBuffer;
    }

    /**
     * Set a matrix in the shader.  Java uses column-major order, where OpenGL and DirectX use row-major order.
     * Thus the thurd parameter is true to make the video card transpose the matrix from row-major to column-major.
     * @see <a href="https://registry.khronos.org/OpenGL-Refpages/gl4/html/glUniform.xhtml">glUniform</a>
     * @param gl the viewport context
     * @param name the name of the uniform variable
     * @param value the matrix to set
     */
    public void setMatrix4d(GL3 gl, String name, Matrix4d value) {
        gl.glUniformMatrix4fv(getUniformLocation(gl, name), 1, true, matrixToFloatBuffer(value));
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void set1i(GL3 gl, String name, int value) {
        gl.glUniform1i(getUniformLocation(gl, name), value);
        OpenGLHelper.checkGLError(gl,logger);
    }

    public void setColor(GL3 gl3, String name, Color color) {
        set4f(gl3,name,color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f,color.getAlpha()/255f);
    }
}