package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.preferences.InteractionPreferences;
import com.marginallyclever.robotoverlord.preferences.GraphicsPreferences;
import com.marginallyclever.robotoverlord.systems.render.Compass3D;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;
import com.marginallyclever.robotoverlord.systems.render.SkyBox;
import com.marginallyclever.robotoverlord.systems.render.Viewport;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.SelectionTool;
import com.marginallyclever.robotoverlord.tools.move.MoveCameraTool;
import com.marginallyclever.robotoverlord.tools.move.RotateEntityMultiTool;
import com.marginallyclever.robotoverlord.tools.move.TranslateEntityMultiTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * Encapsulates the OpenGL rendering.
 * @author Dan Royer
 */
public class OpenGLRenderPanel implements RenderPanel, GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderPanel.class);

    private final EntityManager entityManager;

    // OpenGL debugging
    private final JPanel panel = new JPanel(new BorderLayout());

    // the systems canvas
    private GLJPanel glCanvas;

    // mouse steering controls
    private boolean isMouseIn=false;

    // timing for animations
    private final FPSAnimator animator = new FPSAnimator(GraphicsPreferences.framesPerSecond.get());
    private long lastTime;
    private double frameDelay;
    private double frameLength;

    private final Viewport viewport = new Viewport();

    /**
     * Displayed in a 2D overlay, helps the user orient themselves in 3D space.
     */
    private final Compass3D compass3d = new Compass3D();

    /**
     * The "very far away" background to the scene.
     */
    private final SkyBox skyBox = new SkyBox();

    private final List<EditorTool> editorTools = new ArrayList<>();
    private int activeToolIndex = -1;

    private final BooleanParameter showWorldOrigin = new BooleanParameter("Show world origin",false);
    private final MaterialComponent defaultMaterial = new MaterialComponent();
    private final JToolBar toolBar = new JToolBar();

    private UpdateCallback updateCallback;

    private ShaderProgram shaderDefault;
    private ShaderProgram shaderOutline;
    private ShaderProgram shaderHUD;
    private final List<Entity> collectedEntities = new ArrayList<>();
    private final List<LightComponent> lights = new ArrayList<>();
    private final Mesh cursorMesh = new Mesh();


    public OpenGLRenderPanel(EntityManager entityManager) {
        super();
        logger.info("creating OpenGLRenderPanel");
        this.entityManager = entityManager;
        createCanvas();

        addCanvasListeners();

        hideDefaultCursor();
        createCursorMesh();
        setupTools();

        panel.setMinimumSize(new Dimension(300, 300));
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(glCanvas, BorderLayout.CENTER);

        startAnimationSystem();
    }

    @Override
    public void setUpdateCallback(UpdateCallback updateCallback) {
        this.updateCallback = updateCallback;
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    private void setupTools() {
        SelectionTool selectionTool = new SelectionTool(entityManager,viewport);
        editorTools.add(selectionTool);

        TranslateEntityMultiTool translateEntityMultiTool = new TranslateEntityMultiTool();
        editorTools.add(translateEntityMultiTool);

        RotateEntityMultiTool rotateEntityMultiTool = new RotateEntityMultiTool();
        editorTools.add(rotateEntityMultiTool);

        editorTools.add(new MoveCameraTool());

        for(EditorTool t : editorTools) {
            t.setViewport(viewport);
        }

        // build the bar
        toolBar.setFloatable(false);
        JButton activateSelectionTool = new JButton("Select");
        toolBar.add(activateSelectionTool);
        activateSelectionTool.addActionListener(e -> setActiveToolIndex(editorTools.indexOf(selectionTool)));

        JButton activateTranslateTool = new JButton("Translate");
        toolBar.add(activateTranslateTool);
        activateTranslateTool.addActionListener(e -> setActiveToolIndex(editorTools.indexOf(translateEntityMultiTool)));

        JButton activateRotateTool = new JButton("Rotate");
        toolBar.add(activateRotateTool);
        activateRotateTool.addActionListener(e -> setActiveToolIndex(editorTools.indexOf(rotateEntityMultiTool)));

        JComboBox<String> frameOfReferenceSelector = new JComboBox<>(new String[]{"World","Local","Camera"});
        frameOfReferenceSelector.addActionListener(e -> {
            int index = frameOfReferenceSelector.getSelectedIndex();
            for(EditorTool tool : editorTools) {
                tool.setFrameOfReference(index);
            }
        });
        frameOfReferenceSelector.setMaximumSize(frameOfReferenceSelector.getPreferredSize());
        toolBar.add(frameOfReferenceSelector);
    }

    private void setActiveToolIndex(int activeToolIndex) {
        deactivateAllTools();
        if(this.activeToolIndex == activeToolIndex) {
            // toggle off?
            this.activeToolIndex = -1;
        } else {
            this.activeToolIndex = activeToolIndex;
        }
        Clipboard.setSelectedEntities(Clipboard.getSelectedEntities());
    }

    private void hideDefaultCursor() {
        Cursor noCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0),
                "blank cursor");
        glCanvas.setCursor(noCursor);
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

    private void createCanvas() {
        try {
            logger.info("availability="+GLProfile.glAvailabilityToString());
            GLCapabilities capabilities = getCapabilities();
            logger.info("create canvas");
            glCanvas = new GLJPanel(capabilities);
        } catch(GLException e) {
            logger.error("Failed to create canvas.  Are your native drivers missing?");
        }
    }

    private void addCanvasListeners() {
        glCanvas.addGLEventListener(this);
        glCanvas.addMouseListener(this);
        glCanvas.addMouseMotionListener(this);
        glCanvas.addMouseWheelListener(this);
        glCanvas.addKeyListener(this);
    }

    private GL3 getGL3(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        if(GraphicsPreferences.glDebug.get()) gl = useGLDebugPipeline(gl);
        if(GraphicsPreferences.glTrace.get()) gl = useTracePipeline(gl);
        return gl.getGL3();
    }

    @Override
    public void init( GLAutoDrawable drawable ) {
        GL3 gl = getGL3(drawable);

        // turn on vsync
        gl.setSwapInterval(GraphicsPreferences.verticalSync.get() ? 1 : 0);

        // make things pretty
        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        // TODO add a settings toggle for this option, it really slows down older machines.
        if (GraphicsPreferences.fsaaSamples.get()>0) {
            gl.glEnable(GL3.GL_MULTISAMPLE);
        } else {
            gl.glDisable(GL3.GL_MULTISAMPLE);
        }

        // Don't draw triangles facing away from camera
        gl.glCullFace(GL3.GL_BACK);

        gl.glActiveTexture(GL3.GL_TEXTURE0);

        // depth testing and culling options
        gl.glDepthFunc(GL3.GL_LESS);
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthMask(true);
        gl.glEnable(GL3.GL_CULL_FACE);

        gl.glEnable(GL.GL_STENCIL_TEST);

        // default blending option for transparent materials
        gl.glEnable(GL3.GL_BLEND);
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);

        // set the color to use when wiping the draw buffer
        gl.glClearColor(0.85f, 0.85f, 0.85f, 0.0f);

        createShaderPrograms(gl);

        reloadAllAssets(gl);
    }

    private void reloadAllAssets(GL3 gl) {
        TextureFactory.unloadAll(gl);
        TextureFactory.loadAll();

        List<Entity> list = new ArrayList<>();
        list.add(entityManager.getRoot());
        while(!list.isEmpty()) {
            Entity test = list.remove(0);
            list.addAll(test.getChildren());
            MaterialComponent material = test.getComponent(MaterialComponent.class);
            if(material != null) {
                material.reloadTextures(gl);
            }
            ShapeComponent shape = test.getComponent(ShapeComponent.class);
            if(shape != null) {
                shape.unload(gl);
            }
        }
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
        viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
    }

    @Override
    public void dispose( GLAutoDrawable drawable ) {
        GL3 gl3 = getGL3(drawable);
        shaderDefault.delete(gl3);
        shaderOutline.delete(gl3);
        shaderHUD.delete(gl3);
    }

    @Override
    public void display( GLAutoDrawable drawable ) {
        long nowTime = System.currentTimeMillis();
        long dt = nowTime - lastTime;
        lastTime = nowTime;

        updateStep(dt*0.001);  // to seconds

        try {
            renderStep(getGL3(drawable));
        } catch(Exception e) {
            logger.error("Exception during render",e);
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        for(EditorTool tool : editorTools) tool.handleMouseEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        for(EditorTool tool : editorTools) tool.handleMouseEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for(EditorTool tool : editorTools) tool.handleMouseEvent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        isMouseIn=true;
        // needed for keys+mouse to work
        glCanvas.requestFocusInWindow();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        isMouseIn=false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        viewport.setCursor(e.getX(),e.getY());
        for(EditorTool tool : editorTools) tool.handleMouseEvent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        viewport.setCursor(e.getX(),e.getY());
        for(EditorTool tool : editorTools) tool.handleMouseEvent(e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        for(EditorTool tool : editorTools) tool.handleMouseEvent(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        for(EditorTool tool : editorTools) tool.handleKeyEvent(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        for(EditorTool tool : editorTools) tool.handleKeyEvent(e);
    }

    private String [] readResource(String resourceName) {
        ArrayList<String> lines = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getResourceAsStream(resourceName))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines.toArray(new String[0]);
    }

    private void createShaderPrograms(GL3 gl3) {
        shaderDefault = new ShaderProgram(gl3,
            readResource("default_330.vert"),
            readResource("default_330.frag"));
        shaderOutline = new ShaderProgram(gl3,
            readResource("outline_330.vert"),
            readResource("outline_330.frag"));
        shaderHUD = new ShaderProgram(gl3,
            readResource("default_330.vert"),
            readResource("givenColor_330.frag"));
    }

    private void destroyShaderPrograms(GL3 gl) {
        shaderDefault.delete(gl);
        shaderOutline.delete(gl);
        shaderHUD.delete(gl);
    }

    private void deactivateAllTools() {
        for(EditorTool tool : editorTools) tool.deactivate();
    }

    private GL useTracePipeline(GL gl) {
        logger.debug("using GL trace pipeline");
        try {
            return gl.getContext().setGL( GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[] { System.err } ) );
        } catch (Exception e) {
            logger.error("Failed to create GL trace pipeline", e);
        }
        return gl;
    }

    private GL useGLDebugPipeline(GL gl) {
        logger.info("using GL debug pipeline");
        try {
            return gl.getContext().setGL( GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null) );
        } catch (Exception e) {
            logger.error("Failed to create GL debug pipeline", e);
        }
        return gl;
    }

    private void renderStep(GL3 gl) {
        // clear green color, the depth bit, and the stencil buffer.
        gl.glClearColor(0.85f,0.85f,0.85f,1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT | GL3.GL_STENCIL_BUFFER_BIT);
        gl.glColorMask(true,true,true,true);

        draw3DScene(gl);
        //viewport.showPickingTest(gl);
        drawOverlays(gl);
    }

    private void draw3DScene(GL3 gl) {
        CameraComponent camera = entityManager.getCamera();
        if (camera == null) {
            // TODO display a "no active camera found" message?
            return;
        }

        collectSelectedEntitiesAndTheirChildren();  // TODO only when selection changes?

        viewport.setCamera(camera);
        renderLights();
        useShaderDefault(gl);
        skyBox.render(gl, viewport, shaderDefault);

        prepareToOutlineSelectedEntities(gl);
        renderAllEntities(gl, entityManager.getEntities(),shaderDefault);
        outlineCollectedEntities(gl);
    }

    private void useShaderDefault(GL3 gl) {
        Vector3d cameraPos = entityManager.getCamera().getPosition();

        OpenGLHelper.checkGLError(gl,logger);

        Vector3d lightPos, lightColor;
        if(!lights.isEmpty()) {
            LightComponent light0 = lights.get(0);
            Matrix4d lightPose = light0.getEntity().getComponent(PoseComponent.class).getWorld();
            lightPos = MatrixHelper.getPosition(lightPose);
            lightColor = new Vector3d(light0.diffuse.getR(), light0.diffuse.getG(), light0.diffuse.getB());
        } else {
            lightPos = new Vector3d();
            lightColor = new Vector3d(1,1,1);
        }

        shaderDefault.use(gl);
        setProjectionMatrix(gl,shaderDefault);
        setViewMatrix(gl,shaderDefault);

        OpenGLHelper.checkGLError(gl,logger);

        shaderDefault.setVector3d(gl,"lightPos",lightPos);  // Light position in world space
        OpenGLHelper.checkGLError(gl,logger);
        shaderDefault.setVector3d(gl,"cameraPos",cameraPos);  // Camera position in world space
        OpenGLHelper.checkGLError(gl,logger);
        shaderDefault.setVector3d(gl,"lightColor",lightColor);  // Light color
        OpenGLHelper.checkGLError(gl,logger);
        shaderDefault.set4f(gl,"objectColor",1,1,1,1);
        OpenGLHelper.checkGLError(gl,logger);
        shaderDefault.set1i(gl,"diffuseTexture",0);
        OpenGLHelper.checkGLError(gl,logger);
        shaderDefault.setVector3d(gl,"specularColor",new Vector3d(0.5,0.5,0.5));
        OpenGLHelper.checkGLError(gl,logger);
        shaderDefault.setVector3d(gl,"ambientLightColor",new Vector3d(0.2,0.2,0.2));

        shaderDefault.set1f(gl,"useVertexColor",0);

        OpenGLHelper.checkGLError(gl,logger);
    }

    private void setProjectionMatrix(GL3 gl, ShaderProgram program) {
        Matrix4d projectionMatrix = viewport.getChosenProjectionMatrix();
        program.setMatrix4d(gl,"projectionMatrix",projectionMatrix);
    }

    private void setOrthographicMatrix(GL3 gl3, ShaderProgram program) {
        Matrix4d projectionMatrix = viewport.getOrthographicMatrix();
        program.setMatrix4d(gl3,"projectionMatrix",projectionMatrix);
    }

    private void setViewMatrix(GL3 gl3,ShaderProgram program) {
        Matrix4d viewMatrix = viewport.getViewMatrix();
        viewMatrix.transpose();
        program.setMatrix4d(gl3,"viewMatrix",viewMatrix);
    }

    /**
     * Render 3d and then 2d overlays.
     * @param gl the OpenGL context
     */
    private void drawOverlays(GL3 gl) {
        gl.glClear(GL3.GL_DEPTH_BUFFER_BIT | GL3.GL_STENCIL_BUFFER_BIT);
        useShaderDefault(gl);
        // 3D overlays
        if (showWorldOrigin.get()) MatrixHelper.drawMatrix(10).render(gl);
        for(EditorTool tool : editorTools) tool.render(gl,shaderDefault);
        // 2D overlays
        compass3d.render(gl,viewport,shaderDefault);
        drawCursor(gl);
    }

    private void prepareToOutlineSelectedEntities(GL3 gl3) {
        gl3.glStencilFunc(GL.GL_ALWAYS,1,0xff);
        // if we pass the depth test, keep the old value in the stencil buffer.
        // if we pass the stencil test, keep the old value in the stencil buffer.
        // if we pass BOTH then replace the stencil buffer value with the reference value (1).
        gl3.glStencilOp(GL.GL_KEEP,GL.GL_KEEP,GL.GL_REPLACE);
        // by default write nothing to the stencil buffer.
        gl3.glStencilMask(0x00);
    }

    // get the selected entities and all their children.
    private void collectSelectedEntitiesAndTheirChildren() {
        List<Entity> toScan = new ArrayList<>(Clipboard.getSelectedEntities());
        collectedEntities.clear();
        while(!toScan.isEmpty()) {
            Entity entity = toScan.remove(0);
            toScan.addAll(entity.getChildren());
            collectedEntities.add(entity);
        }
    }

    private void outlineCollectedEntities(GL3 gl) {
        if(shaderOutline==null) return;

        MatrixMaterialRenderSet mmrSet = new MatrixMaterialRenderSet(collectedEntities);
        sortMMRAlpha(mmrSet);

        // update the depth buffer so the outline will appear around the collected entities.
        // without this any part of a collected entity behind another entity will be completely filled with the outline color.
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glColorMask(false,false,false,false);
        renderMMRSet(gl,mmrSet,shaderDefault);
        gl.glColorMask(true,true,true,true);

        // next draw, only draw where the stencil buffer is not 1
        gl.glStencilFunc(GL.GL_NOTEQUAL,1,0xff);
        gl.glStencilOp(GL.GL_KEEP,GL.GL_KEEP,GL.GL_KEEP);
        // and do not update the stencil buffer.
        gl.glStencilMask(0x00);

        gl.glLineWidth(GraphicsPreferences.outlineWidth.get());
        gl.glDepthMask(false);
        gl.glDisable(GL.GL_CULL_FACE);

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE);
        try {
            useShaderOutline(gl);
            renderMMRSet(gl, mmrSet, shaderOutline);
        } catch(Exception ignored) {}
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_FILL);

        gl.glEnable(GL.GL_CULL_FACE);
        gl.glDepthMask(true);
        gl.glLineWidth(1);

        // clean up
        gl.glUseProgram(0);
        gl.glStencilMask(0xFF);
    }

    /**
     * Sort alpha objects back to front
     * @param mmrSet the set to sort
     */
    private void sortMMRAlpha(MatrixMaterialRenderSet mmrSet) {
        if(mmrSet.alpha.isEmpty()) return;
        Vector3d cameraPoint = new Vector3d();
        Entity cameraEntity = entityManager.getCamera().getEntity();
        cameraEntity.getComponent(PoseComponent.class).getWorld().get(cameraPoint);
        mmrSet.sortAlpha(cameraPoint);
    }

    private void useShaderOutline(GL3 gl) {
        // must be in use before calls to glUniform*.
        shaderOutline.use(gl);
        // tell the shader some important information
        setProjectionMatrix(gl,shaderOutline);
        setViewMatrix(gl,shaderOutline);
        double[] color = GraphicsPreferences.outlineColor.get();
        shaderOutline.set4f(gl,
                "outlineColor",
                (float)color[0],
                (float)color[1],
                (float)color[2],
                (float)color[3]);
        shaderOutline.set1f(gl,"useVertexColor",0);
    }

    /**
     * Render all Entities in the scene.  Search all entities for a {@link RenderComponent}.
     * Sort them into three lists: those with no material, those with opaque material, and those with transparent
     * material.  Further sort the alpha list by distance from the camera.  Then systems the opaque, systems the alpha,
     * and systems the no-material.
     * @param gl3 the OpenGL context
     * @param list the list of entities to render
     * @param shaderProgram the shader to use
     */
    private void renderAllEntities(GL3 gl3,List<Entity> list,ShaderProgram shaderProgram) {
        MatrixMaterialRenderSet mmrSet = new MatrixMaterialRenderSet(list);
        sortMMRAlpha(mmrSet);
        renderMMRSet(gl3, mmrSet, shaderProgram);
    }

    /**
     * Render all the lists in an {@link MatrixMaterialRenderSet} in the correct order.
     * @param gl3 the OpenGL context
     * @param mmrSet the set to render
     * @param shaderProgram the shader to use
     */
    private void renderMMRSet(GL3 gl3, MatrixMaterialRenderSet mmrSet, ShaderProgram shaderProgram) {
        defaultMaterial.render(gl3);
        // opaque objects
        renderMMRList(gl3,mmrSet.opaque,shaderProgram);
        // alpha objects
        renderMMRList(gl3, mmrSet.alpha, shaderProgram);
        // objects with no material
        defaultMaterial.render(gl3);
        renderMMRList(gl3,mmrSet.noMaterial,shaderProgram);
        // objects on top of everything else
        gl3.glDisable(GL3.GL_DEPTH_TEST);
        defaultMaterial.render(gl3);
        renderMMRList(gl3,mmrSet.onTop,shaderProgram);
        gl3.glEnable(GL3.GL_DEPTH_TEST);
    }

    private void renderMMRList(GL3 gl, List<MatrixMaterialRender> list,ShaderProgram shaderProgram) {
        for (MatrixMaterialRender mmr : list) {
            renderOneMMRItem(gl, mmr, shaderProgram);
        }
    }

    private void renderOneMMRItem(GL3 gl, MatrixMaterialRender mmr, ShaderProgram shaderProgram) {
        if(mmr.renderComponent==null || !mmr.renderComponent.getVisible()) return;

        if(mmr.matrix!=null) {
            Matrix4d m = mmr.matrix;
            m.transpose();
            // tell the shaders about our modelMatrix.
            shaderProgram.setMatrix4d(gl,"modelMatrix",m);
        }

        // if this mesh is one of the selected entities, then also render it to the stencil buffer for the outline shader.
        boolean isCollected = collectedEntities.contains(mmr.renderComponent.getEntity());
        gl.glStencilMask(isCollected ? 0xFF : 0x00);
        OpenGLHelper.checkGLError(gl,logger);

        Texture texture = null;
        boolean useVertexColor=true;
        boolean useTexture=true;
        boolean useLighting=true;
        if(mmr.materialComponent!=null && mmr.materialComponent.getEnabled()) {
            MaterialComponent material = mmr.materialComponent;
            material.render(gl);
            OpenGLHelper.checkGLError(gl,logger);
            // flat light?
            useLighting &= material.isLit();
            // if we have a texture assigned, then we might still enable textures.
            texture = material.texture.getTexture();
            if(texture==null) useTexture = false;
            // assign the object's overall color.
            double[] diffuseColor = material.getDiffuseColor();
            shaderProgram.set4f(gl,
                    "objectColor",
                    (float)diffuseColor[0],
                    (float)diffuseColor[1],
                    (float)diffuseColor[2],
                    (float)diffuseColor[3]);

            OpenGLHelper.checkGLError(gl,logger);
        }

        boolean hasMesh = false;
        if(mmr.renderComponent instanceof ShapeComponent) {
            // if this component is a shape
            ShapeComponent shape = (ShapeComponent)mmr.renderComponent;
            Mesh mesh = shape.getModel();
            if(mesh != null) {
                hasMesh = true;
                // and it has vertex colors, enable them.
                useVertexColor &= mesh.getHasColors();
                // and it has texture coordinates, continue to allow textures.
                useTexture &= mesh.getHasTextures();
                useLighting &= mesh.getHasNormals();
            }
        }
        if(!hasMesh) {
            useVertexColor=false;
            useTexture=false;
            useLighting=false;
        }

        shaderProgram.set1i(gl,"useVertexColor",useVertexColor?1:0);
        shaderProgram.set1i(gl,"useLighting",useLighting?1:0);
        shaderProgram.set1i(gl,"useTexture",useTexture?1:0);

        OpenGLHelper.checkGLError(gl,logger);

        if(useTexture && texture!=null) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            mmr.materialComponent.render(gl);
            shaderProgram.set1i(gl,"diffuseTexture",0);
            OpenGLHelper.checkGLError(gl,logger);
        }

        mmr.renderComponent.render(gl);
    }

    private void renderLights() {
        lights.clear();

        Queue<Entity> found = new LinkedList<>(entityManager.getEntities());
        while(!found.isEmpty()) {
            Entity obj = found.remove();
            found.addAll(obj.getChildren());

            LightComponent light = obj.getComponent(LightComponent.class);
            if(light!=null && light.getEnabled()) {
                lights.add(light);
            }
        }
    }

    private void drawCursor(GL3 gl3) {
        if(!isMouseIn) return;

        shaderHUD.use(gl3);
        setOrthographicMatrix(gl3,shaderHUD);
        shaderHUD.setMatrix4d(gl3,"viewMatrix",MatrixHelper.createIdentityMatrix4());

        double [] cursor = viewport.getCursorAsNormalized();
        Matrix4d modelView = MatrixHelper.createIdentityMatrix4();
        modelView.m03 = cursor[0] * viewport.getCanvasWidth() / 2d;
        modelView.m13 = cursor[1] * viewport.getCanvasHeight() / 2d;
        modelView.m23 = -10;
        modelView.transpose();
        shaderHUD.setMatrix4d(gl3,"modelMatrix",modelView);

        // draw!
        boolean tex = OpenGLHelper.disableTextureStart(gl3);
        int top = OpenGLHelper.drawAtopEverythingStart(gl3);

        cursorMesh.render(gl3);

        OpenGLHelper.drawAtopEverythingEnd(gl3,top);
        OpenGLHelper.disableTextureEnd(gl3,tex);
    }

    private void createCursorMesh() {
        // build mesh - only needs to be done once.
        float c = (float) InteractionPreferences.cursorSize.get();
        cursorMesh.clear();
        cursorMesh.setRenderStyle(GL3.GL_LINES);
        cursorMesh.addVertex(1,-c,0);   cursorMesh.addColor(0,0,0,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex(1, c,0);   cursorMesh.addColor(0,0,0,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex(-c,1,0);   cursorMesh.addColor(0,0,0,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex( c,1,0);   cursorMesh.addColor(0,0,0,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);

        cursorMesh.addVertex(-1,-c,0);  cursorMesh.addColor(0,0,0,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex(-1, c,0);  cursorMesh.addColor(0,0,0,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex(-c,-1,0);  cursorMesh.addColor(0,0,0,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex( c,-1,0);  cursorMesh.addColor(0,0,0,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);

        cursorMesh.addVertex(0,-c,0);   cursorMesh.addColor(1,1,1,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex(0, c,0);   cursorMesh.addColor(1,1,1,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex(-c,0,0);   cursorMesh.addColor(1,1,1,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
        cursorMesh.addVertex( c,0,0);   cursorMesh.addColor(1,1,1,1);   cursorMesh.addNormal(0,0,1);    cursorMesh.addTexCoord(0,0);
    }

    private void updateStep(double dt) {
        frameDelay+=dt;
        if(frameDelay>frameLength) {
            frameDelay-=frameLength;

            for(EditorTool tool : editorTools) tool.update(dt);

            updateCallback.update(dt);
        }
    }

    @Override
    public void startAnimationSystem() {
        logger.debug("starting animation system");
        int fps = GraphicsPreferences.framesPerSecond.get();
        animator.setFPS(fps);
        frameDelay=0;
        frameLength=1.0f/(float)fps;
        animator.add(glCanvas);
        // record the start time of the application, also the end of the core initialization process.
        lastTime = System.currentTimeMillis();
        // start the main application loop.  it will call display() repeatedly.
        animator.start();
    }

    @Override
    public void stopAnimationSystem() {
        logger.debug("stopping animation system");
        animator.stop();
    }

    @Override
    public void updateSubjects(List<Entity> list) {
        if(activeToolIndex>=0) {
            editorTools.get(activeToolIndex).deactivate();
            editorTools.get(activeToolIndex).activate(list);
        }
    }
}
