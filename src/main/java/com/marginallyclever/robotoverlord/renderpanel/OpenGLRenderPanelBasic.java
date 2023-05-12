package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.marginallyclever.robotoverlord.*;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class OpenGLRenderPanelBasic implements RenderPanel {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderPanelBasic.class);
    private final JPanel panel = new JPanel(new BorderLayout());
    private final GLJPanel glCanvas;
    private ShaderProgram shaderDefault;
    private final Mesh testTriangle = createTestTriangle();
    private final Viewport viewport = new Viewport();

    int[] myVertexBuffer;

    public OpenGLRenderPanelBasic(EntityManager entityManager, UpdateCallback updateCallback) {
        super();
        logger.info("creating OpenGLRenderPanelBasic");
        glCanvas = createCanvas();

        panel.setMinimumSize(new Dimension(300, 300));
        panel.add(glCanvas, BorderLayout.CENTER);

        addCanvasListeners();
    }

    private void createFragmentShader(GL2 gl2) {
        shaderDefault = new ShaderProgram(gl2,
                readResource("notransform_330.vert"),
                readResource("red_330.frag"));
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
                createFragmentShader(gl2);

                myVertexBuffer = rawSetup(gl2);
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
                viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
                viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
            }

            @Override
            public void dispose( GLAutoDrawable drawable ) {
                GL2 gl2 = drawable.getGL().getGL2();
                rawCleanup(gl2, myVertexBuffer);
                shaderDefault.delete(gl2);
            }

            @Override
            public void display( GLAutoDrawable drawable ) {
                GL2 gl2 = drawable.getGL().getGL2();
                gl2.glClearColor(0, 0, 0, 0);
                gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

                //testRaw(gl2);
                //testRawWithShader(gl2);
                testRawWithShaderAndSetup(gl2);
                //testShaderAndMesh(gl2);
            }

        });
    }

    private void testRawWithShaderAndSetup(GL2 gl2) {
        shaderDefault.use(gl2);
        rawRender(gl2, myVertexBuffer);
        gl2.glUseProgram(0);
    }

    private void testRawWithShader(GL2 gl2) {
        int[] vertexBuffer = rawSetup(gl2);

        shaderDefault.use(gl2);
        rawRender(gl2,vertexBuffer);
        gl2.glUseProgram(0);

        rawCleanup(gl2, vertexBuffer);
    }

    private void testRaw(GL2 gl2) {
        int[] vertexBuffer = rawSetup(gl2);
        rawRender(gl2,vertexBuffer);
        rawCleanup(gl2,vertexBuffer);
    }

    private void rawRender(GL2 gl2,int[] vertexBuffer) {
        gl2.glEnableVertexAttribArray(0);
        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer[0]);
        gl2.glVertexAttribPointer(
                0,                  // attribute 0. No particular reason for 0, but must match the layout in the shader.
                3,                  // size
                GL2.GL_FLOAT,           // type
                false,           // normalized?
                0,                  // stride
                0);                  // array buffer offset
        // Draw the triangle !
        gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, 3); // Starting from vertex 0; 3 vertices total -> 1 triangle
        gl2.glDisableVertexAttribArray(0);
    }

    private void rawCleanup(GL2 gl2, int[] vertexbuffer) {
        gl2.glDeleteBuffers(1,vertexbuffer,0);
    }

    private int[] rawSetup(GL2 gl2) {
        int [] vertexbuffer = new int[1];
        gl2.glGenBuffers(1, vertexbuffer,0);

        FloatBuffer vertexData = FloatBuffer.allocate(3*3);
        vertexData.put(-1.0f);
        vertexData.put(-1.0f);
        vertexData.put(0.0f);
        vertexData.put(1.0f);
        vertexData.put(-1.0f);
        vertexData.put(0.0f);
        vertexData.put(0.0f);
        vertexData.put(1.0f);
        vertexData.put(0.0f);
        vertexData.rewind();

        final int BYTES_PER_FLOAT=(Float.SIZE/8);
        gl2.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexbuffer[0]);
        gl2.glBufferData(GL.GL_ARRAY_BUFFER, 9*BYTES_PER_FLOAT, vertexData, GL.GL_STATIC_DRAW);
        return vertexbuffer;
    }

    private void testShaderAndMesh(GL2 gl2) {
        shaderDefault.use(gl2);
        //shaderDefault.setMatrix4d(gl2,"projectionMatrix",MatrixHelper.orthographicMatrix4d(0,glCanvas.getSurfaceWidth(),0,glCanvas.getSurfaceHeight(),-1,1));
        //shaderDefault.setMatrix4d(gl2,"viewMatrix",viewport.getViewMatrix());
        //shaderDefault.setMatrix4d(gl2,"modelMatrix", MatrixHelper.createIdentityMatrix4());

        testTriangle.render(gl2);
    }

    private Mesh createTestTriangle() {
        Mesh mesh = new Mesh();
        mesh.addVertex(-1.0f, -1.0f, 0.0f);
        mesh.addVertex(1.0f, -1.0f, 0.0f);
        mesh.addVertex(0.0f,  1.0f, 0.0f);
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

    }

    @Override
    public void stopAnimationSystem() {

    }

    @Override
    public void updateSubjects(List<Entity> list) {

    }
}
