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

public class OpenGLTest implements RenderPanel {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLTest.class);
    private final JPanel panel = new JPanel(new BorderLayout());
    protected final GLJPanel glCanvas = createCanvas();
    private final FPSAnimator animator = new FPSAnimator(30);

    public OpenGLTest() {
        super();
        logger.info("creating {}",this.getClass().getName());
        logger.info("\n"+JoglVersion.getInstance().toString());

        panel.setMinimumSize(new Dimension(300, 300));
        panel.add(glCanvas, BorderLayout.CENTER);

        startAnimationSystem();
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
        JFrame frame = new JFrame( OpenGLTest.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        OpenGLTest opengl = new OpenGLTest();
        frame.setContentPane(opengl.getPanel());
        frame.setPreferredSize(new Dimension(600,600));
        frame.setSize(600,600);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
