package com.marginallyclever.robotoverlord;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.ColorParameter;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.SelectedItems;
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
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;

/**
 * Encapsulates the OpenGL rendering.
 * @author Dan Royer
 */
public class OpenGLRenderPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderPanel.class);
    private static final int FSAA_NUM_SAMPLES = 4;  // 1,2,4,8
    private static final int VERTICAL_SYNC_ON = 1;  // 1 on, 0 off
    private static final int DEFAULT_FRAMES_PER_SECOND = 30;

    private final Scene scene;

    // OpenGL debugging
    private final boolean glDebug=false;
    private final boolean glTrace=false;

    // should I check the state of the OpenGL stack size?  true=every frame, false=never
    private final boolean checkStackSize = false;

    // used to check the stack size.
    private final IntBuffer stackDepth = IntBuffer.allocate(1);

    // the render canvas
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
    private transient final ViewCube viewCube = new ViewCube();

    /**
     * The "very far away" background to the scene.
     */
    private transient final SkyBox sky = new SkyBox();

    private final List<EditorTool> editorTools = new ArrayList<>();
    private int activeToolIndex = -1;

    private final BooleanParameter showWorldOrigin = new BooleanParameter("Show world origin",false);

    private final ColorParameter ambientLight = new ColorParameter("Ambient light",0.2,0.2,0.2,1);
    private final MaterialComponent defaultMaterial = new MaterialComponent();

    /**
     * Used to sort items at render time. Opaque items are rendered first, then alpha items.
     */
    private static class MatrixMaterialRender {
        public Matrix4d matrix = new Matrix4d();
        public RenderComponent renderComponent;
        public MaterialComponent materialComponent;
    }
    private final List<MatrixMaterialRender> opaque = new ArrayList<>();
    private final List<MatrixMaterialRender> alpha = new ArrayList<>();
    private final List<MatrixMaterialRender> noMaterial = new ArrayList<>();

    public OpenGLRenderPanel(Scene scene) {
        super(new BorderLayout());
        this.scene = scene;

        createCanvas();
        addCanvasListeners();
        hideDefaultCursor();

        setMinimumSize(new Dimension(300, 300));
        add(setupTools(), BorderLayout.NORTH);
        add(glCanvas, BorderLayout.CENTER);

        startAnimationSystem();
    }

    private JToolBar setupTools() {
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
        JButton activateTranslateTool = new JButton("Translate");
        bar.add(activateTranslateTool);
        activateTranslateTool.addActionListener(e -> setActiveToolIndex(editorTools.indexOf(translateEntityMultiTool)));

        JButton activateRotateTool = new JButton("Rotate");
        bar.add(activateRotateTool);
        activateRotateTool.addActionListener(e -> setActiveToolIndex(editorTools.indexOf(rotateEntityMultiTool)));

        return bar;
    }

    public void setActiveToolIndex(int activeToolIndex) {
        deactivateAllTools();
        this.activeToolIndex = activeToolIndex;
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
            logger.info("...set caps");
            caps.setBackgroundOpaque(true);
            caps.setDoubleBuffered(true);
            caps.setHardwareAccelerated(true);
            if(FSAA_NUM_SAMPLES>1) {
                caps.setSampleBuffers(true);
                caps.setNumSamples(FSAA_NUM_SAMPLES);
            }
            logger.info("...create panel");
            glCanvas = new GLJPanel(caps);
        } catch(GLException e) {
            logger.error("Failed the first call to OpenGL.  Are your native drivers missing?");
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

                int [] buf = new int[1];
                int [] sbuf = new int[1];
                gl2.glGetIntegerv(GL2.GL_SAMPLES, buf, 0);
                gl2.glGetIntegerv(GL2.GL_SAMPLE_BUFFERS, sbuf, 0);

                // depth testing and culling options
                gl2.glDepthFunc(GL2.GL_LESS);
                gl2.glEnable(GL2.GL_DEPTH_TEST);
                gl2.glDepthMask(true);

                // Scale normals using the scale of the transform matrix so that lighting is sane.
                // This is more efficient than gl2.glEnable(GL2.GL_NORMALIZE);
                //gl2.glEnable(GL2.GL_RESCALE_NORMAL);
                gl2.glEnable(GL2.GL_NORMALIZE);

                // default blending option for transparent materials
                gl2.glEnable(GL2.GL_BLEND);
                gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

                // set the color to use when wiping the draw buffer
                gl2.glClearColor(0.85f,0.85f,0.85f,1.0f);
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
                viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
                viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
            }

            @Override
            public void dispose( GLAutoDrawable drawable ) {}

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
                // if they dragged the cursor around before releasing the mouse button, don't pick.
                if (e.getClickCount() == 2) {
                    pickItemUnderCursor();
                }
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

    private void pickItemUnderCursor() {
        Entity found = findEntityUnderCursor();
        System.out.println((found==null)?"found=null":"found=" + found.getName());

        List<Entity> list = new ArrayList<>();
        if(found!=null) list.add(found);
        UndoSystem.addEvent(this,new SelectEdit(Clipboard.getSelectedEntities(),list));
    }

    /**
     * test ray intersection with all entities in the scene.
     * @param ray the ray to test.
     */
    public List<RayHit> findRayIntersections(Ray ray) {
        List<RayHit> rayHits = new ArrayList<>();

        Queue<Entity> toTest = new LinkedList<>(scene.getChildren());
        while(!toTest.isEmpty()) {
            Entity entity = toTest.remove();
            toTest.addAll(entity.getChildren());

            List<ShapeComponent> shapes = entity.findAllComponents(ShapeComponent.class);
            for(ShapeComponent shape : shapes) {
                RayHit hit = shape.intersect(ray);
                if(hit!=null) rayHits.add(hit);
            }
        }
        return rayHits;
    }

    private CameraComponent getCamera() {
        return scene.getCamera();
    }

    private void checkRenderStep(GL2 gl2) {
        if(checkStackSize) {
            gl2.glGetIntegerv(GL2.GL_MODELVIEW_STACK_DEPTH, stackDepth);
            logger.debug("stack depth start = " + stackDepth.get(0));
        }

        renderStep(gl2);

        if(checkStackSize) {
            gl2.glGetIntegerv(GL2.GL_MODELVIEW_STACK_DEPTH, stackDepth);
            logger.debug("stack depth end = " + stackDepth.get(0));
        }
    }

    private void renderStep(GL2 gl2) {
        CameraComponent camera = getCamera();
        if(camera==null) {
            gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
            // TODO display a "no active camera found" message?
            return;
        }
        gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);

        viewport.setCamera(camera);
        viewport.renderChosenProjection(gl2);

        sky.render(gl2,camera);

        renderLights(gl2);
        renderAllEntities(gl2);
        // PASS 2: everything transparent?
        //renderAllBoundingBoxes(gl2);
        if(showWorldOrigin.get()) PrimitiveSolids.drawStar(gl2,10);

        //viewport.showPickingTest(gl2);

        gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);

        // 3D overlays
        for(EditorTool tool : editorTools) tool.render(gl2);

        // 2D overlays
        viewCube.render(gl2,viewport);
        drawCursor(gl2);
    }

    /**
     * Render all Entities in the scene.  Search all entities for a {@link RenderComponent}.
     * Sort them into three lists: those with no material, those with opaque material, and those with transparent
     * material.  Further sort the alpha list by distance from the camera.  Then render the opaque, render the alpha,
     * and render the no-material.
     *
     * @param gl2 the OpenGL context
     */
    private void renderAllEntities(GL2 gl2) {
        opaque.clear();
        alpha.clear();
        noMaterial.clear();

        // collect all entities with a RenderComponent
        Queue<Entity> toRender = new LinkedList<>(scene.getChildren());
        while(!toRender.isEmpty()) {
            Entity entity = toRender.remove();
            toRender.addAll(entity.getChildren());

            RenderComponent renderComponent = entity.findFirstComponent(RenderComponent.class);
            if(renderComponent!=null) {
                MatrixMaterialRender mmr = new MatrixMaterialRender();
                mmr.renderComponent = entity.findFirstComponent(RenderComponent.class);
                mmr.materialComponent = entity.findFirstComponent(MaterialComponent.class);
                PoseComponent pose = entity.findFirstComponent(PoseComponent.class);
                if(pose!=null) mmr.matrix.set(pose.getWorld());

                if(mmr.materialComponent==null) noMaterial.add(mmr);
                else if(mmr.materialComponent.isAlpha()) alpha.add(mmr);
                else opaque.add(mmr);
            }
        }

        // render opaque objects
        defaultMaterial.render(gl2);
        renderMMRList(gl2,opaque);

        // sort alpha objects back to front
        Vector3d cameraPoint = new Vector3d();
        Entity cameraEntity = getCamera().getEntity();
        cameraEntity.findFirstComponent(PoseComponent.class).getWorld().get(cameraPoint);

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
        // render alpha objects
        renderMMRList(gl2,alpha);

        // render objects with no material last
        defaultMaterial.render(gl2);
        renderMMRList(gl2,noMaterial);
    }

    private void renderMMRList(GL2 gl2, List<MatrixMaterialRender> list) {
        for(MatrixMaterialRender ems : list) {
            gl2.glPushMatrix();
            if(ems.matrix!=null) {
                MatrixHelper.applyMatrix(gl2,ems.matrix);
            }
            if(ems.materialComponent!=null && ems.materialComponent.getEnabled()) {
                ems.materialComponent.render(gl2);
            }
            if(ems.renderComponent!=null && ems.renderComponent.getVisible()) {
                ems.renderComponent.render(gl2);
            }
            gl2.glPopMatrix();
        }
    }

    private void renderLights(GL2 gl2) {
        // global ambient light
        gl2.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, ambientLight.getFloatArray(),0);

        int maxLights = getMaxLights(gl2);
        turnOffAllLights(gl2,maxLights);

        Queue<Entity> found = new LinkedList<>(scene.getChildren());
        int i=0;
        while(!found.isEmpty()) {
            Entity obj = found.remove();
            found.addAll(obj.children);

            LightComponent light = obj.findFirstComponent(LightComponent.class);
            if(light!=null && light.getEnabled()) {
                light.setupLight(gl2,i++);
                if(i==maxLights) return;
            }
        }
    }

    public int getMaxLights(GL2 gl2) {
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

        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glPushMatrix();
        MatrixHelper.setMatrix(gl2, MatrixHelper.createIdentityMatrix4());
        viewport.renderOrthographic(gl2,1);
        gl2.glMatrixMode(GL2.GL_MODELVIEW);

        double [] cursor = viewport.getCursorAsNormalized();
        cursor[0] *= viewport.getCanvasWidth() / 2d;
        cursor[1] *= viewport.getCanvasHeight() / 2d;

        gl2.glPushMatrix();
        MatrixHelper.setMatrix(gl2, MatrixHelper.createIdentityMatrix4());
        gl2.glTranslated(cursor[0],cursor[1],-1);
        gl2.glBegin(GL2.GL_LINES);
        gl2.glColor4d(1,1,1,1);
        double v = 10;
        gl2.glVertex2d(0,-v);
        gl2.glVertex2d(0, v);
        gl2.glVertex2d(-v,0);
        gl2.glVertex2d( v,0);
        gl2.glColor3d(0,0,0);
        gl2.glVertex2d(1,-v);
        gl2.glVertex2d(1, v);
        gl2.glVertex2d(-v,1);
        gl2.glVertex2d( v,1);
        gl2.glVertex2d(-1,-v);
        gl2.glVertex2d(-1, v);
        gl2.glVertex2d(-v,-1);
        gl2.glVertex2d( v,-1);
        gl2.glEnd();
        gl2.glPopMatrix();

        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glPopMatrix();
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
    }

    private void updateStep(double dt) {
        frameDelay+=dt;
        if(frameDelay>frameLength) {
            frameDelay-=frameLength;
            scene.update(frameLength);
        }
    }

    public void startAnimationSystem() {
        logger.debug("setup the animation system");
        frameDelay=0;
        frameLength=1.0f/(float)DEFAULT_FRAMES_PER_SECOND;
        animator.add(glCanvas);
        // record the start time of the application, also the end of the core initialization process.
        lastTime = System.currentTimeMillis();
        // start the main application loop.  it will call display() repeatedly.
        animator.start();
    }

    public void stopAnimationSystem() {
        animator.stop();
    }

    /**
     * Use ray tracing to find the Entity at the cursor position closest to the camera.
     * @return the name of the item under the cursor, or -1 if nothing was picked.
     */
    private Entity findEntityUnderCursor() {
        CameraComponent camera = getCamera();
        if(camera==null) return null;

        Ray ray = viewport.getRayThroughCursor();

        // traverse the scene Entities and find the ShapeComponent that collides with the ray.
        List<RayHit> rayHits = findRayIntersections(ray);
        if(rayHits.size()==0) return null;

        rayHits.sort(Comparator.comparingDouble(o -> o.distance));

        return rayHits.get(0).target.getEntity();
    }

    public void updateSubjects(List<Entity> list) {
        SelectedItems selectedItems = new SelectedItems(list);
        if(activeToolIndex>=0) {
            editorTools.get(activeToolIndex).deactivate();
            editorTools.get(activeToolIndex).activate(selectedItems);
        }
    }

    public Viewport getViewport() {
        return viewport;
    }
}
