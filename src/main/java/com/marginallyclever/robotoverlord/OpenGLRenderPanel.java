package com.marginallyclever.robotoverlord;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entities.SkyBoxEntity;
import com.marginallyclever.robotoverlord.entities.ViewCube;
import com.marginallyclever.robotoverlord.swinginterface.InputManager;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
import com.marginallyclever.robotoverlord.tools.move.MoveTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the OpenGL rendering.
 * @author Dan Royer
 */
public class OpenGLRenderPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderPanel.class);
    private static final int FSAA_NUM_SAMPLES = 3;
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

    private transient final MoveTool moveTool;

    public OpenGLRenderPanel(RobotOverlord robotOverlord,Scene scene) {
        super(new BorderLayout());
        this.robotOverlord = robotOverlord;
        this.scene = scene;
        moveTool = new MoveTool(robotOverlord);

        createCanvas();
        addCanvasListeners();

        this.setMinimumSize(new Dimension(300,300));
        this.add(glCanvas,BorderLayout.CENTER);
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
                gl2.glEnable(GL2.GL_NORMALIZE);
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
                // This is more efficient than gl2.gleEnable(GL2.GL_NORMALIZE);
                //gl2.glEnable(GL2.GL_RESCALE_NORMAL);
                //gl2.glEnable(GL2.GL_NORMALIZE);

                // default blending option for transparent materials
                gl2.glEnable(GL2.GL_BLEND);
                gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

                // set the color to use when wiping the draw buffer
                gl2.glClearColor(0.85f,0.85f,0.85f,1.0f);

                // draw to the back buffer, so we can swap buffer later and avoid vertical sync tearing
                gl2.glDrawBuffer(GL2.GL_BACK);
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
                // set up the projection matrix
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

                pickStep(gl2);
            }
        });

        // this class also listens to the glCanvas (messy!)
        glCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // if they dragged the cursor around before releasing the mouse button, don't pick.
                if (e.getClickCount() == 2) {
                    pickNow=true;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    viewport.pressed();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if(SwingUtilities.isLeftMouseButton(e)) {
                    viewport.released();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isMouseIn=true;
                glCanvas.requestFocus();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isMouseIn=false;
            }
        });

        // this class also listens to the mouse button clicks.
        glCanvas.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                viewport.setCursor(e.getX(),e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                viewport.setCursor(e.getX(),e.getY());
            }
        });  // this class also listens to the mouse movement.
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

    private void pickStep(GL2 gl2) {
        if(!pickNow) return;
        pickNow = false;

        int pickName = findItemUnderCursor(gl2,getCamera());
        Entity next = scene.pickEntityWithName(pickName);

        UndoSystem.addEvent(this,new SelectEdit(robotOverlord,robotOverlord.getSelectedEntities(),next));
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

        viewport.setCamera(camera);
        viewport.renderChosenProjection(gl2);

        sky.render(gl2,camera);
        scene.render(gl2);
        // overlays
        moveTool.render(gl2);
        viewCube.render(gl2,viewport);
    }

    private void updateStep(double dt) {
        frameDelay+=dt;
        if(frameDelay>frameLength) {
            frameDelay-=frameLength;
            InputManager.update(isMouseIn);
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
     * Use glRenderMode(GL_SELECT) to ray pick the item under the cursor.
     * See <a href="https://github.com/sgothel/jogl-demos/blob/master/src/demos/misc/Picking.java">1</a>
     * and <a href="http://web.engr.oregonstate.edu/~mjb/cs553/Handouts/Picking/picking.pdf">2</a>
     * @param gl2 the openGL render context
     * @return the name of the item under the cursor, or -1 if nothing was picked.
     */
    private int findItemUnderCursor(GL2 gl2,CameraComponent cameraComponent) {
        if(gl2==null || cameraComponent==null) return -1;

        IntBuffer pickBuffer = Buffers.newDirectIntBuffer(PICK_BUFFER_SIZE);
        gl2.glSelectBuffer(PICK_BUFFER_SIZE, pickBuffer);

        gl2.glRenderMode( GL2.GL_SELECT );
        // wipe the select buffer
        gl2.glInitNames();

        viewport.renderPick(gl2,cameraComponent);

        gl2.glLoadName(0);
        // render in selection mode, without advancing time in the simulation.
        scene.render(gl2);

        gl2.glPopName();
        gl2.glFlush();

        // get the picking results and return the render mode to the default
        int hits = gl2.glRenderMode( GL2.GL_RENDER );

        return getPickNameFromPickList(pickBuffer,hits,false);
    }

    private int getPickNameFromPickList(IntBuffer pickBuffer,int hits,boolean verbose) {
        if(verbose) logger.debug(hits+" PICKS @ "+ Arrays.toString(viewport.getCursor()));

        float zMinBest = Float.MAX_VALUE;
        int i, index=0, bestPick=0;

        for(i=0;i<hits;++i) {
            if(verbose) describePickBuffer(pickBuffer,index);

            int nameCount=pickBuffer.get(index++);
            float z1 = (float) (pickBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;
            @SuppressWarnings("unused")
            float z2 = (float) (pickBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;

            index+=nameCount;
            if(nameCount>0 && zMinBest > z1) {
                zMinBest = z1;
                bestPick = pickBuffer.get(index-1);
            }
        }
        return bestPick;
    }

    private void describePickBuffer(IntBuffer pickBuffer, int index) {
        int nameCount=pickBuffer.get(index++);
        float z1 = (float) (pickBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;
        float z2 = (float) (pickBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;

        StringBuilder msg= new StringBuilder("  names=" + nameCount + " zMin=" + z1 + " zMax=" + z2 + ": ");
        String add="";
        int pickName;
        for(int j=0;j<nameCount;++j) {
            pickName = pickBuffer.get(index++);
            msg.append(add).append(pickName);
            add=", ";
        }
        logger.debug(msg.toString());
    }

    public void updateSubjects() {
        moveTool.setSubject(null);

        List<Entity> list = robotOverlord.getSelectedEntities();
        if( !list.isEmpty()) {
            if(list.size() == 1) {
                Entity firstEntity = list.get(0);
                if(firstEntity.findFirstComponent(PoseComponent.class) != null) {
                    moveTool.setSubject(firstEntity);
                }
            }
        }
    }

    public Viewport getViewport() {
        return viewport;
    }
}
