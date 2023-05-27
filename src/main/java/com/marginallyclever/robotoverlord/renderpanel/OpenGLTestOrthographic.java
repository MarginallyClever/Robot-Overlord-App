package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.MatrixHelper;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenGLTestOrthographic implements RenderPanel, GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLTestOrthographic.class);
    private static final int BYTES_PER_FLOAT=(Float.SIZE/8);
    private final JPanel panel = new JPanel(new BorderLayout());
    protected final GLJPanel glCanvas;
    private ShaderProgram shaderNoTransform;
    private ShaderProgram shaderDefault;
    private final Mesh testTriangle = createTestTriangle();
    protected final Viewport viewport = new Viewport();
    private int[] myVertexBuffer;
    private int[] myArrayBuffer;
    private final FPSAnimator animator = new FPSAnimator(30);
    private static double time = 0;

    public OpenGLTestOrthographic(EntityManager entityManager) {
        super();
        logger.info("creating OpenGLRenderPanelBasic");
        glCanvas = createCanvas();

        panel.setMinimumSize(new Dimension(300, 300));
        panel.add(glCanvas, BorderLayout.CENTER);

        glCanvas.addGLEventListener(this);
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

    private GLJPanel createCanvas() {
        GLJPanel canvas = null;
        try {
            logger.info("...get default caps");
            GLCapabilities caps = new GLCapabilities(GLProfile.getMaximum(true));
            caps.setBackgroundOpaque(true);
            caps.setDoubleBuffered(true);
            caps.setHardwareAccelerated(true);
            caps.setStencilBits(8);
            StringBuilder sb = new StringBuilder();
            caps.toString(sb);
            logger.info("...set caps to "+sb.toString());
            logger.info("...create canvas");
            canvas = new GLJPanel(caps);
        } catch(GLException e) {
            logger.error("Failed to get/set Capabilities.  Are your native drivers missing?");
        }
        return canvas;
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        GL3 gl3 = drawable.getGL().getGL3();

        // turn on vsync
        gl3.setSwapInterval(1);

        // make things pretty
        gl3.glEnable(GL3.GL_LINE_SMOOTH);
        gl3.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl3.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        // TODO add a settings toggle for this option, it really slows down older machines.
        gl3.glEnable(GL3.GL_MULTISAMPLE);
/*
        // depth testing and culling options
        gl3.glDepthFunc(GL3.GL_LESS);
        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glDepthMask(true);

        gl3.glEnable(GL3.GL_CULL_FACE);

        gl3.glEnable(GL.GL_STENCIL_TEST);
*/
        // default blending option for transparent materials
        gl3.glEnable(GL3.GL_BLEND);
        gl3.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

        // set the color to use when wiping the draw buffer
        gl3.glClearColor(0.85f,0.85f,0.85f,0.0f);

        createShaderPrograms(gl3);

        myArrayBuffer = rawSetupVAO(gl3);
        rawSetupVBO(gl3);
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
        viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
    }

    @Override
    public void dispose( GLAutoDrawable drawable ) {
        GL3 gl3 = drawable.getGL().getGL3();
        rawCleanupVBO(gl3, myVertexBuffer);
        rawCleanupVAO(gl3, myArrayBuffer);
        shaderNoTransform.delete(gl3);
        shaderDefault.delete(gl3);
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        GL3 gl3 = drawable.getGL().getGL3();
        gl3.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        //testRaw(gl3);
        //testRawWithShader(gl3);
        //testRawWithShaderAndSetup(gl3);
        //testRawWithShaderAndSetupVAO(gl3);
        //testShaderAndMesh(gl3,shaderNoTransform);
        testShaderAndMesh(gl3,shaderDefault);
    }

    private void testRawWithShaderAndSetupVAO(GL3 gl3) {
        shaderNoTransform.use(gl3);

        setVertexBuffer(gl3,0,3);
        setVertexBuffer(gl3,1,3);
        setVertexBuffer(gl3,2,4);

        // Draw the triangle !
        gl3.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);

        gl3.glDisableVertexAttribArray(0);
        gl3.glDisableVertexAttribArray(1);
        gl3.glDisableVertexAttribArray(2);

        gl3.glUseProgram(0);
    }

    private void testRawWithShaderAndSetup(GL3 gl3) {
        shaderNoTransform.use(gl3);
        rawRender(gl3, myVertexBuffer);
        gl3.glUseProgram(0);
    }

    private void testRawWithShader(GL3 gl3) {
        rawSetupVBO(gl3);

        shaderNoTransform.use(gl3);
        rawRender(gl3,myVertexBuffer);
        gl3.glUseProgram(0);

        rawCleanupVBO(gl3, myVertexBuffer);
    }

    private void testRaw(GL3 gl3) {
        rawSetupVBO(gl3);
        rawRender(gl3,myVertexBuffer);
        rawCleanupVBO(gl3,myVertexBuffer);
    }

    private void rawRender(GL3 gl3,int[] vertexBuffer) {
        setVertexBuffer(gl3,0,3);
        setVertexBuffer(gl3,1,3);
        setVertexBuffer(gl3,2,4);

        // Draw the triangle !
        gl3.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);

        gl3.glDisableVertexAttribArray(0);
        gl3.glDisableVertexAttribArray(1);
        gl3.glDisableVertexAttribArray(2);
    }

    private int[] rawSetupVAO(GL3 gl3) {
        int [] arrayBuffer = new int[1];
        gl3.glGenVertexArrays(1, arrayBuffer,0);
        gl3.glBindVertexArray(arrayBuffer[0]);
        return arrayBuffer;
    }

    private void rawCleanupVAO(GL3 gl3, int[] arrayBuffer) {
        gl3.glDeleteVertexArrays(arrayBuffer.length,arrayBuffer,0);
    }

    private void rawSetupVBO(GL3 gl3) {
        myVertexBuffer = new int[3];
        gl3.glGenBuffers(myVertexBuffer.length, myVertexBuffer,0);

        // vertexes
        setVertexBuffer(gl3,0,3);
        gl3.glBufferData(GL.GL_ARRAY_BUFFER, 3*3*BYTES_PER_FLOAT, createVertexData(), GL.GL_STATIC_DRAW);

        // normals
        setVertexBuffer(gl3,1,3);
        gl3.glBufferData(GL.GL_ARRAY_BUFFER, 3*3*BYTES_PER_FLOAT, createNormalData(), GL.GL_STATIC_DRAW);

        // colors
        setVertexBuffer(gl3,2,4);
        gl3.glBufferData(GL.GL_ARRAY_BUFFER, 4*3*BYTES_PER_FLOAT, createColorData(), GL.GL_STATIC_DRAW);
    }

    void setVertexBuffer(GL3 gl3, int index, int size) {
        gl3.glEnableVertexAttribArray(index);
        gl3.glBindBuffer(GL.GL_ARRAY_BUFFER, myVertexBuffer[index]);
        gl3.glVertexAttribPointer(index,size,GL3.GL_FLOAT,false,0,0);
    }

    private void rawCleanupVBO(GL3 gl3, int[] vertexBuffer) {
        gl3.glDeleteBuffers(vertexBuffer.length,vertexBuffer,0);
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
        FloatBuffer vertexData = FloatBuffer.wrap(new float[]{
                0,0,1,
                0,0,1,
                0,0,1,
        });
        vertexData.rewind();
        return vertexData;
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

    private void createShaderPrograms(GL3 gl3) {
        shaderNoTransform = new ShaderProgram(gl3,
                readResource("notransform_330.vert"),
                readResource("givenColor_330.frag"));
        shaderDefault = new ShaderProgram(gl3,
                readResource("default_330.vert"),
                readResource("givenColor_330.frag"));
    }

    private void testShaderAndMesh(GL3 gl3,ShaderProgram program) {
        program.use(gl3);

        setProjectionMatrix(gl3, program);
        setViewMatrix(gl3, program);

        // set model matrix
        // slowly rotate the matrix over time.
        time = (double)System.currentTimeMillis() * 0.001;

        Matrix4d modelMatrix = new Matrix4d();
        modelMatrix.rotZ(time * 0.25 * Math.PI);
        modelMatrix.setTranslation(new Vector3d(0,0,0));
        modelMatrix.transpose();
        program.setMatrix4d(gl3,"modelMatrix",modelMatrix);

        testTriangle.render(gl3);
    }

    private void setViewMatrix(GL3 gl3, ShaderProgram program) {
        Matrix4d viewMatrix = MatrixHelper.createIdentityMatrix4();
        viewMatrix.setTranslation(new Vector3d(0,0,-15));
        viewMatrix.transpose();
        program.setMatrix4d(gl3,"viewMatrix",viewMatrix);
    }

    protected void setProjectionMatrix(GL3 gl3, ShaderProgram program) {
        double w = glCanvas.getSurfaceWidth()/2.0;
        double h = glCanvas.getSurfaceHeight()/2.0;
        Matrix4d orthoMatrix = MatrixHelper.orthographicMatrix4d(-w,w,-h,h,10,20);
        program.setMatrix4d(gl3,"projectionMatrix",orthoMatrix);
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
}
