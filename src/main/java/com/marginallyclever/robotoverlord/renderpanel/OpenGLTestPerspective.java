package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.*;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OpenGLTestPerspective implements RenderPanel {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLTestPerspective.class);
    private static final int BYTES_PER_FLOAT=(Float.SIZE/8);
    private final JPanel panel = new JPanel(new BorderLayout());
    private final GLJPanel glCanvas;
    private ShaderProgram shaderDefault;
    private ShaderProgram shaderTransform;
    private final Mesh testTriangle = createTestTriangle();
    private final Viewport viewport = new Viewport();
    private final FPSAnimator animator = new FPSAnimator(30);
    private static double time = 0;

    public OpenGLTestPerspective(EntityManager entityManager, UpdateCallback updateCallback) {
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

                // default blending option for transparent materials
                gl2.glEnable(GL2.GL_BLEND);
                gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

                // set the color to use when wiping the draw buffer
                gl2.glClearColor(0.85f,0.85f,0.85f,0.0f);

                createShaderPrograms(gl2);
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
                viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
                viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
            }

            @Override
            public void dispose( GLAutoDrawable drawable ) {
                GL2 gl2 = drawable.getGL().getGL2();
                shaderDefault.delete(gl2);
                shaderTransform.delete(gl2);
            }

            @Override
            public void display( GLAutoDrawable drawable ) {
                GL2 gl2 = drawable.getGL().getGL2();
                gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

                testShaderAndMesh(gl2);
            }
        });
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

        Matrix4d ident = MatrixHelper.createIdentityMatrix4();

        double w = (double)glCanvas.getSurfaceWidth()/2.0;
        double h = (double)glCanvas.getSurfaceHeight()/2.0;

        time = (double)System.currentTimeMillis() * 0.001;

        Matrix4d orthoMatrix = MatrixHelper.orthographicMatrix4d(-w,w,-h,h,-1,1);
        Matrix4d projectionMatrix = MatrixHelper.perspectiveMatrix4d(
                45, w/h, 1f, 1000.0f);

        //compareMatrices(gl2);

        Matrix4d viewMatrix = MatrixHelper.createIdentityMatrix4();
        //viewMatrix.set(MatrixHelper.lookAt(new Vector3d(0,0,-5),new Vector3d(0,0,0)));
        viewMatrix.setTranslation(new Vector3d(0,0,-5));
        viewMatrix.invert();

        // slowly rotate the matrix over time.
        Matrix4d modelMatrix = new Matrix4d();
        modelMatrix.rotZ(time * 0.25 * Math.PI);
        modelMatrix.setTranslation(new Vector3d(0,0,-3));
        modelMatrix.transpose();

        program.setMatrix4d(gl2,"projectionMatrix",orthoMatrix);
        program.setMatrix4d(gl2,"viewMatrix",viewMatrix);
        program.setMatrix4d(gl2,"modelMatrix",modelMatrix);

        testTriangle.render(gl2);
    }

    private void compareMatrices(GL2 gl2) {
        Entity cameraEntity = new Entity("Camera");
        CameraComponent camera = new CameraComponent();
        cameraEntity.addComponent(camera);
        viewport.setCamera(camera);
        camera.setOrbitDistance(5);
        viewport.renderChosenProjection(gl2);

        double [] oldProjectionMatrix = new double[16];
        gl2.glGetDoublev(GL2.GL_PROJECTION_MATRIX,oldProjectionMatrix,0);
        double [] oldViewMatrix = new double[16];
        gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX,oldViewMatrix,0);

        gl2.glLoadIdentity();
        Matrix4d modelMatrix = new Matrix4d();
        modelMatrix.rotZ(time * 0.25 * Math.PI);
        modelMatrix.setTranslation(new Vector3d(0,0,3));
        MatrixHelper.applyMatrix(gl2,modelMatrix);
        modelMatrix.transpose();

        double [] oldModelMatrix = new double[16];
        gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX,oldModelMatrix,0);

        double w = glCanvas.getSurfaceWidth();
        double h = glCanvas.getSurfaceHeight();

        Matrix4d projectionMatrix = MatrixHelper.perspectiveMatrix4d(
                60, w/h, 0.1f, 100.0f);

        Matrix4d viewMatrix = MatrixHelper.createIdentityMatrix4();
        viewMatrix.setTranslation(new Vector3d(0,0,-5));
        viewMatrix.invert();

        // good place to put a breakpoint.
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();
        gl2.glMatrixMode(GL2.GL_MODELVIEW_MATRIX);
        gl2.glLoadIdentity();
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
    public void updateSubjects(List<Entity> list) {

    }

    public static void main(String[] args) {
        // make a frame
        JFrame frame = new JFrame( OpenGLTestPerspective.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        OpenGLTestPerspective opengl = new OpenGLTestPerspective(null,null);
        frame.setContentPane(opengl.getPanel());
        frame.setPreferredSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.pack();
        frame.setVisible(true);
    }
}
