package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenGLTestOrthographic implements RenderPanel, GLEventListener, KeyListener {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLTestOrthographic.class);
    private static final int BYTES_PER_FLOAT=(Float.SIZE/8);
    private final JPanel panel = new JPanel(new BorderLayout());
    protected final GLJPanel glCanvas;
    private ShaderProgram shaderNoTransform;
    private ShaderProgram shaderTransform;
    private ShaderProgram shaderDefault;
    private final Mesh testTriangle = createTestTriangle();
    protected final Viewport viewport = new Viewport();
    private int[] myVertexBuffer;
    private int[] myArrayBuffer;
    private final FPSAnimator animator = new FPSAnimator(30);
    private static double time = 0;
    private int testMode = 0;

    public OpenGLTestOrthographic(EntityManager entityManager) {
        super();
        logger.info("creating {}",this.getClass().getName());
        logger.info("\n"+JoglVersion.getInstance().toString());

        glCanvas = createCanvas();
        glCanvas.addGLEventListener(this);
        glCanvas.addKeyListener(this);

        panel.setMinimumSize(new Dimension(300, 300));
        panel.add(glCanvas, BorderLayout.CENTER);

        startAnimationSystem();
    }

    protected String [] readResource(String resourceName) {
        List<String> lines = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getResourceAsStream(resourceName))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines.toArray(new String[0]);
    }

    private GLCapabilities getCapabilities() {
        GLProfile profile = GLProfile.getMaxProgrammable(true);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(true);
        capabilities.setBackgroundOpaque(true);
        capabilities.setDoubleBuffered(true);
        capabilities.setStencilBits(8);
        return capabilities;
    }

    private GLJPanel createCanvas() {
        GLJPanel canvas = null;
        try {
            GLCapabilities capabilities = getCapabilities();
            StringBuilder sb = new StringBuilder();
            capabilities.toString(sb);
            logger.info("capabilities="+sb);

            canvas = new GLJPanel(capabilities);
        } catch(GLException e) {
            logger.error("Failed to get/set Capabilities.  Are your native drivers missing?");
        }
        return canvas;
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        GL3 gl = drawable.getGL().getGL3();

        // turn on vsync
        gl.setSwapInterval(1);

        // make things pretty
        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        // TODO add a settings toggle for this option, it really slows down older machines.
        gl.glEnable(GL3.GL_MULTISAMPLE);
/*
        // depth testing and culling options
        gl.glDepthFunc(GL3.GL_LESS);
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthMask(true);

        gl.glEnable(GL3.GL_CULL_FACE);

        gl.glEnable(GL.GL_STENCIL_TEST);
*/
        // default blending option for transparent materials
        gl.glEnable(GL3.GL_BLEND);
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

        // set the color to use when wiping the draw buffer
        gl.glClearColor(0.85f,0.85f,0.85f,0.0f);

        createShaderPrograms(gl);

        myArrayBuffer = rawSetupVAO(gl);
        myVertexBuffer = rawSetupVBO(gl);
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
        viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
    }

    @Override
    public void dispose( GLAutoDrawable drawable ) {
        GL3 gl = drawable.getGL().getGL3();
        rawCleanupVBO(gl, myVertexBuffer);
        rawCleanupVAO(gl, myArrayBuffer);
        shaderDefault.delete(gl);
        shaderTransform.delete(gl);
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        switch (testMode) {
            case 0 -> testRaw(gl);
            case 1 -> testRawWithShader(gl);
            case 2 -> testRawWithShaderAndSetup(gl);
            case 3 -> testRawWithShaderAndSetupVAO(gl);
            case 4 -> testShaderAndMesh(gl, shaderDefault);
            case 5 -> testShaderAndMesh(gl, shaderTransform);
        }
    }

    private void testRawWithShaderAndSetupVAO(GL3 gl) {
        gl.glBindVertexArray(myArrayBuffer[0]);

        shaderDefault.use(gl);
        rawRender(gl, myVertexBuffer);
        gl.glUseProgram(0);

        gl.glBindVertexArray(0); // Unbind the VAO
    }

    private void testRawWithShaderAndSetup(GL3 gl) {
        shaderDefault.use(gl);
        rawRender(gl, myVertexBuffer);
        gl.glUseProgram(0);
    }

    private void testRawWithShader(GL3 gl) {
        int[] vertexBuffer = rawSetupVBO(gl);

        shaderDefault.use(gl);
        setProjectionMatrix(gl, shaderDefault);
        setViewMatrix(gl, shaderDefault);
        setModelviewMatrix(gl, shaderDefault);

        rawRender(gl,vertexBuffer);
        gl.glUseProgram(0);

        rawCleanupVBO(gl, vertexBuffer);
    }

    private void testRaw(GL3 gl) {
        int[] vertexBuffer = rawSetupVBO(gl);
        rawRender(gl,vertexBuffer);
        rawCleanupVBO(gl,vertexBuffer);
    }

    private void rawRender(GL3 gl,int[] vertexBuffer) {
        enableAndBind(gl,0,3,vertexBuffer);
        enableAndBind(gl,1,3,vertexBuffer);
        enableAndBind(gl,2,4,vertexBuffer);

        // Draw the triangle !
        gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);

        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);
        gl.glDisableVertexAttribArray(2);
    }

    private int[] rawSetupVAO(GL3 gl) {
        logger.debug("rawSetupVAO");
        int [] arrayBuffer = new int[1];
        gl.glGenVertexArrays(1, arrayBuffer,0);
        OpenGLHelper.checkGLError(gl,logger);
        gl.glBindVertexArray(arrayBuffer[0]);
        OpenGLHelper.checkGLError(gl,logger);
        return arrayBuffer;
    }

    private void rawCleanupVAO(GL3 gl, int[] arrayBuffer) {
        gl.glDeleteVertexArrays(arrayBuffer.length,arrayBuffer,0);
    }

    private int[] rawSetupVBO(GL3 gl) {
        logger.debug("rawSetupVBO");
        int [] vertexBuffer = new int[3];
        gl.glGenBuffers(vertexBuffer.length, vertexBuffer,0);

        createBuffer(gl,0,3,vertexBuffer,createVertexData());
        createBuffer(gl,1,3,vertexBuffer,createNormalData());
        createBuffer(gl,2,4,vertexBuffer,createColorData());

        return vertexBuffer;
    }

    private void enableAndBind(GL3 gl, int bufferID, int size, int[] vertexBuffer) {
        gl.glEnableVertexAttribArray(bufferID);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer[bufferID]);
        gl.glVertexAttribPointer(bufferID,size,GL3.GL_FLOAT,false,0,0);
    }

    private void createBuffer(GL3 gl, int bufferID,int size,int [] vertexBuffer,FloatBuffer source) {
        enableAndBind(gl, bufferID, size, vertexBuffer);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) size *3*BYTES_PER_FLOAT, source, GL.GL_STATIC_DRAW);
    }

    private void rawCleanupVBO(GL3 gl, int[] vertexBuffer) {
        gl.glDeleteBuffers(vertexBuffer.length,vertexBuffer,0);
    }

    private FloatBuffer createVertexData() {
        FloatBuffer vertexData = FloatBuffer.wrap(new float[]{
                -1f,-1f,0f,
                1f,-1f,0f,
                0f, 1f,0f,
        });
        vertexData.rewind();
        return vertexData;
    }

    private FloatBuffer createNormalData() {
        FloatBuffer normalData = FloatBuffer.wrap(new float[]{
                0,0,1,
                0,0,1,
                0,0,1,
        });
        normalData.rewind();
        return normalData;
    }

    private FloatBuffer createColorData() {
        FloatBuffer colorData = FloatBuffer.wrap(new float[]{
                1,0,0,1,
                0,1,0,1,
                0,0,1,1,
        });
        colorData.rewind();
        return colorData;
    }

    private void createShaderPrograms(GL3 gl) {
        System.out.println("Create shader programs");
        shaderDefault = new ShaderProgram(gl,
                readResource("default_330.vert"),
                readResource("givenColor_330.frag"));
        shaderNoTransform = new ShaderProgram(gl,
                readResource("noTransform_330.vert"),
                readResource("givenColor_330.frag"));
        shaderTransform = new ShaderProgram(gl,
                readResource("default_330.vert"),
                readResource("givenColor_330.frag"));
    }

    private void testShaderAndMesh(GL3 gl,ShaderProgram program) {
        program.use(gl);

        setProjectionMatrix(gl, program);
        setViewMatrix(gl, program);
        setModelviewMatrix(gl, program);

        testTriangle.render(gl);
    }

    protected void setModelviewMatrix(GL3 gl, ShaderProgram program) {
        // set model matrix
        // slowly rotate the matrix over time.
        time = (double)System.currentTimeMillis() * 0.001;

        Matrix4d modelMatrix = new Matrix4d();
        modelMatrix.rotZ(time * 0.25 * Math.PI);
        modelMatrix.setTranslation(new Vector3d(0,0,0));
        modelMatrix.transpose();
        program.setMatrix4d(gl,"modelMatrix",modelMatrix);
    }

    protected void setViewMatrix(GL3 gl, ShaderProgram program) {
        Matrix4d viewMatrix = MatrixHelper.createIdentityMatrix4();
        viewMatrix.setTranslation(new Vector3d(0,0,-15));
        viewMatrix.transpose();
        program.setMatrix4d(gl,"viewMatrix",viewMatrix);
    }

    protected void setProjectionMatrix(GL3 gl, ShaderProgram program) {
        double w = (double)glCanvas.getSurfaceWidth()/2.0;
        double h = (double)glCanvas.getSurfaceHeight()/2.0;
        Matrix4d orthoMatrix = MatrixHelper.orthographicMatrix4d(-w,w,-h,h,-1,100);
        program.setMatrix4d(gl,"projectionMatrix",orthoMatrix);
    }

    private Mesh createTestTriangle() {
        Mesh mesh = new Mesh();
        mesh.addVertex(-10.0f, -10.0f, 0.0f);
        mesh.addVertex(10.0f, -10.0f, 0.0f);
        mesh.addVertex(0.0f,  10.0f, 0.0f);
        mesh.addColor(1,0,0,1);
        mesh.addColor(0,1,0,1);
        mesh.addColor(0,0,1,1);
        mesh.addNormal(0,0,1);
        mesh.addNormal(0,0,1);
        mesh.addNormal(0,0,1);
        return mesh;
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void startAnimationSystem() {
        animator.add(glCanvas);
        animator.start();
    }

    @Override
    public void stopAnimationSystem() {
        animator.stop();
    }

    @Override
    public void updateSubjects(List<Entity> list) {}

    @Override
    public void setUpdateCallback(UpdateCallback updateCallback) {}

    public static void main(String[] args) {
        // make a frame
        JFrame frame = new JFrame( OpenGLTestOrthographic.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        OpenGLTestOrthographic opengl = new OpenGLTestOrthographic(null);
        frame.setContentPane(opengl.getPanel());
        frame.setPreferredSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
            case KeyEvent.VK_1 -> testMode = 0;
            case KeyEvent.VK_2 -> testMode = 1;
            case KeyEvent.VK_3 -> testMode = 2;
            case KeyEvent.VK_4 -> testMode = 3;
            case KeyEvent.VK_5 -> testMode = 4;
            case KeyEvent.VK_6 -> testMode = 5;
        }
        System.out.println("testMode="+testMode);
    }

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
