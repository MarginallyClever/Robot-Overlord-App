package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotoverlord.*;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
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
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * Encapsulates the OpenGL rendering.
 * @author Dan Royer
 */
public class OpenGLRenderPanel implements RenderPanel {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderPanel.class);
    private static final int FSAA_NUM_SAMPLES = 4;  // 1,2,4,8
    private static final int VERTICAL_SYNC_ON = 1;  // 1 on, 0 off
    private static final int DEFAULT_FRAMES_PER_SECOND = 30;

    private final EntityManager entityManager;

    // OpenGL debugging
    private final boolean glDebug=false;
    private final boolean glTrace=false;

    // should I check the state of the OpenGL stack size?  true=every frame, false=never
    private final boolean checkStackSize = false;

    // used to check the stack size.
    private final IntBuffer stackDepth = IntBuffer.allocate(1);

    private final JPanel panel = new JPanel(new BorderLayout());

    // the systems canvas
    private GLJPanel glCanvas;

    // mouse steering controls
    private boolean isMouseIn=false;

    // timing for animations
    private final FPSAnimator animator = new FPSAnimator(DEFAULT_FRAMES_PER_SECOND);
    private long lastTime;
    private double frameDelay;
    private double frameLength;

    // click on screen to change which entity is selected
    private final Viewport viewport = new Viewport();

    /**
     * Displayed in a 2D overlay, helps the user orient themselves in 3D space.
     */
    private transient final Compass3D compass3d = new Compass3D();

    /**
     * The "very far away" background to the scene.
     */
    private transient final SkyBox skyBox = new SkyBox();

    private final List<EditorTool> editorTools = new ArrayList<>();
    private int activeToolIndex = -1;

    private final BooleanParameter showWorldOrigin = new BooleanParameter("Show world origin",false);

    private final ColorParameter ambientLight = new ColorParameter("Ambient light",0.2,0.2,0.2,1);
    private final MaterialComponent defaultMaterial = new MaterialComponent();

    /**
     * Used to sort items at systems time. Opaque items are rendered first, then alpha items.
     */
    private static class MatrixMaterialRender {
        public Matrix4d matrix = new Matrix4d();
        public RenderComponent renderComponent;
        public MaterialComponent materialComponent;
    }
    private final List<MatrixMaterialRender> opaque = new ArrayList<>();
    private final List<MatrixMaterialRender> alpha = new ArrayList<>();
    private final List<MatrixMaterialRender> noMaterial = new ArrayList<>();
    private final List<MatrixMaterialRender> onTop = new ArrayList<>();
    private UpdateCallback updateCallback;

    private ShaderProgram shaderDefault;
    private ShaderProgram shaderOutline;
    private ShaderProgram shaderHUD;
    private final List<Entity> collectedEntities = new ArrayList<>();
    private final List<LightComponent> lights = new ArrayList<>();

    private double cursorSize = 10;


    public OpenGLRenderPanel(EntityManager entityManager) {
        super();
        logger.info("creating OpenGLRenderPanel");

        this.entityManager = entityManager;
        createCanvas();
        addCanvasListeners();
        hideDefaultCursor();

        panel.setMinimumSize(new Dimension(300, 300));
        panel.add(setupTools(), BorderLayout.NORTH);
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

    private JToolBar setupTools() {
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
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        JButton activateSelectionTool = new JButton("Select");
        bar.add(activateSelectionTool);
        activateSelectionTool.addActionListener(e -> setActiveToolIndex(editorTools.indexOf(selectionTool)));

        JButton activateTranslateTool = new JButton("Translate");
        bar.add(activateTranslateTool);
        activateTranslateTool.addActionListener(e -> setActiveToolIndex(editorTools.indexOf(translateEntityMultiTool)));

        JButton activateRotateTool = new JButton("Rotate");
        bar.add(activateRotateTool);
        activateRotateTool.addActionListener(e -> setActiveToolIndex(editorTools.indexOf(rotateEntityMultiTool)));

        return bar;
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

    private void createCanvas() {
        try {
            logger.info("...get default caps");
            GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
            caps.setBackgroundOpaque(true);
            caps.setDoubleBuffered(true);
            caps.setHardwareAccelerated(true);
            caps.setStencilBits(8);
            if(FSAA_NUM_SAMPLES>1) {
                caps.setSampleBuffers(true);
                caps.setNumSamples(FSAA_NUM_SAMPLES);
            }
            StringBuilder sb = new StringBuilder();
            caps.toString(sb);
            logger.info("...set caps to "+sb.toString());
            logger.info("...create canvas");
            glCanvas = new GLJPanel(caps);
        } catch(GLException e) {
            logger.error("Failed to get/set Capabilities.  Are your native drivers missing?");
        }
    }

    private void addCanvasListeners() {
        glCanvas.addGLEventListener(new GLEventListener() {
            @Override
            public void init( GLAutoDrawable drawable ) {
                GL gl = drawable.getGL();
                if(glDebug) gl = useGLDebugPipeline(gl);
                if(glTrace) gl = useTracePipeline(gl);

                GL2 gl2 = drawable.getGL().getGL2();

                // turn on vsync
                gl2.setSwapInterval(VERTICAL_SYNC_ON);

                // make things pretty
                gl2.glEnable(GL2.GL_LINE_SMOOTH);
                gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
                gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
                // TODO add a settings toggle for this option, it really slows down older machines.
                gl2.glEnable(GL2.GL_MULTISAMPLE);

                // Don't draw triangles facing away from camera
                gl2.glCullFace(GL2.GL_BACK);

                gl2.glActiveTexture(GL2.GL_TEXTURE0);

                int [] buf = new int[1];
                int [] sbuf = new int[1];
                gl2.glGetIntegerv(GL2.GL_SAMPLES, buf, 0);
                gl2.glGetIntegerv(GL2.GL_SAMPLE_BUFFERS, sbuf, 0);

                // depth testing and culling options
                gl2.glDepthFunc(GL2.GL_LESS);
                gl2.glEnable(GL2.GL_DEPTH_TEST);
                gl2.glDepthMask(true);
                gl2.glEnable(GL2.GL_CULL_FACE);

                gl2.glEnable(GL.GL_STENCIL_TEST);

                // default blending option for transparent materials
                gl2.glEnable(GL2.GL_BLEND);
                gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

                // set the color to use when wiping the draw buffer
                gl2.glClearColor(0.85f,0.85f,0.85f,0.0f);

                createShaderPrograms(gl2);
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
                viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
                viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
            }

            @Override
            public void dispose( GLAutoDrawable drawable ) {
                GL2 gl2 = drawable.getGL().getGL2();
                shaderDefault.delete(gl2);
                shaderOutline.delete(gl2);
                shaderHUD.delete(gl2);
            }

            @Override
            public void display( GLAutoDrawable drawable ) {
                long nowTime = System.currentTimeMillis();
                long dt = nowTime - lastTime;
                lastTime = nowTime;

                updateStep(dt*0.001);  // to seconds

                GL2 gl2 = drawable.getGL().getGL2();

                checkRenderStep(gl2);
            }
        });

        // this class also listens to the glCanvas (messy!)
        glCanvas.addMouseListener(new MouseAdapter() {
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
                glCanvas.requestFocusInWindow();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isMouseIn=false;
            }
        });

        glCanvas.addMouseMotionListener(new MouseMotionListener() {
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
        });

        glCanvas.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                for(EditorTool tool : editorTools) tool.handleMouseEvent(e);
            }
        });

        glCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                for(EditorTool tool : editorTools) tool.handleKeyEvent(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                for(EditorTool tool : editorTools) tool.handleKeyEvent(e);
            }
        });
    }

    private String [] readResource(String resourceName) {
        ArrayList<String> lines = new ArrayList<>();
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

    private void createShaderPrograms(GL2 gl2) {
        shaderDefault = new ShaderProgram(gl2,
            readResource("default_330.vert"),
            readResource("default_330.frag"));
        shaderOutline = new ShaderProgram(gl2,
            readResource("outline_330.vert"),
            readResource("outline_330.frag"));
        shaderHUD = new ShaderProgram(gl2,
            readResource("notransform_330.vert"),
            readResource("givenColor_330.frag"));
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

    private CameraComponent getCamera() {
        return entityManager.getCamera();
    }

    private void checkRenderStep(GL2 gl2) {
        int before;
        if(checkStackSize) {
            gl2.glGetIntegerv(GL2.GL_MODELVIEW_STACK_DEPTH, stackDepth);
            before = stackDepth.get(0);
        }

        try {
            renderStep(gl2);
        } catch(Exception e) {
            logger.error("GL error",e);
            e.printStackTrace();
        }

        if(checkStackSize) {
            gl2.glGetIntegerv(GL2.GL_MODELVIEW_STACK_DEPTH, stackDepth);
            int after = stackDepth.get(0);
            if(before != after) {
                System.err.println("stack depth " + before + " vs " + after);
                logger.warn("stack depth " + before + " vs " + after);
            }
        }
    }

    private void renderStep(GL2 gl2) {
        // clear green color, the depth bit, and the stencil buffer.
        gl2.glClearColor(0.85f,0.85f,0.85f,1.0f);
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);

        draw3DScene(gl2);
        //viewport.showPickingTest(gl2);
        drawOverlays(gl2);
    }

    private void draw3DScene(GL2 gl2) {
        CameraComponent camera = getCamera();
        if (camera == null) {
            gl2.glClearColor(0.85f,0.85f,0.85f,1.0f);
            gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
            // TODO display a "no active camera found" message?
            return;
        }
        collectSelectedEntitiesAndTheirChildren();  // TODO only when selection changes?
        prepareToOutlineSelectedEntities(gl2);

        viewport.setCamera(camera);
        viewport.renderChosenProjection(gl2);
        renderLights(gl2);

        useShaderDefault(gl2);

        skyBox.render(gl2, camera,shaderDefault);

        renderAllEntities(gl2, entityManager.getEntities(),shaderDefault);
        if (showWorldOrigin.get()) PrimitiveSolids.drawStar(gl2, 10);

        outlineCollectedEntities(gl2);
    }

    private void useShaderDefault(GL2 gl2) {
        Vector3d cameraPos = getCamera().getPosition();

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

        shaderDefault.use(gl2);
        setProjectionMatrix(gl2,shaderDefault);
        setViewMatrix(gl2,shaderDefault);

        shaderDefault.setVector3d(gl2,"lightPos",lightPos);  // Light position in world space
        shaderDefault.setVector3d(gl2,"cameraPos",cameraPos);  // Camera position in world space
        shaderDefault.setVector3d(gl2,"lightColor",lightColor);  // Light color
        shaderDefault.set4f(gl2,"objectColor",1,1,1,1);
        shaderDefault.set1f(gl2,"diffuseTexture",0);
        shaderDefault.setVector3d(gl2,"specularColor",new Vector3d(0.5,0.5,0.5));
        shaderDefault.setVector3d(gl2,"ambientLightColor",new Vector3d(0.2,0.2,0.2));
    }

    private void setProjectionMatrix(GL2 gl2, ShaderProgram program) {
        Matrix4d projectionMatrix = viewport.getChosenProjectionMatrix();
        program.setMatrix4d(gl2,"projectionMatrix",projectionMatrix);
    }

    private void setOrthograpicMatrix(GL2 gl2, ShaderProgram program) {
        Matrix4d projectionMatrix = viewport.getOrthographicMatrix();
        program.setMatrix4d(gl2,"projectionMatrix",projectionMatrix);
    }

    private void setViewMatrix(GL2 gl2,ShaderProgram program) {
        Matrix4d viewMatrix = viewport.getViewMatrix();
        viewMatrix.transpose();
        program.setMatrix4d(gl2,"viewMatrix",viewMatrix);
    }

    private void drawOverlays(GL2 gl2) {
        // overlays
        gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
        gl2.glUseProgram(0);
        viewport.renderChosenProjection(gl2);

        // 3D overlays
        for(EditorTool tool : editorTools) tool.render(gl2);

        // 2D overlays
        compass3d.render(gl2,viewport,shaderDefault);
        drawCursor(gl2);

        shaderDefault.use(gl2);
    }

    private void checkGLError(GL2 gl2) {
        if(gl2.glGetError() != GL2.GL_NO_ERROR) {
            logger.error("GL error:" + gl2.glGetError());
        }
    }

    private void prepareToOutlineSelectedEntities(GL2 gl2) {
        gl2.glStencilFunc(GL.GL_ALWAYS,1,0xff);
        // if we pass the depth test, keep the old value in the stencil buffer.
        // if we pass the stencil test, keep the old value in the stencil buffer.
        // if we pass BOTH then replace the stencil buffer value with the reference value (1).
        gl2.glStencilOp(GL.GL_KEEP,GL.GL_KEEP,GL.GL_REPLACE);
        // by default write nothing to the stencil buffer.
        gl2.glStencilMask(0x00);
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

    private void outlineCollectedEntities(GL2 gl2) {
        if(shaderOutline ==null) return;

        // update the depth buffer so the outline will appear around the collected entities.
        // without this any part of a collected entity behind another entity will be completely filled with the outline color.
        gl2.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl2.glColorMask(false,false,false,false);
        renderAllEntities(gl2,collectedEntities,shaderDefault);
        gl2.glColorMask(true,true,true,true);

        // next draw, only draw where the stencil buffer is not 1
        gl2.glStencilFunc(GL.GL_NOTEQUAL,1,0xff);
        gl2.glStencilOp(GL.GL_KEEP,GL.GL_KEEP,GL.GL_KEEP);
        // and do not update the stencil buffer.
        gl2.glStencilMask(0x00);

        useShaderOutline(gl2);

        renderAllEntities(gl2,collectedEntities,shaderOutline);

        // clean up
        gl2.glUseProgram(0);
        gl2.glStencilMask(0xFF);
    }

    private void useShaderOutline(GL2 gl2) {
        // must be in use before calls to glUniform*.
        shaderOutline.use(gl2);
        // tell the shader some important information
        setProjectionMatrix(gl2,shaderOutline);
        setViewMatrix(gl2,shaderOutline);
        shaderOutline.set4f(gl2,"outlineColor",0.0f, 1.0f, 0.0f, 0.5f);
        shaderOutline.set1f(gl2,"outlineSize",0.25f);
    }

    /**
     * Render all Entities in the scene.  Search all entities for a {@link RenderComponent}.
     * Sort them into three lists: those with no material, those with opaque material, and those with transparent
     * material.  Further sort the alpha list by distance from the camera.  Then systems the opaque, systems the alpha,
     * and systems the no-material.
     *
     */
    private void renderAllEntities(GL2 gl2,List<Entity> list,ShaderProgram shaderProgram) {
        opaque.clear();
        alpha.clear();
        noMaterial.clear();
        onTop.clear();

        // collect all entities with a RenderComponent
        Queue<Entity> toRender = new LinkedList<>(list);
        while(!toRender.isEmpty()) {
            Entity entity = toRender.remove();
            toRender.addAll(entity.getChildren());

            RenderComponent renderComponent = entity.getComponent(RenderComponent.class);
            if(renderComponent!=null) {
                MatrixMaterialRender mmr = new MatrixMaterialRender();
                mmr.renderComponent = entity.getComponent(RenderComponent.class);
                mmr.materialComponent = entity.getComponent(MaterialComponent.class);
                PoseComponent pose = entity.getComponent(PoseComponent.class);
                if(pose!=null) mmr.matrix.set(pose.getWorld());

                if(mmr.materialComponent==null) noMaterial.add(mmr);
                else if(mmr.materialComponent.drawOnTop.get()) onTop.add(mmr);
                else if(mmr.materialComponent.isAlpha()) alpha.add(mmr);
                else opaque.add(mmr);
            }
        }

        // opaque objects
        defaultMaterial.render(gl2);
        renderMMRList(gl2,opaque,shaderProgram);

        // sort alpha objects back to front
        Vector3d cameraPoint = new Vector3d();
        Entity cameraEntity = getCamera().getEntity();
        cameraEntity.getComponent(PoseComponent.class).getWorld().get(cameraPoint);

        Vector3d p1 = new Vector3d();
        Vector3d p2 = new Vector3d();
        alpha.sort((o1, o2) -> {
            o1.matrix.get(p1);
            o2.matrix.get(p2);
            p1.sub(cameraPoint);
            p2.sub(cameraPoint);
            double d1 = p1.lengthSquared();
            double d2 = p2.lengthSquared();
            return (int)Math.signum(d2-d1);
        });

        // alpha objects
        renderMMRList(gl2,alpha,shaderProgram);

        // objects with no material
        defaultMaterial.render(gl2);
        renderMMRList(gl2,noMaterial,shaderProgram);

        // onTop
        gl2.glDisable(GL2.GL_DEPTH_TEST);
        defaultMaterial.render(gl2);
        renderMMRList(gl2,onTop,shaderProgram);
        gl2.glEnable(GL2.GL_DEPTH_TEST);
    }

    private void renderMMRList(GL2 gl2, List<MatrixMaterialRender> list,ShaderProgram shaderProgram) {
        for(MatrixMaterialRender mmr : list) {
            if(mmr.renderComponent==null || !mmr.renderComponent.getVisible()) continue;

            if(mmr.matrix!=null) {
                Matrix4d m = mmr.matrix;
                m.transpose();
                // tell the shaders about our modelMatrix.
                shaderProgram.setMatrix4d(gl2,"modelMatrix",m);
            }

            if(collectedEntities.contains(mmr.renderComponent.getEntity())) {
                // if this mesh is one of the selected entities, then also render it to the stencil buffer for the outline shader.
                gl2.glStencilMask(0xFF);
            } else {
                gl2.glStencilMask(0x00);
            }

            Texture texture = null;
            boolean useVertexColor=true;
            boolean useTexture=true;
            boolean useLighting=true;
            if(mmr.materialComponent!=null && mmr.materialComponent.getEnabled()) {
                MaterialComponent material = mmr.materialComponent;
                material.render(gl2);
                // flat light?
                useLighting &= material.isLit();
                // if we have a texture assigned, then we might still enable textures.
                texture = material.texture.getTexture();
                if(texture==null) useTexture = false;
                // assign the object's overall color.
                double[] diffuseColor = material.getDiffuseColor();
                shaderDefault.set4f(gl2,
                        "objectColor",
                        (float)diffuseColor[0],
                        (float)diffuseColor[1],
                        (float)diffuseColor[2],
                        (float)diffuseColor[3]);
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

            shaderProgram.set1i(gl2,"useVertexColor",useVertexColor?1:0);
            shaderProgram.set1i(gl2,"useLighting",useLighting?1:0);
            shaderProgram.set1i(gl2,"useTexture",useTexture?1:0);

            if(useTexture && texture!=null) {
                gl2.glEnable(GL.GL_TEXTURE_2D);
                texture.bind(gl2);
                mmr.materialComponent.render(gl2);
                shaderProgram.set1f(gl2,"diffuseTexture",0);
            }

            gl2.glPushMatrix();
            mmr.renderComponent.render(gl2);
            gl2.glPopMatrix();
        }
    }

    private void renderLights(GL2 gl2) {
        // global ambient light
        //gl2.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, ambientLight.getFloatArray(),0);

        int maxLights = getMaxLights(gl2);
        turnOffAllLights(gl2,maxLights);
        lights.clear();

        Queue<Entity> found = new LinkedList<>(entityManager.getEntities());
        int i=0;
        while(!found.isEmpty()) {
            Entity obj = found.remove();
            found.addAll(obj.getChildren());

            LightComponent light = obj.getComponent(LightComponent.class);
            if(light!=null && light.getEnabled()) {
                lights.add(light);
                light.setupLight(gl2,i++);
                if(i==maxLights) return;
            }
        }
    }

    private int getMaxLights(GL2 gl2) {
        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl2.glGetIntegerv(GL2.GL_MAX_LIGHTS, intBuffer);
        return intBuffer.get();
    }

    private void turnOffAllLights(GL2 gl2,int maxLights) {
        for(int i=0;i<maxLights;++i) {
            gl2.glDisable(GL2.GL_LIGHT0+i);
        }
    }

    private void drawCursor(GL2 gl2) {
        if(!isMouseIn) return;

        boolean tex = OpenGLHelper.disableTextureStart(gl2);
        boolean lit = OpenGLHelper.disableLightingStart(gl2);
        int top = OpenGLHelper.drawAtopEverythingStart(gl2);
/*
        shaderHUD.use(gl2);
        setOrthograpicMatrix(gl2,shaderHUD);
        shaderHUD.setMatrix4d(gl2,"viewMatrix",MatrixHelper.createIdentityMatrix4());
        shaderHUD.setMatrix4d(gl2,"modelMatrix",MatrixHelper.createIdentityMatrix4());
*/
        gl2.glUseProgram(0);

        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glPushMatrix();
        gl2.glLoadIdentity();
        viewport.renderOrthographic(gl2,1);
        gl2.glMatrixMode(GL2.GL_MODELVIEW);

        double [] cursor = viewport.getCursorAsNormalized();
        cursor[0] *= viewport.getCanvasWidth() / 2d;
        cursor[1] *= viewport.getCanvasHeight() / 2d;

        gl2.glPushMatrix();
        MatrixHelper.setMatrix(gl2, MatrixHelper.createIdentityMatrix4());
        gl2.glTranslated(cursor[0],cursor[1],-1);
        gl2.glBegin(GL2.GL_LINES);

        gl2.glColor3d(0,0,0);
        gl2.glVertex2d(1,-cursorSize);
        gl2.glVertex2d(1, cursorSize);
        gl2.glVertex2d(-cursorSize,1);
        gl2.glVertex2d( cursorSize,1);

        gl2.glVertex2d(-1,-cursorSize);
        gl2.glVertex2d(-1, cursorSize);
        gl2.glVertex2d(-cursorSize,-1);
        gl2.glVertex2d( cursorSize,-1);

        gl2.glColor4d(1,1,1,1);
        gl2.glVertex2d(0,-cursorSize);
        gl2.glVertex2d(0, cursorSize);
        gl2.glVertex2d(-cursorSize,0);
        gl2.glVertex2d( cursorSize,0);
        gl2.glEnd();
        gl2.glPopMatrix();

        OpenGLHelper.drawAtopEverythingEnd(gl2,top);
        OpenGLHelper.disableLightingEnd(gl2,lit);
        OpenGLHelper.disableTextureEnd(gl2,tex);

        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glPopMatrix();
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
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
        logger.debug("start the animation system");
        frameDelay=0;
        frameLength=1.0f/(float)DEFAULT_FRAMES_PER_SECOND;
        animator.add(glCanvas);
        // record the start time of the application, also the end of the core initialization process.
        lastTime = System.currentTimeMillis();
        // start the main application loop.  it will call display() repeatedly.
        animator.start();
    }

    @Override
    public void stopAnimationSystem() {
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
