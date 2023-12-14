package com.marginallyclever.ro3.render;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.ro3.DockingPanel;
import com.marginallyclever.robotoverlord.preferences.GraphicsPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

/**
 * {@link OpenGLPanel} is a {@link DockingPanel} that contains a {@link GLJPanel} and an {@link FPSAnimator}.
 */
public class OpenGLPanel extends DockingPanel implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLPanel.class);
    private GLJPanel glCanvas;
    private final FPSAnimator animator = new FPSAnimator(GraphicsPreferences.framesPerSecond.get());
    private final List<GLEventListener> listeners = new ArrayList<>();

    public OpenGLPanel() {
        this("OpenGL");
    }

    public OpenGLPanel(String tabText) {
        super(tabText);
        setLayout(new BorderLayout());

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
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        removeGLEventListener(this);
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
    public void init(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {}

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        eraseBackground();
    }

    private void eraseBackground() {
        GL3 gl = GLContext.getCurrentGL().getGL3();
        gl.glClearColor(0.25f,0.25f,0.5f,1);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    }
}
