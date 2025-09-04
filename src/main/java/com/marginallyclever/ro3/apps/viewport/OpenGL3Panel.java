package com.marginallyclever.ro3.apps.viewport;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.ResourceHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.viewport.renderpass.RenderPass;
import com.marginallyclever.ro3.apps.viewport.viewporttool.ViewportTool;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.PrintStream;
import java.util.prefs.Preferences;

/**
 * {@link OpenGL3Panel} manages a {@link GLJPanel} and an {@link FPSAnimator}.
 * It is a concrete implementation of {@link Viewport}.
 */
public class OpenGL3Panel extends Viewport implements GLEventListener {
    private static final Logger logger = LoggerFactory.getLogger(OpenGL3Panel.class);
    protected GLJPanel glCanvas;
    private boolean hardwareAccelerated = true;
    private boolean doubleBuffered = true;
    private int fsaaSamples = 2;
    private boolean verticalSync = true;
    private int fps = 30;
    private final FPSAnimator animator;
    private ShaderProgram toolShader;

    public OpenGL3Panel() {
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
        animator = new FPSAnimator(fps);
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
        capabilities.setDepthBits(32);  // 32 bit depth buffer is floating point
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

        attachPipelines(glAutoDrawable);

        GL3 gl3 = glAutoDrawable.getGL().getGL3();

        // turn on vsync
        gl3.setSwapInterval(verticalSync?1:0);

        // make things pretty
        gl3.glEnable(GL3.GL_LINE_SMOOTH);
        gl3.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl3.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        gl3.glEnable(GL3.GL_MULTISAMPLE);
        // depth testing and culling options
        gl3.glEnable(GL3.GL_DEPTH_TEST);
        gl3.glDepthFunc(GL3.GL_LESS);
        gl3.glDepthMask(true);

        /*
        // for reverse-z depth buffer
        GL4 gl = glAutoDrawable.getGL().getGL4();
        gl.glDisable(GL3.GL_DEPTH_TEST);
        gl.glDepthFunc(GL3.GL_GREATER);
        gl.glClearDepth(0);
        gl.glClipControl(GL4.GL_LOWER_LEFT, GL4.GL_ZERO_TO_ONE);
        */

        // Don't draw triangles facing away from camera
        gl3.glEnable(GL3.GL_CULL_FACE);
        gl3.glCullFace(GL3.GL_BACK);

        // default blending option for transparent materials
        gl3.glEnable(GL3.GL_BLEND);
        gl3.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

        gl3.glActiveTexture(GL3.GL_TEXTURE0);

        try {
            var spf = Registry.shaderProgramFactory;
            toolShader = spf.createShaderProgram(Lifetime.APPLICATION,"toolShader",
                    spf.createShader(Lifetime.APPLICATION,GL3.GL_VERTEX_SHADER, ResourceHelper.readResource(this.getClass(),"/com/marginallyclever/ro3/apps/viewport/default.vert")),
                    spf.createShader(Lifetime.APPLICATION,GL3.GL_FRAGMENT_SHADER, ResourceHelper.readResource(this.getClass(),"/com/marginallyclever/ro3/apps/viewport/default.frag"))
            );
        } catch(Exception e) {
            logger.error("Failed to load shader", e);
        }
        for(ViewportTool tool : viewportTools) tool.init(gl3);
    }

    /**
     * Attaches the OpenGL pipelines to the {@link GLAutoDrawable}.
     * This allows for debugging and tracing OpenGL calls.
     * @param glAutoDrawable the OpenGL drawable to attach the pipelines to.
     */
    private void attachPipelines(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();
        if(this.isTraceGL()) {
            logger.info("Activating trace pipeline");
            gl = new TraceGL3(gl, new PrintStream(System.out));
        }
        if(this.isDebugGL()) {
            logger.info("Activating debug pipeline");
            gl = new DebugGL3(gl);
        }
        glAutoDrawable.setGL(gl);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        logger.info("dispose");
        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        for(ViewportTool tool : viewportTools) tool.dispose(gl3);
        Registry.textureFactory.unloadAll(gl3);
        Registry.meshFactory.unloadAll(gl3);
        Registry.shaderProgramFactory.unloadAll(gl3);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        double dt = 1.0 / (double)this.getFPS();
        for(ViewportTool tool : viewportTools) tool.update(dt);
        updateAllNodes(dt);

        GL3 gl3 = glAutoDrawable.getGL().getGL3();
        renderAllPasses(gl3);
        renderViewportTools(gl3);
    }

    public void renderViewportTools(GL3 gl3) {
        Camera camera = getActiveCamera();
        assert camera != null;

        toolShader.use(gl3);
        toolShader.setMatrix4d(gl3, "viewMatrix", camera.getViewMatrix(isOriginShift()));
        toolShader.setMatrix4d(gl3, "projectionMatrix", camera.getChosenProjectionMatrix(canvasWidth, canvasHeight));
        Vector3d cameraWorldPos = MatrixHelper.getPosition(camera.getWorld());
        toolShader.setVector3d(gl3, "cameraPos", cameraWorldPos);  // Camera position in world space
        toolShader.setVector3d(gl3, "lightPos", cameraWorldPos);  // doesn't matter, viewport tools don't use lighting.

        toolShader.setColor(gl3, "lightColor", Color.WHITE);
        toolShader.setColor(gl3, "diffuseColor", Color.WHITE);
        toolShader.setColor(gl3, "specularColor", Color.WHITE);
        toolShader.setColor(gl3,"ambientColor",Color.BLACK);

        toolShader.set1i(gl3,"useTexture",0);
        toolShader.set1i(gl3,"useLighting",0);
        toolShader.set1i(gl3,"useVertexColor",0);

        gl3.glDisable(GL3.GL_DEPTH_TEST);
        for(ViewportTool tool : viewportTools) {
            tool.render(gl3,toolShader);
        }
        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }

    @Override
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

    @Override
    public boolean isVerticalSync() {
        return verticalSync;
    }

    public void setVerticalSync(boolean verticalSync) {
        this.verticalSync = verticalSync;
    }

    public int getFPS() {
        return fps;
    }

    @Override
    protected void addRenderPass(Object source, RenderPass renderPass) {
        addGLEventListener(renderPass);
        super.addRenderPass(source,renderPass);
    }

    @Override
    protected void removeRenderPass(Object source,RenderPass renderPass) {
        removeGLEventListener(renderPass);
        super.removeRenderPass(source,renderPass);
    }
}
