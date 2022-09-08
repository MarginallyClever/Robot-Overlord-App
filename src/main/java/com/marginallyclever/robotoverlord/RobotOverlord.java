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
import com.marginallyclever.robotoverlord.swinginterface.ComponentPanel;
import com.marginallyclever.robotoverlord.swinginterface.InputManager;
import com.marginallyclever.robotoverlord.swinginterface.SoundSystem;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.actions.*;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanel;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanelEvent;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.util.PropertiesFileHelper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.awt.Component;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * {@code RobotOverlord} is the top-level controller of an application to educate robots.
 * It is built around good design patterns.
 * See https://github.com/MarginallyClever/Robot-Overlord-App
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

	// used for checking the application version with the github release, for "there is a new version available!" notification
	public static final String VERSION = PropertiesFileHelper.getVersionPropertyValue();
	// settings
	private final Preferences prefs = Preferences.userRoot().node("Evil Overlord");  // Secretly evil?  Nice.
    //private RecentFiles recentFiles = new RecentFiles();
    
    private Scene scene = new Scene();
	private transient final List<Entity> selectedEntities = new ArrayList<>();
	private transient final List<Entity> copiedEntities = new ArrayList<>();
	private final MoveTool moveTool = new MoveTool();
	private transient final ViewCube viewCube = new ViewCube();
	
	// The main frame of the GUI
	private JFrame mainFrame; 
	private static JFrame logFrame;
    private JMenuBar mainMenu;
	private final JSplitPane splitLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private final JSplitPane rightFrameSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private EntityTreePanel entityTree;
	private final ComponentPanel componentPanel = new ComponentPanel();
	
	private RenameEntityAction renameEntity;
	private RemoveEntityAction removeEntity;

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
        
        BasicDemo bd = new BasicDemo();
        bd.execute(this);
		updateEntityTree();

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
				// if they dragged the cursor around before unclicking, don't pick.
				if (e.getClickCount() == 2) {
					pickPoint.set(e.getX(),e.getY());
					pickNow=true;
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					pickPoint.set(e.getX(),e.getY());
					viewport.pressed();
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
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
		entityTree = new EntityTreePanel(true);
		entityTree.addEntityTreePanelListener((e)-> {
			if(e.eventType == EntityTreePanelEvent.UNSELECT) {
				selectedEntities.removeAll(e.subjects);
				updateSelectEntities();
			}
			if(e.eventType == EntityTreePanelEvent.SELECT) {
				selectedEntities.addAll(e.subjects);
				updateSelectEntities();
			}
		});

		entityTree.setPopupMenu(buildEntityTreePopupMenu());
		return entityTree;
	}

	private JPopupMenu buildEntityTreePopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		renameEntity=new RenameEntityAction(Translator.get("RenameEntityAction.name"),this);
		removeEntity=new RemoveEntityAction(Translator.get("RemoveEntityAction.name"),this);

		renameEntity.putValue(Action.SHORT_DESCRIPTION, Translator.get("RenameEntityAction.shortDescription"));
		removeEntity.putValue(Action.SHORT_DESCRIPTION, Translator.get("RemoveEntityAction.shortDescription"));

		removeEntity.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK));

		renameEntity.setEnabled(false);
		removeEntity.setEnabled(false);

		popupMenu.add(new AddChildEntityAction(this));
		popupMenu.add(renameEntity);
		popupMenu.add(removeEntity);

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
		return selectedEntities;
	}

	@Deprecated
	public void loadWorldFromFile(String absolutePath) {
		Scene nextScene = new Scene();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutePath)));
			StringBuilder responseStrBuilder = new StringBuilder();
			String inputStr;
			while ((inputStr = reader.readLine()) != null) {
				responseStrBuilder.append(inputStr);
			}
			nextScene.parseJSON(new JSONObject(responseStrBuilder.toString()));
		} catch(Exception e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(mainFrame,e.getLocalizedMessage());
			return;
		}

		removeAllChildren();
		scene = nextScene;
		addChild(sky);
		addChild(viewport);
		addChild(scene);
		addChild(moveTool);
		addChild(viewCube);
		setSelectedEntity(null);
		updateEntityTree();
	}

	public void newScene() {
		removeAllChildren();
		
		scene = new Scene();

		PoseComponent pose = new PoseComponent();
		CameraComponent camera = new CameraComponent();
		scene.addComponent(new PoseComponent());
		Entity mainCamera = new Entity("Main Camera");
		mainCamera.addComponent(pose);
		mainCamera.addComponent(camera);
		scene.addChild(mainCamera);
		pose.setPosition(new Vector3d(0,-10,-5));
		camera.lookAt(new Vector3d(0,0,0));

		Entity light0 = new Entity("Light");
		light0.addComponent(pose = new PoseComponent());
		light0.addComponent(new LightComponent());
		scene.addChild(light0);
		pose.setPosition(new Vector3d(-50,-50,50));

		addChild(sky);
 		addChild(viewport);
        addChild(scene);
 		addChild(moveTool);
 		addChild(viewCube);
		setSelectedEntity(null);
		updateEntityTree();
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

		SaveSceneAction saveSceneAction = new SaveSceneAction(Translator.get("SaveAsAction.name"),this);
		saveSceneAction.putValue(Action.SMALL_ICON,new UnicodeIcon("💾"));
		saveSceneAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("SaveAsAction.shortDescription"));
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

	public void updateEntityTree() {
    	entityTree.update(scene);
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
	
    public void updateSelectEntities() {
		if(renameEntity!=null) renameEntity.setEnabled(false);

    	if( selectedEntities != null && selectedEntities.size()>0) {
    		pickEntity(selectedEntities.get(0));
    		
	    	boolean removable = true;
	    	boolean moveable = true;
	    	
	    	for(Entity entity : selectedEntities) {
	    		if(entity == scene) removable=false;
	    		if(null==entity.getComponent(PoseComponent.class)) moveable=false;
	    		//if(e1 instanceof EntityFocusListener) ((EntityFocusListener)e1).lostFocus();
	    		if(entity instanceof EntityFocusListener) ((EntityFocusListener)entity).gainedFocus();
	    	}
			Entity firstEntity = selectedEntities.get(0);
			if(renameEntity!=null) renameEntity.setEnabled(selectedEntities.size()==1);
			if(removeEntity!=null) removeEntity.setEnabled(removable);
			
			moveTool.setSubject(null);
			if(moveable && selectedEntities.size()==1) {
				moveTool.setSubject(firstEntity);
			}
    	}
    	componentPanel.update(selectedEntities,this);
	}

    private void saveWindowSizeAndPosition() {
		// remember window location for next time.
    	Log.message("saveWindowSizeAndPosition()");
    	
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

			// Log.end() should be the very last call.  mainFrame.dispose() kills the thread, so this is as close as I can get.
			Log.end();
			
        	// Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
	        new Thread(new Runnable() {
	            @Override
				public void run() {
	            	animator.stop();
					mainFrame.dispose();
	            }
	        }).start();
        }
	}
	
	/**
	 * Deep search for a child with this name.
	 * @param name the name to match
	 * @return the entity.  null if nothing found.
	 */
	public Entity findChildWithName(String name) {
		ArrayList<Entity> list = new ArrayList<>();
		list.add(scene);
		while( !list.isEmpty() ) {
			Entity obj = list.remove(0);
			String objectName = obj.getName();
			if(name.equals(objectName)) return obj;
			list.addAll(obj.getChildren());
		}
		return null;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("RO", "Robot Overlord");
		view.popStack();
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
		UndoSystem.addEvent(this,new SelectEdit(this,selectedEntities,next));
    }
    
    private void checkRenderStep(GL2 gl2) {
		IntBuffer stackDepth = IntBuffer.allocate(1);
		gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,stackDepth);
		Log.message("stack depth start = "+stackDepth.get(0));

		renderStep(gl2);
		
		gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,stackDepth);
		Log.message("stack depth end = "+stackDepth.get(0));
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
		Log.message("setup the animation system");
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
     * https://github.com/sgothel/jogl-demos/blob/master/src/demos/misc/Picking.java
     * http://web.engr.oregonstate.edu/~mjb/cs553/Handouts/Picking/picking.pdf
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
		if(verbose) Log.message(hits+" PICKS @ "+pickPoint.x+","+pickPoint.y);
		
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
		Log.message(msg.toString());
	}

	public Viewport getViewport() {
		return viewport;
	}

	public CameraComponent getCamera() {
		return findFirstComponent(CameraComponent.class);
	}

	public void setSelectedEntity(Entity entity) {
		selectedEntities.clear();
		if(entity!=null) selectedEntities.add(entity);
		updateSelectEntities();
		updateEntityTree();
	}

	public void setSelectedEntities(List<Entity> list) {
		selectedEntities.clear();
		selectedEntities.addAll(list);
		updateSelectEntities();
		updateEntityTree();
	}

	public void setCopiedEntities(List<Entity> list) {
		copiedEntities.clear();
		copiedEntities.addAll(list);
	}
}
