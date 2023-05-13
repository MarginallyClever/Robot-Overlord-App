package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.*;
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

public class OpenGLTestOrthographic implements RenderPanel {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLTestOrthographic.class);
    private static final int BYTES_PER_FLOAT=(Float.SIZE/8);
    private final JPanel panel = new JPanel(new BorderLayout());
    protected final GLJPanel glCanvas;
    private ShaderProgram shaderDefault;
    private ShaderProgram shaderTransform;
    private final Mesh testTriangle = createTestTriangle();
    protected final Viewport viewport = new Viewport();
    private int[] myVertexBuffer;
    private int[] myArrayBuffer;
    private final FPSAnimator animator = new FPSAnimator(30);
    private static double time = 0;

    public OpenGLTestOrthographic(EntityManager entityManager, UpdateCallback updateCallback) {
        super();
        logger.info("creating OpenGLRenderPanelBasic");
        glCanvas = createCanvas();

        panel.setMinimumSize(new Dimension(300, 300));
        panel.add(glCanvas, BorderLayout.CENTER);

        addCanvasListeners();
        startAnimationSystem();
    }

    private String [] readResource(String resourceName) {
        List<String> lines = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(resourceName)))) {
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
            GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
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

    private void addCanvasListeners() {
        glCanvas.addGLEventListener(new GLEventListener() {
            @Override
            public void init( GLAutoDrawable drawable ) {
                GL2 gl2 = drawable.getGL().getGL2();

                // turn on vsync
                gl2.setSwapInterval(1);

                // make things pretty
                gl2.glEnable(GL2.GL_LINE_SMOOTH);
                gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
                gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
                // TODO add a settings toggle for this option, it really slows down older machines.
                gl2.glEnable(GL2.GL_MULTISAMPLE);
/*
                // depth testing and culling options
                gl2.glDepthFunc(GL2.GL_LESS);
                gl2.glEnable(GL2.GL_DEPTH_TEST);
                gl2.glDepthMask(true);

                gl2.glEnable(GL2.GL_CULL_FACE);

                gl2.glEnable(GL.GL_STENCIL_TEST);
*/
                // default blending option for transparent materials
                gl2.glEnable(GL2.GL_BLEND);
                gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

                // set the color to use when wiping the draw buffer
                gl2.glClearColor(0.85f,0.85f,0.85f,0.0f);

                createShaderPrograms(gl2);

                myArrayBuffer = rawSetupVAO(gl2);
                myVertexBuffer = rawSetupVBO(gl2);
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
                viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
                viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
            }

            @Override
            public void dispose( GLAutoDrawable drawable ) {
                GL2 gl2 = drawable.getGL().getGL2();
                rawCleanupVBO(gl2, myVertexBuffer);
                rawCleanupVAO(gl2, myArrayBuffer);
                shaderDefault.delete(gl2);
                shaderTransform.delete(gl2);
            }

            @Override
            public void display( GLAutoDrawable drawable ) {
                GL2 gl2 = drawable.getGL().getGL2();
                gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

                //testRaw(gl2);
                //testRawWithShader(gl2);
                //testRawWithShaderAndSetup(gl2);
                //testRawWithShaderAndSetupVAO(gl2);
                testShaderAndMesh(gl2);
            }
        });
    }

    private void testRawWithShaderAndSetupVAO(GL2 gl2) {
        shaderDefault.use(gl2);

        gl2.glEnableVertexAttribArray(0);
        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, myVertexBuffer[0]);
        gl2.glVertexAttribPointer(0,3,GL2.GL_FLOAT,false,0,0);

        gl2.glEnableVertexAttribArray(1);
        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, myVertexBuffer[1]);
        gl2.glVertexAttribPointer(1,4,GL2.GL_FLOAT,false,0,0);

        // Draw the triangle !
        gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, 3);

        gl2.glDisableVertexAttribArray(0);
        gl2.glDisableVertexAttribArray(1);

        gl2.glUseProgram(0);
    }

    private void testRawWithShaderAndSetup(GL2 gl2) {
        shaderDefault.use(gl2);
        rawRender(gl2, myVertexBuffer);
        gl2.glUseProgram(0);
    }

    private void testRawWithShader(GL2 gl2) {
        int[] vertexBuffer = rawSetupVBO(gl2);

        shaderDefault.use(gl2);
        rawRender(gl2,vertexBuffer);
        gl2.glUseProgram(0);

        rawCleanupVBO(gl2, vertexBuffer);
    }

    private void testRaw(GL2 gl2) {
        int[] vertexBuffer = rawSetupVBO(gl2);
        rawRender(gl2,vertexBuffer);
        rawCleanupVBO(gl2,vertexBuffer);
    }

    private void rawRender(GL2 gl2,int[] vertexBuffer) {
        gl2.glEnableVertexAttribArray(0);
        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer[0]);
        gl2.glVertexAttribPointer(0,3,GL2.GL_FLOAT,false,0,0);

        gl2.glEnableVertexAttribArray(1);
        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer[1]);
        gl2.glVertexAttribPointer(1,4,GL2.GL_FLOAT,false,0,0);

        // Draw the triangle !
        gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, 3);

        gl2.glDisableVertexAttribArray(0);
        gl2.glDisableVertexAttribArray(1);
    }

    private int[] rawSetupVAO(GL2 gl2) {
        int [] arrayBuffer = new int[1];
        gl2.glGenVertexArrays(1, arrayBuffer,0);
        gl2.glBindVertexArray(arrayBuffer[0]);
        return arrayBuffer;
    }

    private void rawCleanupVAO(GL2 gl2, int[] arrayBuffer) {
        gl2.glDeleteVertexArrays(arrayBuffer.length,arrayBuffer,0);
    }

    private int[] rawSetupVBO(GL2 gl2) {
        int [] vertexBuffer = new int[2];
        gl2.glGenBuffers(vertexBuffer.length, vertexBuffer,0);

        gl2.glEnableVertexAttribArray(0);
        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer[0]);
        gl2.glVertexAttribPointer(0,3,GL2.GL_FLOAT,false,0,0);
        gl2.glBufferData(GL.GL_ARRAY_BUFFER, 3*3*BYTES_PER_FLOAT, createVertexData(), GL.GL_STATIC_DRAW);

        gl2.glEnableVertexAttribArray(1);
        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer[1]);
        gl2.glVertexAttribPointer(1,4,GL2.GL_FLOAT,false,0,0);
        gl2.glBufferData(GL.GL_ARRAY_BUFFER, 3*4*BYTES_PER_FLOAT, createColorData(), GL.GL_STATIC_DRAW);

        return vertexBuffer;
    }

    private void rawCleanupVBO(GL2 gl2, int[] vertexBuffer) {
        gl2.glDeleteBuffers(vertexBuffer.length,vertexBuffer,0);
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

    private FloatBuffer createColorData() {
        FloatBuffer colorData = FloatBuffer.wrap(new float[]{
                1,0,0,1,
                0,1,0,1,
                0,0,1,1,
        });
        colorData.rewind();
        return colorData;
    }

    private void createShaderPrograms(GL2 gl2) {
        shaderDefault = new ShaderProgram(gl2,
                readResource("notransform_330.vert"),
                readResource("givenColor_330.frag"));
        shaderTransform = new ShaderProgram(gl2,
                readResource("default_330.vert"),
                readResource("givenColor_330.frag"));
    }

    private void testShaderAndMesh(GL2 gl2) {
        //ShaderProgram program = shaderDefault;
        ShaderProgram program = shaderTransform;
        program.use(gl2);

        setProjectionMatrix(gl2, program);
        setViewMatrix(gl2, program);

        // set model matrix
        // slowly rotate the matrix over time.
        time = (double)System.currentTimeMillis() * 0.001;

        Matrix4d modelMatrix = new Matrix4d();
        modelMatrix.rotZ(time * 0.25 * Math.PI);
        modelMatrix.setTranslation(new Vector3d(0,0,0));
        modelMatrix.transpose();
        program.setMatrix4d(gl2,"modelMatrix",modelMatrix);

        testTriangle.render(gl2);
    }

    private void setViewMatrix(GL2 gl2, ShaderProgram program) {
        Matrix4d viewMatrix = MatrixHelper.createIdentityMatrix4();
        viewMatrix.setTranslation(new Vector3d(0,0,-15));
        viewMatrix.transpose();
        program.setMatrix4d(gl2,"viewMatrix",viewMatrix);
    }

    protected void setProjectionMatrix(GL2 gl2, ShaderProgram program) {
        double w = (double)glCanvas.getSurfaceWidth()/2.0;
        double h = (double)glCanvas.getSurfaceHeight()/2.0;
        Matrix4d orthoMatrix = MatrixHelper.orthographicMatrix4d(-w,w,-h,h,-1,1);
        program.setMatrix4d(gl2,"projectionMatrix",orthoMatrix);
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

    public static void main(String[] args) {
        // make a frame
        JFrame frame = new JFrame( OpenGLTestOrthographic.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        OpenGLTestOrthographic opengl = new OpenGLTestOrthographic(null,null);
        frame.setContentPane(opengl.getPanel());
        frame.setPreferredSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.pack();
        frame.setVisible(true);
    }
}
