package com.marginallyclever.robotoverlord;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.demos.*;
import com.marginallyclever.robotoverlord.entities.SkyBoxEntity;
import com.marginallyclever.robotoverlord.entities.ViewCube;
import com.marginallyclever.robotoverlord.movetool.MoveTool;
import com.marginallyclever.robotoverlord.swinginterface.*;
import com.marginallyclever.robotoverlord.swinginterface.actions.*;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanel;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanelEvent;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.util.PropertiesFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.awt.Component;
import java.awt.*;
import java.awt.event.*;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * {@code RobotOverlord} is the top-level controller of an application to educate robots.
 * It is built around good design patterns.
 * See <a href="https://github.com/MarginallyClever/Robot-Overlord-App">https://github.com/MarginallyClever/Robot-Overlord-App</a>
 *
 * @author Dan Royer
 */
public class RobotOverlord extends Entity {
	private static final Logger logger = LoggerFactory.getLogger(RobotOverlord.class);

	public static final String APP_TITLE = "Robot Overlord";
	public static final String APP_URL = "https://github.com/MarginallyClever/Robot-Overlord";
	private static final int FSAA_NUM_SAMPLES = 3;
	private static final int VERTICAL_SYNC_ON = 1;  // 1 on, 0 off
	private static final int DEFAULT_FRAMES_PER_SECOND = 30;
    public static final int PICK_BUFFER_SIZE = 256;

	public static final FileNameExtensionFilter FILE_FILTER = new FileNameExtensionFilter("RO files", "RO");

	// used for checking the application version with the GitHub release, for "there is a new version available!" notification
	public static final String VERSION = PropertiesFileHelper.getVersionPropertyValue();
	// settings
	private final Preferences prefs = Preferences.userRoot().node("Evil Overlord");  // Secretly evil?  Nice.
    //private RecentFiles recentFiles = new RecentFiles();
    
    private final Scene scene = new Scene();
	private transient final List<Entity> selectedEntities = new ArrayList<>();
	private transient Entity copiedEntities = new Entity();

	/**
	 * The list of actions registered in the editor.  This list is used for calls to
	 * {@link #updateActionEnableStatus()}.
	 */
	private final ArrayList<AbstractAction> actions = new ArrayList<>();

	private final MoveTool moveTool = new MoveTool();
	private transient final ViewCube viewCube = new ViewCube();
	
	// The main frame of the GUI
	private JFrame mainFrame; 
	private static JFrame logFrame;
    private JMenuBar mainMenu;
	private final JSplitPane splitLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private final JSplitPane rightFrameSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final EntityTreePanel entityTree = new EntityTreePanel();
	private final ComponentPanel componentPanel = new ComponentPanel();
	
	private RenameEntityAction renameEntityAction;
	private DeleteEntityAction deleteEntityAction;

	private final FPSAnimator animator = new FPSAnimator(DEFAULT_FRAMES_PER_SECOND);
	private GLJPanel glCanvas;
	
	// should I check the state of the OpenGL stack size?  true=every frame, false=never
	private final boolean checkStackSize = false;
	
	// mouse steering controls
	private boolean isMouseIn=false;

	private final Viewport viewport = new Viewport();
	
    // timing for animations
    private long lastTime;
    private double frameDelay;
    private double frameLength;

	// click on screen to change which entity is selected
	protected transient boolean pickNow = false;
	protected transient Vector2d pickPoint = new Vector2d();

	public final SkyBoxEntity sky = new SkyBoxEntity();
	
 	private RobotOverlord() {
 		super();
 		setName("");
		this.addComponent(new PoseComponent());
 		 		
		if(GraphicsEnvironment.isHeadless()) {
			throw new RuntimeException("RobotOverlord cannot be run headless yet.");
		}

		Translator.start();
		SoundSystem.start();
		InputManager.start();

		buildMainFrame();
		buildMainMenu();
		createSimulationPanel();
		layoutComponents();
		startAnimationSystem();

		entityTree.addEntity(scene);
		scene.addSceneChangeListener(entityTree);

		addEntity(sky);
		addEntity(viewport);
		addEntity(scene);
		addEntity(moveTool);
		addEntity(viewCube);

		NewSceneAction action = new NewSceneAction("New Scene",this);
		action.resetScene();

		Log.message("** READY **");
    }

