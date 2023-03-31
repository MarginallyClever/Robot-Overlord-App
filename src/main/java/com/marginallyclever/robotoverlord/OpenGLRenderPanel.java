package com.marginallyclever.robotoverlord;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.Ray;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entities.SkyBoxEntity;
import com.marginallyclever.robotoverlord.entities.ViewCube;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.move.MoveCameraTool;
import com.marginallyclever.robotoverlord.tools.move.MoveEntityTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the OpenGL rendering.
 * @author Dan Royer
 */
public class OpenGLRenderPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderPanel.class);
    private static final int FSAA_NUM_SAMPLES = 4;
    private static final int VERTICAL_SYNC_ON = 1;  // 1 on, 0 off
    private static final int DEFAULT_FRAMES_PER_SECOND = 30;
    private static final int PICK_BUFFER_SIZE = 256;

    private final RobotOverlord robotOverlord;
    private final Scene scene;

    // OpenGL debugging
    private final boolean glDebug=false;
    private final boolean glTrace=false;

    // should I check the state of the OpenGL stack size?  true=every frame, false=never
    private final boolean checkStackSize = false;

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
    private transient boolean pickNow = false;
    private final Viewport viewport = new Viewport();

    // elements in view, not really part of the scene
    private transient final ViewCube viewCube = new ViewCube();
    private transient final SkyBoxEntity sky = new SkyBoxEntity();

    private transient final MoveEntityTool moveEntityTool;
    private transient final MoveCameraTool moveCameraTool = new MoveCameraTool();

    private final List<EditorTool> editorTools = new ArrayList<>();

    private final IntBuffer pickBuffer = Buffers.newDirectIntBuffer(PICK_BUFFER_SIZE);


    public OpenGLRenderPanel(RobotOverlord robotOverlord,Scene scene) {
        super(new BorderLayout());
        this.robotOverlord = robotOverlord;
        this.scene = scene;

        moveEntityTool = new MoveEntityTool(robotOverlord);

        createCanvas();
        addCanvasListeners();
        hideDefaultCursor();

        this.setMinimumSize(new Dimension(300, 300));
        this.add(glCanvas, BorderLayout.CENTER);

        setupTools();
    }

    private void setupTools() {
        editorTools.add(moveEntityTool);
        editorTools.add(moveCameraTool);
    }

    private void hideDefaultCursor() {
        Cursor noCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0),
                "blank cursor");
        this.setCursor(noCursor);
    }

    private void createCanvas() {
        try {
            Log.message("...get default caps");
            GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
            Log.message("...set caps");
            caps.setBackgroundOpaque(true);
            caps.setDoubleBuffered(true);
            caps.setHardwareAccelerated(true);
            if(FSAA_NUM_SAMPLES>1) {
                caps.setSampleBuffers(true);
                caps.setNumSamples(FSAA_NUM_SAMPLES);
            }
            Log.message("...create panel");
            glCanvas = new GLJPanel(caps);
        } catch(GLException e) {
            Log.error("Failed the first call to OpenGL.  Are your native drivers missing?");
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

                if(checkStackSize) checkRenderStep(gl2);
                else renderStep(gl2);
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
                for(EditorTool tool : editorTools) tool.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for(EditorTool tool : editorTools) tool.mouseReleased(e);
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
                for(EditorTool tool : editorTools) tool.mouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                viewport.setCursor(e.getX(),e.getY());
                for(EditorTool tool : editorTools) tool.mouseMoved(e);
            }
        });

        glCanvas.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                for(EditorTool tool : editorTools) tool.mouseWheelMoved(e);
            }
        });

        glCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                for(EditorTool tool : editorTools) tool.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                for(EditorTool tool : editorTools) tool.keyReleased(e);
            }
        });
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
        Log.message("using GL debug pipeline");
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
        UndoSystem.addEvent(this,new SelectEdit(robotOverlord,robotOverlord.getSelectedEntities(),found));
    }

    private CameraComponent getCamera() {
        return robotOverlord.findFirstComponentRecursive(CameraComponent.class);
    }

    private void checkRenderStep(GL2 gl2) {
        IntBuffer stackDepth = IntBuffer.allocate(1);
        gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,stackDepth);
        logger.debug("stack depth start = "+stackDepth.get(0));

        renderStep(gl2);

        gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,stackDepth);
        logger.debug("stack depth end = "+stackDepth.get(0));
    }

    private void renderStep(GL2 gl2) {
        CameraComponent camera = getCamera();
        if(camera==null) {
            gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
            // TODO display a "no active camera found" message?
            return;
        }
        gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);

        moveCameraTool.setCamera(camera);
        viewport.setCamera(camera);
        viewport.renderChosenProjection(gl2);

        sky.render(gl2,camera);
        scene.render(gl2);

        //viewport.showPickingTest(gl2);

        // 3D overlays
        moveEntityTool.render(gl2);

        // 2D overlays
        gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        viewCube.render(gl2,viewport);
        drawCursor(gl2);
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
        Entity found = null;

        return found;
    }

    public void updateSubjects() {
        moveEntityTool.setSubject(null);

        List<Entity> list = robotOverlord.getSelectedEntities();
        if( !list.isEmpty()) {
            if(list.size() == 1) {
                Entity firstEntity = list.get(0);
                if(firstEntity.findFirstComponent(PoseComponent.class) != null) {
                    moveEntityTool.setSubject(firstEntity);
                }
            }
        }
    }

    public Viewport getViewport() {
        return viewport;
    }
}
