package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * <p>Use JOGL to open a GLJPanel in a JFrame and render a triangle.
 * Color each corner of the triangle RGB and interpolate the colors across the triangle.
 * Use GL2 (the old fixed-function pipeline) to do this.</p>
 * <p>CC-BY-SA 2025-08-16 Dan Royer (dan@marginallyclever.com)</p>
 */
public class MinimalOpenGL2 extends JPanel implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(MinimalOpenGL2.class);
    private final GLJPanel glPanel;
    private final FPSAnimator animator;
    private int displayWidth = 800;
    private int displayHeight = 600;

    private static boolean HARDWARE_ACCELERATED = true;
    private static boolean DOUBLE_BUFFERED = true;
    private static final int FSAA_SAMPLES = 2;
    private static final int FPS = 30;

    private static final long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        logger.info("start time "+startTime);
        // create a JFrame, add a JHelloWorldGL2 to it, and make it visible.
        JFrame frame = new JFrame("Hello World GL2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        MinimalOpenGL2 panel = new MinimalOpenGL2();
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);
    }

    public MinimalOpenGL2() {
        super();
        var capabilities = getCapabilities();
        glPanel = new GLJPanel(capabilities);
        glPanel.addGLEventListener(this);
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
    public void init(GLAutoDrawable glAutoDrawable) {
        var gl2 = glAutoDrawable.getGL().getGL2();
        gl2.glClearColor(0.8f,0.8f,0.8f,1);

        gl2.glHint(GL2.GL_LINE_SMOOTH, GL2.GL_NICEST);
        gl2.glEnable(GL2.GL_LINE_SMOOTH);
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        var gl2 = glAutoDrawable.getGL().getGL2();
        animator.stop();
        gl2.glFinish(); // Ensure all OpenGL commands are completed before disposing
        logger.info("OpenGL resources disposed.");
    }

    /**
     * Render one frame of the scene.
     * @param glAutoDrawable the OpenGL drawable to render to.
     */
    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        var gl2 = glAutoDrawable.getGL().getGL2();
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);

        spinTriangle(gl2);
        drawTriangle(gl2);
    }

    private void spinTriangle(GL2 gl2) {
        // get time since last frame, in seconds.
        double dt = 1.0 / FPS;
        double secondsSinceStart = (System.currentTimeMillis() - startTime) / 1000.0;

        System.out.println("A "+secondsSinceStart);
        // rotate the triangle around the Z axis
        //gl2.glPushMatrix();
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity(); // Reset the modelview matrix
        gl2.glRotated(secondsSinceStart * 90, 0, 0, 1);
        // draw the triangle again with the new rotation
    }

    private void drawTriangle(GL2 gl2) {
        gl2.glBegin(GL2.GL_TRIANGLES);
        gl2.glColor3f(1, 0, 0);        gl2.glVertex2f(-0.5f, -0.5f); // Red
        gl2.glColor3f(0, 1, 0);        gl2.glVertex2f( 0.5f, -0.5f); // Green
        gl2.glColor3f(0, 0, 1);        gl2.glVertex2f( 0.0f,  0.5f); // Blue
        gl2.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        //var gl2 = glAutoDrawable.getGL().getGL2();
        displayWidth = width;
        displayHeight = height;
    }
}