	private void createSimulationPanel() {
		createCanvas();
        addCanvasListeners();
        glCanvas.setMinimumSize(new Dimension(300,300));
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
			private final boolean glDebug=false;
			private final boolean glTrace=false;

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
		});  // this class also listens to the glcanvas (messy!) 
		glCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// if they dragged the cursor around before releasing the mouse button, don't pick.
				if (e.getClickCount() == 2) {
					pickPoint.set(e.getX(),e.getY());
					pickNow=true;
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					pickPoint.set(e.getX(),e.getY());
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
		});  // this class also listens to the mouse button clicks.
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
	
	public static void main(String[] argv) {
		logFrame = LogPanel.createFrame();
		Log.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}
		
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(RobotOverlord::new);
	}

	private JComponent buildEntityManagerPanel() {
        Log.message("buildEntityManagerPanel()");

		return buildEntityTree();
	}

	private JComponent buildEntityTree() {
		entityTree.addEntityTreePanelListener((e)-> {
			if (e.eventType == EntityTreePanelEvent.SELECT) {
				setSelectedEntities(e.subjects);
			}
		});

		entityTree.setPopupMenu(buildEntityTreePopupMenu());

		return entityTree;
	}

	private JPopupMenu buildEntityTreePopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		AddChildEntityAction addChildEntityAction = new AddChildEntityAction(Translator.get("AddChildEntityAction.name"),this);
		addChildEntityAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("AddChildEntityAction.shortDescription"));

		renameEntityAction =new RenameEntityAction(Translator.get("RenameEntityAction.name"),this);
		renameEntityAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("RenameEntityAction.shortDescription"));
		renameEntityAction.setEnabled(false);
		actions.add(renameEntityAction);
		actions.add(addChildEntityAction);

		popupMenu.add(addChildEntityAction);
		popupMenu.add(renameEntityAction);
		popupMenu.add(deleteEntityAction);

		popupMenu.add(new AddComponentAction(this));

		return popupMenu;
	}

	private void layoutComponents() {
        Log.message("layoutComponents()");
        
		// the right hand stuff			        
		rightFrameSplitter.add(buildEntityManagerPanel());
		rightFrameSplitter.add(new JScrollPane(componentPanel));
		// make sure the master panel can't be squished.
        Dimension minimumSize = new Dimension(360,300);
        rightFrameSplitter.setMinimumSize(minimumSize);
        // if the window resizes, give top and bottom halves equal share of the real estate
		rightFrameSplitter.setResizeWeight(0.5);

        Log.message("build splitters");
        splitLeftRight.add(glCanvas);
        splitLeftRight.add(rightFrameSplitter);
        // if the window resizes, give left half as much real estate as it can get.
        splitLeftRight.setResizeWeight(1);

        mainFrame.add(splitLeftRight);
        
        mainFrame.setJMenuBar(mainMenu);
 	}

	private void buildMainFrame() {
		Log.message("buildMainFrame()");
		// start the main application frame - the largest visible rectangle on the screen with the minimize/maximize/close buttons.
        mainFrame = new JFrame( APP_TITLE + " " + VERSION ); 
    	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setLayout(new java.awt.BorderLayout());
        mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        mainFrame.setVisible(true);
    	setWindowSizeAndPosition();
       
        mainFrame.addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
        	@Override
        	public void windowClosing(WindowEvent e) {
        		confirmClose();
        		super.windowClosing(e);
        	}
        	
    		// switch back to this window
        	@Override
            public void windowActivated(WindowEvent e) {
        		super.windowActivated(e);
        		InputManager.focusGained();
        	}

    		// switch away to another window
        	@Override
            public void windowDeactivated(WindowEvent e) {
        		super.windowDeactivated(e);
        		InputManager.focusLost();
        	}
		});
	}

	private void setWindowSizeAndPosition() {
		Log.message("Set window size and position");

    	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    	
    	int windowW = prefs.getInt("windowWidth", -1);
    	int windowH = prefs.getInt("windowHeight", -1);
    	if(windowW==-1 || windowH==-1) {
    		Log.message("...default size");
    		windowW = dim.width;
    		windowH = dim.height;
    	}
        mainFrame.setSize( windowW, windowH );
    	
    	int windowX = prefs.getInt("windowX", -1);
    	int windowY = prefs.getInt("windowY", -1);
        if(windowX==-1 || windowY==-1) {
    		Log.message("...default position");
        	// centered
        	windowX = (dim.width - windowW)/2;
        	windowY = (dim.height - windowH)/2;
        }
        
        mainFrame.setLocation( windowX, windowY );
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}
		
	public Scene getScene() {
		return scene;
	}
	
	public List<Entity> getSelectedEntities() {
		return new ArrayList<>(selectedEntities);
	}

	public void buildMainMenu() {
		Log.message("buildMainMenu()");
		
		mainMenu = new JMenuBar();
		mainMenu.removeAll();
		mainMenu.add(createFileMenu());
		mainMenu.add(createDemoMenu());
		mainMenu.add(createEditMenu());
		mainMenu.add(createHelpMenu());
        mainMenu.updateUI();
	}

	private Component createFileMenu() {
		JMenu menu = new JMenu(APP_TITLE);

		NewSceneAction newSceneAction = new NewSceneAction(Translator.get("NewSceneAction.name"),this);
		newSceneAction.putValue(Action.SMALL_ICON,new UnicodeIcon("🌱"));
		newSceneAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("NewSceneAction.shortDescription"));
		newSceneAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK) );

		LoadSceneAction loadSceneAction = new LoadSceneAction(Translator.get("LoadSceneAction.name"),this);
		loadSceneAction.putValue(Action.SMALL_ICON,new UnicodeIcon("🗁"));
		loadSceneAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("LoadSceneAction.shortDescription"));
		loadSceneAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK) );

		SaveSceneAction saveSceneAction = new SaveSceneAction(Translator.get("SaveSceneAction.name"),this);
		saveSceneAction.putValue(Action.SMALL_ICON,new UnicodeIcon("💾"));
		saveSceneAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("SaveSceneAction.shortDescription"));
		saveSceneAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK) );

		menu.add(newSceneAction);
		menu.add(loadSceneAction);
		menu.add(saveSceneAction);
		menu.add(new JSeparator());
		menu.add(new QuitAction(this));
		return menu;
	}

	private Component createDemoMenu() {
		JMenu menu = new JMenu("Demos");
		menu.add(new JMenuItem(new DemoAction(this,new BasicDemo())));
		menu.add(new JMenuItem(new DemoAction(this,new RobotArmsDemo())));
		menu.add(new JMenuItem(new DemoAction(this,new ODEPhysicsDemo())));
		menu.add(new JMenuItem(new DemoAction(this,new PhysicsDemo())));
		menu.add(new JMenuItem(new DemoAction(this,new DogDemo())));
		menu.add(new JMenuItem(new DemoAction(this,new SkycamDemo())));
		menu.add(new JMenuItem(new DemoAction(this,new StewartPlatformDemo())));
		return menu;
	}

	private Component createEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.add(new JMenuItem(UndoSystem.getCommandUndo()));
		menu.add(new JMenuItem(UndoSystem.getCommandRedo()));
		menu.add(new JSeparator());

		CopyEntityAction copyEntityAction = new CopyEntityAction(Translator.get("CopyEntityAction.name"),this);
		PasteEntityAction pasteEntityAction = new PasteEntityAction(Translator.get("PasteEntityAction.name"),this);
		deleteEntityAction = new DeleteEntityAction(Translator.get("DeleteEntityAction.name"),this);
		CutEntityAction cutEntityAction = new CutEntityAction(Translator.get("CutEntityAction.name"), deleteEntityAction,copyEntityAction);

		copyEntityAction.putValue(Action.SMALL_ICON,new UnicodeIcon("📋"));
		copyEntityAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("CopyEntityAction.shortDescription"));
		copyEntityAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK) );

		pasteEntityAction.putValue(Action.SMALL_ICON,new UnicodeIcon("📎"));
		pasteEntityAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("PasteEntityAction.shortDescription"));
		pasteEntityAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK) );

		deleteEntityAction.putValue(Action.SMALL_ICON,new UnicodeIcon("🗑"));
		deleteEntityAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("DeleteEntityAction.shortDescription"));
		deleteEntityAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0) );

		cutEntityAction.putValue(Action.SMALL_ICON,new UnicodeIcon("✂️"));
		cutEntityAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("CutEntityAction.shortDescription"));
		cutEntityAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK) );

		menu.add(copyEntityAction);
		menu.add(pasteEntityAction);
		menu.add(cutEntityAction);
		menu.add(deleteEntityAction);

		actions.add(copyEntityAction);
		actions.add(pasteEntityAction);
		actions.add(cutEntityAction);
		actions.add(deleteEntityAction);

		return menu;
	}

	private Component createHelpMenu() {
		JMenu menu = new JMenu("Help");
		JMenuItem buttonViewLog = new JMenuItem("Show Log");
		buttonViewLog.addActionListener((e) -> showLogDialog() );
		menu.add(buttonViewLog);
		menu.add(new JMenuItem(new AboutControlsAction()));
		menu.add(new JMenuItem(new ForumsAction()));
		menu.add(new JMenuItem(new CheckForUpdateAction()));
		menu.add(new JMenuItem(new AboutAction()));
		return menu;
	}

	private void showLogDialog() {
		logFrame.setVisible(true);
	}

    /**
     * An entity in the 3D scene has been "picked" (aka double-clicked).  This begins the process
     * to select that entity.  Entities selected through other means should not call pickEntity() as it would
     * cause an infinite loop.
     *
     * @param e the entity that was picked
     */
	private void pickEntity(Entity e) {
		//Log.message( "Picked "+((e==null)?"nothing":e.getFullPath()) );
		if(selectedEntities.contains(e)) return;
		entityTree.setSelection(e);
	}
	
    private void updateSelectEntities() {
		if(renameEntityAction !=null) renameEntityAction.setEnabled(false);

		moveTool.setSubject(null);

		List<Entity> list = getSelectedEntities();
    	if( !list.isEmpty()) {
			if(list.size() == 1) {
				Entity firstEntity = list.get(0);
				pickEntity(firstEntity);
				if(firstEntity.getComponent(PoseComponent.class) != null) {
					moveTool.setSubject(firstEntity);
				}
			}
    	}
		updateComponentPanel();
	}

	public void updateComponentPanel() {
		componentPanel.update(getSelectedEntities(),this);
	}

    private void saveWindowSizeAndPosition() {
		// remember window location for next time.
    	logger.debug("saveWindowSizeAndPosition()");
    	
		Dimension d = mainFrame.getSize();
    	prefs.putInt("windowWidth", d.width);
    	prefs.putInt("windowHeight", d.height);
    	Point p = mainFrame.getLocation();
    	prefs.putInt("windowX", p.x);
    	prefs.putInt("windowY", p.y);
	}
	
	public void confirmClose() {
        int result = JOptionPane.showConfirmDialog(
            mainFrame,
            "Are you sure you want to quit?",
            "Quit",
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
        	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	saveWindowSizeAndPosition();
			
        	// Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
	        new Thread(() -> {
				animator.stop();
				mainFrame.dispose();
			}).start();
        }
	}
	
	/**
	 * Deep search for a child with this name.
	 * @param name the name to match
	 * @return the entity.  null if nothing found.
	 */
	public Entity findEntityWithName(String name) {
		ArrayList<Entity> list = new ArrayList<>();
		list.add(scene);
		while( !list.isEmpty() ) {
			Entity obj = list.remove(0);
			String objectName = obj.getName();
			if(name.equals(objectName)) return obj;
			list.addAll(obj.getEntities());
		}
		return null;
	}

    private GL useTracePipeline(GL gl) {
        try {
            return gl.getContext().setGL( GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[] { System.err } ) );
        } catch (Exception e) {
        	e.printStackTrace();
        }
		return gl;
	}

	private GL useGLDebugPipeline(GL gl) {
        Log.message("using GL debug pipeline");
        try {
			return gl.getContext().setGL( GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null) );
        } catch (Exception e) {
        	e.printStackTrace();
        }
		return gl;
	}

	private void pickStep(GL2 gl2) {
        if(!pickNow) return;

		pickNow = false;

		CameraComponent cameraComponent = findFirstComponent(CameraComponent.class);
		if(cameraComponent==null) return;

		int pickName = findItemUnderCursor(gl2,cameraComponent);
		Entity next = scene.pickEntityWithName(pickName);
		UndoSystem.addEvent(this,new SelectEdit(this,getSelectedEntities(),next));
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
		CameraComponent camera = scene.findFirstComponent(CameraComponent.class);
		if(camera==null) return;

        viewport.renderChosenProjection(gl2,camera);

		clearAll(gl2);
		sky.render(gl2);

        scene.render(gl2);

        // overlays
		moveTool.render(gl2);
		viewCube.render(gl2);
	}

	private void clearAll(GL2 gl2) {
		// Clear the screen and depth buffer
		//gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
		gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
	}

	private void updateStep(double dt) {
    	frameDelay+=dt;
    	if(frameDelay>frameLength) {
   			frameDelay-=frameLength;
	    	InputManager.update(isMouseIn);
	    	update( frameLength );
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

	public void stop() {
		animator.stop();
	}

	/**
	 * Use glRenderMode(GL_SELECT) to ray pick the item under the cursor.
	 * See <a href="https://github.com/sgothel/jogl-demos/blob/master/src/demos/misc/Picking.java">1</a>
	 * and <a href="http://web.engr.oregonstate.edu/~mjb/cs553/Handouts/Picking/picking.pdf">2</a>
	 * @param gl2 the openGL render context
	 */
    public int findItemUnderCursor(GL2 gl2,CameraComponent cameraComponent) {
    	IntBuffer pickBuffer = Buffers.newDirectIntBuffer(PICK_BUFFER_SIZE);
        gl2.glSelectBuffer(PICK_BUFFER_SIZE, pickBuffer);

		gl2.glRenderMode( GL2.GL_SELECT );
		// wipe the select buffer
		gl2.glInitNames();

		viewport.renderPick(gl2,cameraComponent,pickPoint.x,pickPoint.y);
		
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
		if(verbose) logger.debug(hits+" PICKS @ "+pickPoint.x+","+pickPoint.y);
		
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

	public Viewport getViewport() {
		return viewport;
	}

	public CameraComponent getCamera() {
		return findFirstComponent(CameraComponent.class);
	}

	public void setSelectedEntity(Entity entity) {
		List<Entity> list = new ArrayList<>();
		if(entity!=null) list.add(entity);
		setSelectedEntities(list);
	}

	public void setSelectedEntities(List<Entity> list) {
		selectedEntities.clear();
		selectedEntities.addAll(list);
		updateSelectEntities();
		updateActionEnableStatus();
	}

	public void setCopiedEntities(Entity container) {
		copiedEntities=container;
		updateActionEnableStatus();
	}

	public Entity getCopiedEntities() {
		return copiedEntities;
	}

	/**
	 * All Actions have the tools to check for themselves if they are active.
	 */
	private void updateActionEnableStatus() {
		for(AbstractAction a : actions) {
			if(a instanceof EditorAction) {
				((EditorAction)a).updateEnableStatus();
			}
		}
	}
}
