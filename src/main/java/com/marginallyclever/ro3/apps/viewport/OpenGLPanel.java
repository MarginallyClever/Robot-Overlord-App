package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * {@link OpenGLPanel} manages a {@link GLJPanel} and an {@link FPSAnimator}.
 */
public class OpenGLPanel extends App implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLPanel.class);
    protected GLJPanel glCanvas;
    private boolean hardwareAccelerated = true;
    private boolean doubleBuffered = true;
    private int fsaaSamples = 2;
    private boolean verticalSync = true;
    private int fps = 30;
    private final FPSAnimator animator = new FPSAnimator(fps);

    public OpenGLPanel() {
        super(new BorderLayout());

        loadPrefs();

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

    private void loadPrefs() {
        Preferences pref = Preferences.userNodeForPackage(this.getClass());
        hardwareAccelerated = pref.getBoolean("hardwareAccelerated",true);
        doubleBuffered = pref.getBoolean("doubleBuffered",true);
        fsaaSamples = pref.getInt("fsaaSamples",2);
        verticalSync = pref.getBoolean("verticalSync",true);
        fps = pref.getInt("fps",30);
    }

    public void savePrefs() {
        Preferences pref = Preferences.userNodeForPackage(this.getClass());
        pref.putBoolean("hardwareAccelerated",hardwareAccelerated);
        pref.putBoolean("doubleBuffered",doubleBuffered);
        pref.putInt("fsaaSamples",fsaaSamples);
        pref.putBoolean("verticalSync",verticalSync);
        pref.putInt("fps",fps);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        addGLEventListener(this);
        glCanvas.addMouseListener(this);
        glCanvas.addMouseMotionListener(this);
        glCanvas.addMouseWheelListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        removeGLEventListener(this);
        glCanvas.removeMouseListener(this);
        glCanvas.removeMouseMotionListener(this);
        glCanvas.removeMouseWheelListener(this);
    }

    private GLCapabilities getCapabilities() {
        GLProfile profile = GLProfile.getMaxProgrammable(true);
        GLCapabilities capabilities = new GLCapabilities(profile);
        capabilities.setHardwareAccelerated(hardwareAccelerated);
        capabilities.setBackgroundOpaque(true);
        capabilities.setDoubleBuffered(doubleBuffered);
        capabilities.setStencilBits(8);
        if(fsaaSamples>0) {
            capabilities.setSampleBuffers(true);
            capabilities.setNumSamples(1<<fsaaSamples);
        }
        StringBuilder sb = new StringBuilder();
        capabilities.toString(sb);
        logger.info("capabilities="+sb);
        return capabilities;
    }

    public void addGLEventListener(GLEventListener listener) {
        glCanvas.addGLEventListener(listener);
    }

    public void removeGLEventListener(GLEventListener listener) {
        glCanvas.removeGLEventListener(listener);
    }

    public void stopAnimationSystem() {
        animator.stop();
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        logger.info("init");

        GL3 gl3 = glAutoDrawable.getGL().getGL3();

        // turn on vsync
        gl3.setSwapInterval(verticalSync?1:0);

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
        Registry.textureFactory.unloadAll();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        //logger.debug("reshape {}x{}",width,height);
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


    public boolean isHardwareAccelerated() {
        return hardwareAccelerated;
    }

    public void setHardwareAccelerated(boolean hardwareAccelerated) {
        this.hardwareAccelerated = hardwareAccelerated;
    }

    @Override
    public boolean isDoubleBuffered() {
        return doubleBuffered;
    }

    @Override
    public void setDoubleBuffered(boolean doubleBuffered) {
        this.doubleBuffered = doubleBuffered;
    }

    public int getFsaaSamples() {
        return fsaaSamples;
    }

    public void setFsaaSamples(int fsaaSamples) {
        this.fsaaSamples = fsaaSamples;
    }

    public boolean isVerticalSync() {
        return verticalSync;
    }

    public void setVerticalSync(boolean verticalSync) {
        this.verticalSync = verticalSync;
    }

    public int getFPS() {
        return fps;
    }
}
