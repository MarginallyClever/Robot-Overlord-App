package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.ro3.shader.Shader;
import com.marginallyclever.ro3.shader.ShaderProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Simple test of a shader program with vertex and fragment shaders.
 */
public class ShaderProgramTest extends JPanel implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ShaderProgramTest.class);
    private static final long startTime = System.currentTimeMillis();

    private final GLJPanel glPanel;
    private final FPSAnimator animator;

    private static boolean HARDWARE_ACCELERATED = true;
    private static boolean DOUBLE_BUFFERED = true;
    private static final int FSAA_SAMPLES = 2;
    private static final int FPS = 60;

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
    // connects the matrix on the CPU to the 'model' matrix in the shader script.
    private Shader vertexShader;
    private Shader fragmentShader;
    private ShaderProgram shaderProgram;
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
        logger.info("start time "+startTime);
        // create a JFrame, add a JHelloWorldGL2 to it, and make it visible.
        JFrame frame = new JFrame("Hello World GL3");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        var panel = new ShaderProgramTest();
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);
    }

    public ShaderProgramTest() {
        super();
        var capabilities = getCapabilities();
        glPanel = new GLJPanel(capabilities);
        this.setLayout(new BorderLayout());
        this.add(glPanel, BorderLayout.CENTER);
        animator = new FPSAnimator(glPanel, FPS);
    }

    private GLCapabilities getCapabilities() {
        GLProfile profile = GLProfile.get(GLProfile.GL3bc);
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
        animator.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if(glPanel!=null) glPanel.removeGLEventListener(this);
        animator.stop();
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

        // enable vsync to prevent screen tearing effect
        gl.setSwapInterval(1);

        gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
        gl.glEnable(GL3.GL_LINE_SMOOTH);

        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);

        gl.glEnable(GL3.GL_BLEND);
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void initShader(GL3 gl) {
        vertexShader = new Shader(GL3.GL_VERTEX_SHADER, vertexCode,"vertex");
        fragmentShader = new Shader(GL3.GL_FRAGMENT_SHADER, fragmentCode,"fragment");
        var list = new ArrayList<Shader>();
        list.add(vertexShader);
        list.add(fragmentShader);
        shaderProgram = new ShaderProgram(list);

        shaderProgram.use(gl);

        matrixId = gl.glGetUniformLocation(shaderProgram.getProgramId(), "model");
    }

    private void initMesh(GL3 gl) {
        createBuffers(gl);
        updateBuffers(gl);
    }

    private void updateBuffers(GL3 gl) {
        // put the vertices and colors into the vbo.
        gl.glBindVertexArray(vao[0]);
        OpenGLHelper.checkGLError(gl,logger);

        setupOneBuffer(gl,0, VERTEX_COMPONENTS, vertices);
        setupOneBuffer(gl,1, COLOR_COMPONENTS, colors  );

        gl.glBindVertexArray(0);
    }

    private void bindOneBuffer(GL3 gl, int attribIndex) {
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[attribIndex]);
        OpenGLHelper.checkGLError(gl,logger);
    }

    private void setupOneBuffer(GL3 gl, int attribIndex, int size, float [] data) {
        bindOneBuffer(gl, attribIndex);
        OpenGLHelper.checkGLError(gl,logger);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, (long) data.length * Float.BYTES, FloatBuffer.wrap(data), GL3.GL_STATIC_DRAW);
        OpenGLHelper.checkGLError(gl,logger);
        gl.glVertexAttribPointer(attribIndex,size,GL3.GL_FLOAT,false,0,0);
        OpenGLHelper.checkGLError(gl,logger);
        gl.glEnableVertexAttribArray(attribIndex);
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

    public static void checkGLError(GL3 GL3) {
        int err = GL3.glGetError();
        if(err != GL.GL_NO_ERROR) {
            GLU glu = GLU.createGLU(GL3);
            logger.error("GL error {}: {}", err, glu.gluErrorString(err));
        }
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL3();

        disposeMesh(gl);
        disposeShader(gl);
        gl.glFinish(); // Ensure all OpenGL commands are completed before disposing
        logger.info("OpenGL resources disposed.");
    }

    private void disposeMesh(GL3 gl) {
        gl.glDeleteBuffers(NUM_BUFFERS, vbo, 0);
        gl.glDeleteVertexArrays(1, vao, 0);
    }

    private void disposeShader(GL3 gl) {
        shaderProgram.unload(gl);
    }

    /**
     * Render one frame of the scene.
     * @param glAutoDrawable the OpenGL drawable to render to.
     */
    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL3();
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

        shaderProgram.use(gl);
        spinTriangle(gl);
        drawTriangle(gl);
        gl.glUseProgram(0);
    }

    private void spinTriangle(GL3 gl) {
        // get time since last frame, in seconds.
        double dt = 1.0 / FPS;
        double secondsSinceStart = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("A " + secondsSinceStart);

        var m = new Matrix4d();
        m.rotZ(Math.toRadians(secondsSinceStart * 90));
        uploadMatrixToCurrentShader(gl,m);
    }

    // assumes matrixId is still valid
    private void uploadMatrixToCurrentShader(GL3 gl, Matrix4d m) {
        float [] list = {
                (float) m.m00, (float) m.m10, (float) m.m20, (float) m.m30,
                (float) m.m01, (float) m.m11, (float) m.m21, (float) m.m31,
                (float) m.m02, (float) m.m12, (float) m.m22, (float) m.m32,
                (float) m.m03, (float) m.m13, (float) m.m23, (float) m.m33
        };
        gl.glUniformMatrix4fv(matrixId, 1, false, list, 0);
    }


    private void drawTriangle(GL3 gl) {
        gl.glBindVertexArray(vao[0]);
        gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
        gl.glBindVertexArray(0);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {}
}
