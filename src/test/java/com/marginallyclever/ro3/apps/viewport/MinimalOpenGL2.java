package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Use JOGL to open a GLJPanel in a JFrame and render a triangle.
 * Color each corner of the triangle RGB and interpolate the colors across the triangle.
 * Use GL2 (the old fixed-function pipeline) to do this.</p>
 * <p>CC-BY-SA 2025-08-16 Dan Royer (dan@marginallyclever.com)</p>
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "headless environment")
public class MinimalOpenGL2 extends JPanel implements GLEventListener {
    private final FPSAnimator animator;
    private final long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hello World GL2");  // create a JFrame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // make sure to exit when it is closed
        frame.setSize(800, 600);  // set the size of the window
        MinimalOpenGL2 panel = new MinimalOpenGL2();  // create an instance of our panel
        frame.setLocationRelativeTo(null);  // center the frame on the screen
        frame.add(panel);  // add our panel to the frame
        frame.setVisible(true);  // make the frame visible
    }

    public MinimalOpenGL2() {
        super(new BorderLayout());
        GLJPanel glPanel = new GLJPanel(getCapabilities());  // create a GLJPanel with the desired capabilities
        glPanel.addGLEventListener(this);  // add this class as a GLEventListener
        add(glPanel, BorderLayout.CENTER);  // add the GLJPanel to this JPanel
        animator = new FPSAnimator(glPanel, 30);  // create an animator to drive the display() method at 30 FPS
    }

    private GLCapabilities getCapabilities() {
        GLCapabilities capabilities = new GLCapabilities(GLProfile.getGL2ES1());
        capabilities.setHardwareAccelerated(true);  // request hardware acceleration
        capabilities.setDoubleBuffered(true);  // request double buffering
        capabilities.setDepthBits(32);  // request a 32-bit depth buffer
        return capabilities;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        animator.start();  // start the animator when the panel is added to its parent
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        animator.stop();  // stop the animator when the panel is removed from its parent
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL2();
        gl.glClearColor(0.8f,0.8f,0.8f,1);  // light grey background
        gl.setSwapInterval(1);  // enable vsync to prevent screen tearing effect
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {}

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {}

    /**
     * Render one frame of the scene.
     * @param glAutoDrawable the OpenGL drawable to render to.
     */
    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        var gl = glAutoDrawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        spinTriangle(gl);
        drawTriangle(gl);
    }

    private void spinTriangle(GL2 gl2) {
        double seconds = (System.currentTimeMillis()-startTime) / 1000.0;
        gl2.glMatrixMode(GL2.GL_MODELVIEW);  // move the model, not the view or the projection.
        gl2.glLoadIdentity(); // Reset the matrix
        gl2.glRotated(seconds * 90,  // 90 degrees per second
                0, 0, 1);  // rotate around the Z axis
    }

    private void drawTriangle(GL2 gl) {
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glColor3f(1, 0, 0);        gl.glVertex2f(-0.5f, -0.5f); // Red
        gl.glColor3f(0, 1, 0);        gl.glVertex2f( 0.5f, -0.5f); // Green
        gl.glColor3f(0, 0, 1);        gl.glVertex2f( 0.0f,  0.5f); // Blue
        gl.glEnd();
    }
}