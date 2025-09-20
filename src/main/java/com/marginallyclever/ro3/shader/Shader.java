package com.marginallyclever.ro3.shader;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.ro3.apps.viewport.OpenGL3Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>{@link Shader} is a wrapper for a single shader (vertex, fragment, geometry, etc).</p>
 */
public class Shader implements OpenGL3Resource {
    private static final Logger logger = LoggerFactory.getLogger(Shader.class);

    private final int shaderType;
    private final String name;
    private final String [] shaderCode;

    private int shaderId=-1;
    private int refCount=0;

    /**
     * Package private constructor.  Use {@link ShaderProgramFactory} to create instances.
     * @param shaderType one of VERTEX, FRAGMENT, GEOMETRY
     * @param shaderCode the shader source code
     * @param name a name for the shader (for logging purposes)
     */
    Shader(int shaderType, String[] shaderCode, String name) {
        super();
        this.shaderType = shaderType;
        this.shaderCode = shaderCode;
        this.name = name;
    }

    public void use(GL3 gl) {
        if( shaderId == -1 ) {
            load(gl);
        }
    }

    /**
     * Load and compile the shader code.
     * @param gl the GL context
     */
    void load(GL3 gl) {
        refCount++;
        if(refCount>1) return;

        shaderId = gl.glCreateShader(shaderType);
        OpenGLHelper.checkGLError(gl, logger);
        gl.glShaderSource(shaderId, shaderCode.length, shaderCode, null, 0);
        OpenGLHelper.checkGLError(gl, logger);
        gl.glCompileShader(shaderId);
        OpenGLHelper.checkGLError(gl, logger);
        if (!OpenGLHelper.checkStatus(gl, shaderId, GL3.GL_COMPILE_STATUS)) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shaderId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shaderId, logLength[0], null, 0, log, 0);
            throw new RuntimeException("Failed to compile " + name + " shader code: " + new String(log));
        }
    }

    @Override
    public void unload(GL3 gl3) {
        if(refCount>0) refCount--;
        if( shaderId != -1 && refCount == 0 ) {
            gl3.glDeleteShader(shaderId);
            OpenGLHelper.checkGLError(gl3, logger);
            shaderId = -1;
        }
    }

    public int getShaderId() {
        return shaderId;
    }
}
