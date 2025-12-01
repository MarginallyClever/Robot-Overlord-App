package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.OSHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.nio.FloatBuffer;

/**
 * <p>Use JOGL to open a GLJPanel in a JFrame and render a triangle.
 * Color each corner of the triangle RGB and interpolate the colors across the triangle.
 * Use GL4 (the old fixed-function pipeline) to do this, which also means creating minimal shader programs.</p>
 * <p>To use GL4 every mesh mush now be compiled into a special buffer that requires at least one Vertex Access Object
 * (VAO) and a Vertex Buffer Object (VBO), which is filled with your actual coordinates.  This data can now be loaded
 * once.  A GLSL program compiled and executed on the video card will use the VBO to render the dots on the screen.
 * Because the high-performance pipeline has taken over everything, you must also do your transforms in the GLSL script.</p>
 * <p>CC-BY-SA 2025-08-16 Dan Royer (dan@marginallyclever.com)</p>
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class MinimalOpenGL4 extends JPanel implements GLEventListener {
    private static final long startTime = System.currentTimeMillis();

    private final Animator animator;

    // shader stuff
    private int shaderId;
    private final String[] vertexCode = {
            "#version 400 core\n",
            "layout(location = 0) in vec3 position;\n",
            "layout(location = 1) in vec4 color;\n",
            "uniform mat4 model;\n",
            "out vec4 thruColor;\n",
            "void main() {\n",
            "    gl_Position = model * vec4(position, 1.0);\n",
            "    thruColor = color;\n",
            "}",
    };
    private final String[] fragmentCode = {
            "#version 400 core\n",
            "in  vec4 thruColor;\n",
            "out vec4 color;\n",
            "void main() {\n",
            "    color = thruColor;\n",
            "}",
    };
    private int vertexShaderId;
    private int fragmentShaderId;
    // connects the matrix on the CPU to the 'model' matrix in the shader script.
    private int matrixId;

    // mesh stuff
    private final float [] vertices = new float[] {
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.0f,  0.5f, 0.0f,
    };
    private final float [] colors = new float[] {
            1, 0, 0, 1,
            0, 1, 0, 1,
            0, 0, 1, 1,
    };
    private static final int NUM_BUFFERS = 2;
    private final int[] vao = new int[1];
    private final int[] vbo = new int[NUM_BUFFERS];
    private static final int VERTEX_COMPONENTS = 3;
    private static final int COLOR_COMPONENTS = 4;


    public static void main(String[] args) {
        JFrame frame = new JFrame("Hello World GL4");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new MinimalOpenGL4());
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public MinimalOpenGL4() {
        super(new BorderLayout());
        var glPanel = new GLJPanel(getCapabilities());
        glPanel.addGLEventListener(this);
        add(glPanel, BorderLayout.CENTER);
        animator = new Animator(glPanel);
    }

    private GLCapabilities getCapabilities() {
        GLProfile.initSingleton();
        GLCapabilities capabilities = new GLCapabilities(GLProfile.getMaxProgrammable(true));
        capabilities.setHardwareAccelerated(true);
        capabilities.setBackgroundOpaque(true);
        capabilities.setDoubleBuffered(true);
        capabilities.setDepthBits(32);  // 32 bit depth buffer is floating point
        return capabilities;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        animator.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        animator.stop();
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL4();
        //glAutoDrawable.setGL(new DebugGL4(gl));
        //gl = glAutoDrawable.getGL().getGL4();
        initPreferences(gl);
        initShader(gl);
        initMesh(gl);
        // this is needed to satisfy macOS requirements
        gl.glBindVertexArray(gl.getContext().getDefaultVAO());
    }

    private void initPreferences(GL4 gl) {
        gl.glClearColor(0.8f,0.8f,0.8f,1);
        gl.setSwapInterval(1);  // enable vsync to prevent screen tearing effect
        gl.glHint(GL4.GL_POLYGON_SMOOTH_HINT, GL4.GL_NICEST);
        gl.glEnable(GL4.GL_POLYGON_SMOOTH);
        gl.glEnable(GL4.GL_BLEND);
        gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void initShader(GL4 gl) {
        shaderId = gl.glCreateProgram();
        vertexShaderId = loadShader(gl, GL4.GL_VERTEX_SHADER, vertexCode,"vertex");
        fragmentShaderId = loadShader(gl, GL4.GL_FRAGMENT_SHADER, fragmentCode,"fragment");

        gl.glAttachShader(shaderId, vertexShaderId);
        gl.glAttachShader(shaderId, fragmentShaderId);

        // Ensure fragment output is bound to color attachment 0
        gl.glBindFragDataLocation(shaderId, 0, "color");

        gl.glLinkProgram(shaderId);

        if (!checkCompileStatus(gl, shaderId, GL4.GL_LINK_STATUS)) {
            throw new IllegalStateException("Failed to link shader program.");
        }
        gl.glValidateProgram(shaderId);
        if (!checkCompileStatus(gl, shaderId, GL4.GL_VALIDATE_STATUS)) {
            throw new IllegalStateException("Failed to validate shader program.");
        }

        matrixId = gl.glGetUniformLocation(shaderId, "model");
    }

    private int loadShader(GL4 gl, int type, String[] shaderCode, String name) {
        int shaderId = gl.glCreateShader(type);
        gl.glShaderSource(shaderId, shaderCode.length, shaderCode, null, 0);
        gl.glCompileShader(shaderId);
        if (!checkCompileStatus(gl, shaderId, GL4.GL_COMPILE_STATUS)) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shaderId, GL4.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shaderId, logLength[0], null, 0, log, 0);

            System.out.println("Failed to compile "+name+" shader code: " + new String(log));
        }
        return shaderId;
    }

    private void initMesh(GL4 gl) {
        createBuffers(gl);
        updateBuffers(gl);
    }

    private void updateBuffers(GL4 gl) {
        // put the vertices and colors into the vbo.
        gl.glBindVertexArray(vao[0]);
        setupOneBuffer(gl,0, VERTEX_COMPONENTS, vertices);
        setupOneBuffer(gl,1, COLOR_COMPONENTS, colors  );
    }

    private void bindOneBuffer(GL4 gl, int attribIndex) {
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[attribIndex]);
    }

    private void setupOneBuffer(GL4 gl, int attribIndex, int size, float [] data) {
        bindOneBuffer(gl, attribIndex);
        gl.glBufferData(GL4.GL_ARRAY_BUFFER, (long) data.length * Float.BYTES, FloatBuffer.wrap(data), GL4.GL_STATIC_DRAW);
        gl.glVertexAttribPointer(attribIndex,size,GL4.GL_FLOAT,false,0,0);
        gl.glEnableVertexAttribArray(attribIndex);
    }
    
    private void createBuffers(GL4 gl) {
        // init vao
        gl.glGenVertexArrays(1, vao, 0);
        // init vbo
        gl.glGenBuffers(NUM_BUFFERS, vbo, 0);
    }

    /**
     * Check the status of a shader or program.
     *
     * @param gl    The OpenGL context
     * @param id    The shader or program id
     * @param param The parameter to check
     * @return true if the status is OK
     */
    private boolean checkCompileStatus(GL4 gl, int id, int param) {
        int[] result = new int[]{GL4.GL_FALSE};
        if (param == GL4.GL_COMPILE_STATUS) {
            gl.glGetShaderiv(id, param, result, 0);
        } else {
            gl.glGetProgramiv(id, param, result, 0);
        }
        return result[0] != GL4.GL_FALSE;
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        glAutoDrawable.getGL().getGL4().glViewport(0, 0, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL4();
        disposeMesh(gl);
        disposeShader(gl);
    }

    private void disposeMesh(GL4 gl) {
        gl.glDeleteBuffers(NUM_BUFFERS, vbo, 0);
        gl.glDeleteVertexArrays(1, vao, 0);
    }

    private void disposeShader(GL4 gl) {
        gl.glDetachShader(shaderId, vertexShaderId);
        gl.glDetachShader(shaderId, fragmentShaderId);
        gl.glDeleteShader(vertexShaderId);
        gl.glDeleteShader(fragmentShaderId);
        gl.glDeleteProgram(shaderId);
    }

    /**
     * Render one frame of the scene.
     * @param glAutoDrawable the OpenGL drawable to render to.
     */
    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL4();
        gl.glClear(GL4.GL_COLOR_BUFFER_BIT);
        gl.glUseProgram(shaderId);
        spinTriangle(gl);
        drawTriangle(gl);
        // this is needed to satisfy macOS requirements
        gl.glBindVertexArray(gl.getContext().getDefaultVAO());
    }

    private void spinTriangle(GL4 gl) {
        double seconds = (System.currentTimeMillis() - startTime) / 1000.0;
        var m = new Matrix4d();
        m.rotZ(Math.toRadians(seconds * 90));  // 90 degrees per second
        uploadMatrixToCurrentShader(gl,m);
    }

    // assumes matrixId is still valid
    private void uploadMatrixToCurrentShader(GL4 gl, Matrix4d m) {
        float [] list = {
                (float) m.m00, (float) m.m10, (float) m.m20, (float) m.m30,
                (float) m.m01, (float) m.m11, (float) m.m21, (float) m.m31,
                (float) m.m02, (float) m.m12, (float) m.m22, (float) m.m32,
                (float) m.m03, (float) m.m13, (float) m.m23, (float) m.m33
        };
        gl.glUniformMatrix4fv(matrixId, 1, false, list, 0);
    }

    private void drawTriangle(GL4 gl) {
        gl.glBindVertexArray(vao[0]);
        gl.glDrawArrays(GL4.GL_TRIANGLES, 0, 3);
    }
}