package com.marginallyclever.ro3.render;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.DockingPanel;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.MeshInstance;
import com.marginallyclever.robotoverlord.preferences.GraphicsPreferences;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.Objects;

/**
 * {@link OpenGLPanel} is a {@link DockingPanel} that contains a {@link GLJPanel} and an {@link FPSAnimator}.
 */
public class OpenGLPanel extends JPanel implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLPanel.class);
    protected GLJPanel glCanvas;
    protected int canvasWidth, canvasHeight;
    private final FPSAnimator animator = new FPSAnimator(GraphicsPreferences.framesPerSecond.get());
    private final List<GLEventListener> listeners = new ArrayList<>();

    public OpenGLPanel() {
        super(new BorderLayout());

        try {
            logger.info("availability="+ GLProfile.glAvailabilityToString());
            GLCapabilities capabilities = getCapabilities();
            logger.info("create canvas");
            glCanvas = new GLJPanel(capabilities);
        } catch(GLException e) {
            logger.error("Failed to create canvas.  Are your native drivers missing?");
        }
        add(glCanvas, BorderLayout.CENTER);
        animator.add(glCanvas);
        animator.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addGLEventListener(this);
        glCanvas.addMouseListener(this);
        glCanvas.addMouseMotionListener(this);
        glCanvas.addMouseWheelListener(this);
        glCanvas.addKeyListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        removeGLEventListener(this);
        glCanvas.removeMouseListener(this);
        glCanvas.removeMouseMotionListener(this);
        glCanvas.removeMouseWheelListener(this);
        glCanvas.removeKeyListener(this);
    }

    private GLCapabilities getCapabilities() {
        GLProfile profile = GLProfile.getMaxProgrammable(true);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(GraphicsPreferences.hardwareAccelerated.get());
        capabilities.setBackgroundOpaque(GraphicsPreferences.backgroundOpaque.get());
        capabilities.setDoubleBuffered(GraphicsPreferences.doubleBuffered.get());
        capabilities.setStencilBits(8);
        int fsaa = GraphicsPreferences.fsaaSamples.get();
        if(fsaa>0) {
            capabilities.setSampleBuffers(true);
            capabilities.setNumSamples(1<<fsaa);
        }
        StringBuilder sb = new StringBuilder();
        capabilities.toString(sb);
        logger.info("capabilities="+sb);
        return capabilities;
    }

    public void addGLEventListener(GLEventListener listener) {
        listeners.add(listener);
        glCanvas.addGLEventListener(listener);
    }

    public void removeGLEventListener(GLEventListener listener) {
        glCanvas.removeGLEventListener(listener);
        listeners.remove(listener);
    }

    public int getGLEventListenersCount() {
        return listeners.size();
    }

    public GLEventListener getGLEventListener(int index) {
        return listeners.get(index);
    }

    public void stopAnimationSystem() {
        animator.stop();
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        logger.info("init");

        GL3 gl3 = glAutoDrawable.getGL().getGL3();

        // turn on vsync
        gl3.setSwapInterval(GraphicsPreferences.verticalSync.get() ? 1 : 0);

        // make things pretty
        gl3.glEnable(GL3.GL_LINE_SMOOTH);
        gl3.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl3.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        // depth testing and culling options
        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glDepthFunc(GL3.GL_LESS);
        gl3.glDepthMask(true);
        // Don't draw triangles facing away from camera
        gl3.glEnable(GL3.GL_CULL_FACE);
        gl3.glCullFace(GL3.GL_BACK);
        // default blending option for transparent materials
        gl3.glEnable(GL3.GL_BLEND);
        gl3.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

        gl3.glActiveTexture(GL3.GL_TEXTURE0);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        logger.info("dispose");
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        unloadAllMeshes(gl3);
        Registry.textureFactory.unloadAll();
    }

    private void unloadAllMeshes(GL3 gl3) {
        List<Node> toScan = new ArrayList<>(Registry.getScene().getChildren());
        while(!toScan.isEmpty()) {
            Node node = toScan.remove(0);

            if(node instanceof MeshInstance meshInstance) {
                Mesh mesh = meshInstance.getMesh();
                if(mesh==null) continue;
                mesh.unload(gl3);
            }

            toScan.addAll(node.getChildren());
        }
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        logger.debug("reshape {}x{}",width,height);
        canvasWidth = width;
        canvasHeight = height;
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {}

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("keyTyped "+e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("keyPressed "+e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        System.out.println("keyReleased "+e);
    }
}
