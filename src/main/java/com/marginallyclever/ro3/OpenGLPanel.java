package com.marginallyclever.ro3;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.robotoverlord.preferences.GraphicsPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * {@link OpenGLPanel} is a {@link DockingPanel} that contains a {@link GLJPanel}.
 */
public class OpenGLPanel extends DockingPanel implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLPanel.class);
    private final JToolBar toolBar = new JToolBar();
    private GLJPanel glCanvas;
    private final FPSAnimator animator = new FPSAnimator(GraphicsPreferences.framesPerSecond.get());

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

        add(toolBar, BorderLayout.NORTH);
        add(glCanvas, BorderLayout.CENTER);
        animator.add(glCanvas);
        animator.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        glCanvas.addGLEventListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        glCanvas.removeGLEventListener(this);
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {}

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        glAutoDrawable.getGL().glClearColor(0,0,0,1);
        glAutoDrawable.getGL().glClear(GL.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {}

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

    public void stopAnimationSystem() {
        animator.stop();
    }
}
