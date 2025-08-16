package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * <p>Use JOGL to open a GLJPanel in a JFrame and render a triangle.
 * Color each corner of the triangle RGB and interpolate the colors across the triangle.
 * Use GL3 (the old fixed-function pipeline) to do this, which also means creating minimal shader programs.</p>
 * <p>To use GL3 every mesh mush now be compiled into a special buffer that requires at least one Vertex Access Object
 * (VAO) and a Vertex Buffer Object (VBO), which is filled with your actual coordinates.  This data can now be loaded
 * once.  A GLSL program compiled and executed on the video card will use the VBO to render the dots on the screen.
 * Because the high-performance pipeline has taken over everything, you must also do your transforms in the GLSL script.</p>
 * <p>CC-BY-SA 2025-08-16 Dan Royer (dan@marginallyclever.com)</p>
 */
public class MinimalOpenGL3 extends JPanel implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(MinimalOpenGL3.class);
    private final GLJPanel glPanel;
    private final FPSAnimator animator;

    private static boolean HARDWARE_ACCELERATED = true;
    private static boolean DOUBLE_BUFFERED = true;
    private static final int FSAA_SAMPLES = 2;
    private static final int FPS = 30;

    private static final long startTime = System.currentTimeMillis();

    // shader stuff
    private int shaderId;
    private final String[] vertexCode = {
            "#version 330 core\n",
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
            "#version 330 core\n",
            "in  vec4 thruColor;\n",
            "out vec4 color;\n",
            "void main() {\n",
            "    color = thruColor;\n",
            "}",
    };
    private int vertexShaderId;
    private int fragmentShaderId;

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
    private int[] vao = new int[1];
    private int[] vbo = new int[NUM_BUFFERS];


    public static void main(String[] args) {
        logger.info("start time "+startTime);
        // create a JFrame, add a JHelloWorldGL2 to it, and make it visible.
        JFrame frame = new JFrame("Hello World GL3");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        MinimalOpenGL3 panel = new MinimalOpenGL3();
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);
    }

    public MinimalOpenGL3() {
        super();
        var capabilities = getCapabilities();
        glPanel = new GLJPanel(capabilities);
        this.setLayout(new java.awt.BorderLayout());
        this.add(glPanel, java.awt.BorderLayout.CENTER);
        animator = new FPSAnimator(glPanel, FPS);
        animator.start();
    }

    private GLCapabilities getCapabilities() {
        GLProfile profile = GLProfile.getMaxProgrammable(true);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(HARDWARE_ACCELERATED);
        capabilities.setBackgroundOpaque(true);
        capabilities.setDoubleBuffered(DOUBLE_BUFFERED);
        //capabilities.setStencilBits(8);
        capabilities.setDepthBits(32);  // 32 bit depth buffer is floating point
        if(FSAA_SAMPLES > 0) {
            capabilities.setSampleBuffers(true);
            capabilities.setNumSamples(1<< FSAA_SAMPLES);
        }
        StringBuilder sb = new StringBuilder();
        capabilities.toString(sb);
        logger.info("capabilities="+sb);
        return capabilities;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if(glPanel!=null) glPanel.addGLEventListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if(glPanel!=null) glPanel.removeGLEventListener(this);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL3();
        initPreferences(gl);
        initShader(gl);
        initMesh(gl);
    }

    private void initPreferences(GL3 gl) {
        gl.glClearColor(0.8f,0.8f,0.8f,1);
        gl.glHint(GL3.GL_LINE_SMOOTH, GL3.GL_NICEST);
        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glEnable(GL3.GL_BLEND);
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void initShader(GL3 gl) {
        shaderId = gl.glCreateProgram();
        vertexShaderId = loadShader(gl, GL3.GL_VERTEX_SHADER, vertexCode,"vertex");
        fragmentShaderId = loadShader(gl, GL3.GL_FRAGMENT_SHADER, fragmentCode,"fragment");

        gl.glAttachShader(shaderId, vertexShaderId);
        gl.glAttachShader(shaderId, fragmentShaderId);

        gl.glLinkProgram(shaderId);

        if (!checkCompileStatus(gl, shaderId, GL3.GL_LINK_STATUS)) {
            throw new IllegalStateException("Failed to link shader program.");
        }
        gl.glValidateProgram(shaderId);
        if (!checkCompileStatus(gl, shaderId, GL3.GL_VALIDATE_STATUS)) {
            throw new IllegalStateException("Failed to validate shader program.");
        }
    }

    private int loadShader(GL3 gl, int type, String[] shaderCode, String name) {
        int shaderId = gl.glCreateShader(type);
        gl.glShaderSource(shaderId, shaderCode.length, shaderCode, null, 0);
        gl.glCompileShader(shaderId);
        if (!checkCompileStatus(gl, shaderId, GL3.GL_COMPILE_STATUS)) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shaderId, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shaderId, logLength[0], null, 0, log, 0);

            logger.error("Failed to compile "+name+" shader code: " + new String(log));
        }
        return shaderId;
    }

    private void initMesh(GL3 gl) {
        createBuffers(gl);
        updateBuffers(gl);
    }

    private void updateBuffers(GL3 gl) {
        // put the vertices and colors into the vbo.
        gl.glBindVertexArray(vao[0]);
        OpenGLHelper.checkGLError(gl,logger);

        setupArray(gl,0,3,vertices);
        OpenGLHelper.checkGLError(gl,logger);

        setupArray(gl,1,4,colors  );
        OpenGLHelper.checkGLError(gl,logger);

        gl.glBindVertexArray(0);
    }

    private void bindArray(GL3 gl, int attribIndex, int size) {
        gl.glEnableVertexAttribArray(attribIndex);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[attribIndex]);
        gl.glVertexAttribPointer(attribIndex,size,GL3.GL_FLOAT,false,0,0);
        OpenGLHelper.checkGLError(gl,logger);
    }

    private void setupArray(GL3 gl, int attribIndex, int size, float [] data) {
        bindArray(gl, attribIndex, size);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, (long) data.length * Float.BYTES, FloatBuffer.wrap(data), GL3.GL_STATIC_DRAW);
        OpenGLHelper.checkGLError(gl,logger);
    }
    
    private void createBuffers(GL3 gl) {
        // init vao
        gl.glGenVertexArrays(1, vao, 0);
        checkGLError(gl);

        // init vbo
        gl.glGenBuffers(NUM_BUFFERS, vbo, 0);
        checkGLError(gl);
    }

    public static void checkGLError(GL3 gl3) {
        int err = gl3.glGetError();
        if(err != GL.GL_NO_ERROR) {
            GLU glu = GLU.createGLU(gl3);
            logger.error("GL error {}: {}", err, glu.gluErrorString(err));
        }
    }

    /**
     * Check the status of a shader or program.
     *
     * @param gl    The OpenGL context
     * @param id    The shader or program id
     * @param param The parameter to check
     * @return true if the status is OK
     */
    private boolean checkCompileStatus(GL3 gl, int id, int param) {
        int[] result = new int[]{GL3.GL_FALSE};
        if (param == GL3.GL_COMPILE_STATUS) {
            gl.glGetShaderiv(id, param, result, 0);
        } else {
            gl.glGetProgramiv(id, param, result, 0);
        }
        return result[0] != GL3.GL_FALSE;
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL3();

        disposeMesh(gl);
        disposeShader(gl);

        animator.stop();
        gl.glFinish(); // Ensure all OpenGL commands are completed before disposing
        logger.info("OpenGL resources disposed.");
    }

    private void disposeMesh(GL3 gl) {
        gl.glDeleteBuffers(NUM_BUFFERS, vbo, 0);
        gl.glDeleteVertexArrays(1, vao, 0);
    }

    private void disposeShader(GL3 gl) {
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
        var gl = glAutoDrawable.getGL().getGL3();
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

        gl.glUseProgram(shaderId);
        spinTriangle(gl);
        drawTriangle(gl);
        gl.glUseProgram(0);
    }

    private void spinTriangle(GL3 gl) {
        // get time since last frame, in seconds.
        double dt = 1.0 / FPS;
        double secondsSinceStart = (System.currentTimeMillis() - startTime) / 1000.0;
        //System.out.println("A "+secondsSinceStart);
        var m =  new Matrix4d();
        m.rotZ(Math.toRadians(secondsSinceStart*90));
        float [] list = {
                (float) m.m00, (float) m.m01, (float) m.m02, (float) m.m03,
                (float) m.m10, (float) m.m11, (float) m.m12, (float) m.m13,
                (float) m.m20, (float) m.m21, (float) m.m22, (float) m.m23,
                (float) m.m30, (float) m.m31, (float) m.m32, (float) m.m33
        };
        int modelLoc = gl.glGetUniformLocation(shaderId, "model");
        gl.glUniformMatrix4fv(modelLoc, 1, false, list, 0);

    }

    private void drawTriangle(GL3 gl) {
        gl.glBindVertexArray(vao[0]);
        gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
        gl.glBindVertexArray(0);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {}
}
