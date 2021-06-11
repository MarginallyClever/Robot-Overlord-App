package com.marginallyclever.robotOverlord;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.vecmath.Vector2d;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLPipelineFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.demos.SixiDemo;
import com.marginallyclever.robotOverlord.demos.SkycamDemo;
import com.marginallyclever.robotOverlord.demos.StewartPlatformDemo;
import com.marginallyclever.robotOverlord.moveTool.MoveTool;
import com.marginallyclever.robotOverlord.swingInterface.FooterBar;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;
import com.marginallyclever.robotOverlord.swingInterface.SoundSystem;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionEntitySelect;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandAbout;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandAboutControls;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandAddEntity;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandCheckForUpdate;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandDemo;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandForums;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandNew;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandOpen;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandQuit;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandRedo;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandRemoveEntity;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandRenameEntity;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandSaveAs;
import com.marginallyclever.robotOverlord.swingInterface.commands.CommandUndo;
import com.marginallyclever.robotOverlord.swingInterface.entityTreePanel.EntityTreePanel;
import com.marginallyclever.robotOverlord.swingInterface.entityTreePanel.EntityTreePanelEvent;
import com.marginallyclever.robotOverlord.swingInterface.entityTreePanel.EntityTreePanelListener;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.util.PropertiesFileHelper;

/**
 * Robot Overlord (RO) is the top-level controller of an application to educate robots.
 * It is built around good design patterns.
 * See https://github.com/MarginallyClever/Robot-Overlord-App
 * 
 * @author Dan Royer
 *
 */
public class RobotOverlord extends Entity implements MouseListener, MouseMotionListener, GLEventListener, UndoableEditListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8890695769715268519L;
	//public static final String APP_TITLE = "Robot Overlord SKYCAM SPECIAL";  // for skycam only
	public static final String APP_TITLE = "Robot Overlord";
	public static final  String APP_URL = "https://github.com/MarginallyClever/Robot-Overlord";
	
	// used for checking the application version with the github release, for "there is a new version available!" notification
	final static public String VERSION = PropertiesFileHelper.getVersionPropertyValue();
	
	// Scene container
	protected Scene scene = new Scene();
	// The currently selected entities to edit.
	protected transient ArrayList<Entity> selectedEntities = new ArrayList<Entity>(); 
	
	// To move selected items in 3D
	protected MoveTool dragTool = new MoveTool();
	// The box in the top right of the user view that shows your orientation in the world.
	protected transient ViewCube viewCube = new ViewCube();
	// All the projection matrix stuff. 
	public Viewport viewport = new Viewport();
	public Camera camera = new Camera();
	
	// click on screen to change which entity is selected
	// select buffer depth
	static final public int PICK_BUFFER_SIZE=256;
	// when to pick
	protected transient boolean pickNow = false;
	// where on screen to pick
	protected transient Vector2d pickPoint = new Vector2d();
	
    // The animator keeps things moving
    private FPSAnimator animator;
    // animation speed
    public static final int DEFAULT_FRAMES_PER_SECOND = 30;
    
	private static final int FSAA_NUM_SAMPLES = 3;
	private static final int VERTICAL_SYNC_ON = 1;  // 1 on, 0 off
    
    // timing for animations
    protected long startTime;
    protected long lastTime;
    private double frameDelay;
    private double frameLength;
	
	// settings
    protected Preferences prefs = Preferences.userRoot().node("Evil Overlord");  // Secretly evil?  Nice.
    protected RecentFiles recentFiles = new RecentFiles();
	
	// should I check the state of the OpenGL stack size?  true=every frame, false=never
	protected boolean checkStackSize = false;

	// The main frame of the GUI
    protected JFrame mainFrame; 
	protected transient JMenuBar mainMenu;
	protected JSplitPane splitLeftRight;
	protected JSplitPane rightFrameSplitter;
	protected GLJPanel glCanvas;
	protected EntityTreePanel entityTree;
	protected JPanel selectedEntityPanel;
	protected FooterBar footerBar;
	
	// undo/redo system
	private UndoManager undoManager = new UndoManager();
	private CommandUndo commandUndo = new CommandUndo(undoManager);
	private CommandRedo commandRedo = new CommandRedo(undoManager);

	private CommandRenameEntity renameEntity;
	private CommandRemoveEntity removeEntity;
	
	// mouse steering controls
	private boolean isMouseIn=false;
	
	
 	private RobotOverlord() {
 		super();
 		setName("");
 		 		
		if(GraphicsEnvironment.isHeadless()) {
			throw new RuntimeException("RobotOverlord cannot be run headless yet.");
		}
		
		Translator.start();
		SoundSystem.start();
		InputManager.start();

        commandUndo.setRedoCommand(commandRedo);
    	commandRedo.setUndoCommand(commandUndo);
    	
 		addChild(viewport);
 		addChild(camera);
        addChild(scene);
 		addChild(dragTool);
 		addChild(viewCube);
 		
 		viewport.setAttachedTo(camera.getFullPath());

        buildMainFrame();
        buildMainMenu();
		buildOpenGLView();
        layoutComponents();
        setupAnimationSystem();
        
		Log.message("** READY **");
    }
 	
 	private void setupAnimationSystem() {
		Log.message("setup the animation system");
        frameDelay=0;
        frameLength=1.0f/(float)DEFAULT_FRAMES_PER_SECOND;
      	animator = new FPSAnimator(DEFAULT_FRAMES_PER_SECOND);
        animator.add(glCanvas);
        // record the start time of the application, also the end of the core initialization process.
        lastTime = startTime = System.currentTimeMillis();
        // start the main application loop.  it will call display() repeatedly.
        animator.start();
	}

	@SuppressWarnings("unused")
	private void buildOpenGLView() {
        Log.message("buildOpenGLView()");

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
	
        Log.message("...add listeners");
        glCanvas.addGLEventListener(this);  // this class also listens to the glcanvas (messy!) 
        glCanvas.addMouseListener(this);  // this class also listens to the mouse button clicks.
        glCanvas.addMouseMotionListener(this);  // this class also listens to the mouse movement.
        Log.message("...set minimum size");
        // not sure what good this does here...
        Dimension minimumSize = new Dimension(300,300);
        glCanvas.setMinimumSize(minimumSize);
 	}

	private JPanel buildEntityManagerPanel() {
        Log.message("build entity tree and panel");
		JPanel entityManagerPanel = new JPanel(new BorderLayout());
		JPanel abContainer = new JPanel(new FlowLayout());
		renameEntity=new CommandRenameEntity(this);
		removeEntity=new CommandRemoveEntity(this);
		renameEntity.setEnabled(false);
		removeEntity.setEnabled(false);
		
		abContainer.add(new JButton(new CommandAddEntity(this)));
		abContainer.add(new JButton(renameEntity));
		abContainer.add(new JButton(removeEntity));
		entityManagerPanel.add(abContainer,BorderLayout.NORTH);

        entityTree = new EntityTreePanel(this,true);
        entityTree.addEntityTreePanelListener(new EntityTreePanelListener() {
    		@Override
    		public void entityTreePanelEvent(EntityTreePanelEvent e) {
    			if(e.eventType== EntityTreePanelEvent.UNSELECT) {
    				selectedEntities.removeAll(e.subjects);
    			}
    			if(e.eventType== EntityTreePanelEvent.SELECT) {
    				selectedEntities.addAll(e.subjects);
    				updateSelectEntities(selectedEntities);
    			}
    		}
        });
        entityManagerPanel.add(entityTree,BorderLayout.CENTER);
        
		return entityManagerPanel;
	}
	
	private void layoutComponents() {
        Log.message("layoutComponents()");
		JPanel entityManagerPanel = buildEntityManagerPanel();

        selectedEntityPanel = new JPanel(new BorderLayout());
        
		// the right hand stuff			        
		rightFrameSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		rightFrameSplitter.add(entityManagerPanel);
		rightFrameSplitter.add(new JScrollPane(selectedEntityPanel));
		// make sure the master panel can't be squished.
        Dimension minimumSize = new Dimension(360,300);
        rightFrameSplitter.setMinimumSize(minimumSize);
        // if the window resizes, give top and bottom halves equal share of the real estate
		rightFrameSplitter.setResizeWeight(0.5);

        Log.message("build splitters");
        splitLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitLeftRight.add(glCanvas);
        splitLeftRight.add(rightFrameSplitter);
        // if the window resizes, give left half as much real estate as it can get.
        splitLeftRight.setResizeWeight(1);

        mainFrame.add(splitLeftRight);
        
        mainFrame.setJMenuBar(mainMenu);
        mainFrame.setVisible(true);
 	}

	private void buildMainFrame() {
		Log.message("buildMainFrame()");
		// start the main application frame - the largest visible rectangle on the screen with the minimize/maximize/close buttons.
        mainFrame = new JFrame( APP_TITLE + " " + VERSION ); 
    	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
    	int windowW = prefs.getInt("windowWidth", 1224);
    	int windowH = prefs.getInt("windowHeight", 768);
    	int windowX = prefs.getInt("windowX", -1);
    	int windowY = prefs.getInt("windowY", -1);

		Log.message("Position main window");
        mainFrame.setSize( windowW, windowH );
        if(windowX==-1 || windowY==-1) {
        	// centered
        	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        	windowX = (dim.width - windowW)/2;
        	windowY = (dim.height - windowH)/2;
        }
        mainFrame.setLocation( windowX, windowY );
        mainFrame.setLayout(new java.awt.BorderLayout());
        mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        // when someone tries to close the app, confirm it.
        mainFrame.addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent e) {
        		confirmClose();
        		super.windowClosing(e);
        	}
		});
        
        // when focus is lost, tell the input manager.
        mainFrame.addWindowFocusListener(new WindowAdapter() {
        	@Override
        	public void windowLostFocus(WindowEvent e) {
        		super.windowLostFocus(e);
        	}
			@Override
			public void windowGainedFocus(WindowEvent e) {
				super.windowLostFocus(e);
			}
        });
    	        
        mainFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            	//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            	//Dimension windowSize = e.getComponent().getSize();
            	//Log.message("Resized to " + windowSize);
            	//Log.message("Screen size " + screenSize);
            	saveWindowSizeAndPosition();
            }
            @Override
            public void componentMoved(ComponentEvent e) {
            	//Log.message("Moved to " + e.getComponent().getLocation());
            }
          });
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}
	
	public UndoManager getUndoManager() {
		return undoManager;
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public ArrayList<Entity> getSelectedEntities() {
		return selectedEntities;
	}
	
	/**
	 * Change the right-side context menu.  contextMenu is already a JScrollPane.
	 * Get all the {@link EntityPanel}s for a {@link Entity}.  
	 * @param panel
	 * @param title
	 */
	public void updateSelectedEntityPanel(ArrayList<Entity> entityList) {
		if(selectedEntityPanel==null) return;
		
		selectedEntityPanel.removeAll();
		if(entityList != null ) {
			if(entityList.size()==1) {
				int size = entityList.size();
				ViewPanel [] panels = new ViewPanel[size];
				for( int i=0;i<size;++i) {
					panels[i] = new ViewPanel(this);
					entityList.get(i).getView(panels[i]);
				}
				
				// keep the first panel.
				// TODO compare panels and keep only the matching elements and - if possible - the data in those elements.
				ViewPanel combined = panels[0];
				
				// TODO throw away panels that have no elements left.
	
				selectedEntityPanel.add(combined.getFinalView(),BorderLayout.PAGE_START);
			} else {
				// TODO display values shared across all selected entities
			}
		}
		selectedEntityPanel.repaint();
		selectedEntityPanel.revalidate();

		JScrollPane scroller = (JScrollPane)rightFrameSplitter.getBottomComponent(); 
		scroller.getVerticalScrollBar().setValue(0);
	}

	public void saveWorldToFile(String filename) {
		saveEntityToFileJSON(filename,scene);
		//saveWorldToFileSerializable(filename);
	}
	
	public void loadWorldFromFile(String filename) {
		scene = (Scene)loadEntityFromFileJSON(filename);
		//loadWorldFromFileSerializable(filename);
	}

	protected ObjectMapper getObjectMapper() {
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);/*
		om.setVisibility(
					om.getSerializationConfig().
					getDefaultVisibilityChecker().
					withFieldVisibility(Visibility.ANY).
					withGetterVisibility(Visibility.NONE));*/
		return om;
	}
	
	public void saveEntityToFileJSON(String filename,Entity ent) {
		ObjectMapper om = getObjectMapper();
		try {
			om.writeValue(new File(filename), ent);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Entity loadEntityFromFileJSON(String filename) {
		ObjectMapper om = getObjectMapper();
		Entity ent = null;
		try {
			ent = (Scene)om.readValue(new File(filename), Scene.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ent;
	}
	
	/**
	 * See http://www.javacoffeebreak.com/text-adventure/tutorial3/tutorial3.html
	 * @param filename
	 */
	@Deprecated
	public void saveWorldToFileSerializable(String filename) {
		FileOutputStream fout=null;
		ObjectOutputStream objectOut=null;
		try {
			fout = new FileOutputStream(filename);
			objectOut = new ObjectOutputStream(fout);
			objectOut.writeObject(scene);
		} catch(java.io.NotSerializableException e) {
			Log.message("Something can't be serialized.");
			e.printStackTrace();
		} catch(IOException e) {
			Log.message("Save failed.");
			e.printStackTrace();
		} finally {
			if(objectOut!=null) {
				try {
					objectOut.close();
				} catch(IOException e) {}
			}
			if(fout!=null) {
				try {
					fout.close();
				} catch(IOException e) {}
			}
		}
	}

	/**
	 *  See http://www.javacoffeebreak.com/text-adventure/tutorial3/tutorial3.html
	 * @param filename
	 */
	@Deprecated
	public void loadWorldFromFileSerializable(String filename) {
		FileInputStream fin=null;
		ObjectInputStream objectIn=null;
		try {
			// Create a file input stream
			fin = new FileInputStream(filename);
	
			// Create an object input stream
			objectIn = new ObjectInputStream(fin);
	
			// Read an object in from object store, and cast it to a GameWorld
			this.scene = (Scene) objectIn.readObject();
		} catch(IOException e) {
			Log.message("World load failed (file io).");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			Log.message("World load failed (class not found)");
			e.printStackTrace();
		} finally {
			if(objectIn!=null) {
				try {
					objectIn.close();
				} catch(IOException e) {}
			}
			if(fin!=null) {
				try {
					fin.close();
				} catch(IOException e) {}
			}
		}
	}

	public void newScene() {
		scene.removeAllChildren();
		updateEntityTree();
		pickEntity(null);
	}
	
	/*
	
	// stuff for trying to find and load plugins, part of future expansion
	 
	private String getPath(Class cls) {
	    String cn = cls.getName();
	    //Log.message("cn "+cn);
	    String rn = cn.replace('.', '/') + ".class";
	    //Log.message("rn "+rn);
	    String path = getClass().getClassLoader().getResource(rn).getPath();
	    //Log.message("path "+path);
	    int ix = path.indexOf("!");
	    if(ix >= 0) {
	        path = path.substring(0, ix);
	    }
	    return path;
	}
	
	protected void EnumerateJarContents(String absPathToJarFile) throws IOException {
	    JarFile jarFile = new JarFile(absPathToJarFile);
	    Enumeration<JarEntry> e = jarFile.entries();
	    while (e.hasMoreElements()) {
			_EnumerateJarContents(e.nextElement());
		}
	}
	
	private static void _EnumerateJarContents(Object obj) {
       JarEntry entry = (JarEntry)obj;
       String name = entry.getName();
       long size = entry.getSize();
       long compressedSize = entry.getCompressedSize();
       Log.message(name + "\t" + size + "\t" + compressedSize);
     }
	
	// Load a class from a Jar file.
	// @param absPathToJarFile c:\some\path\myfile.jar
	// @param className like com.mypackage.myclass
	protected void LoadClasses(String absPathToJarFile,String className) {
		File file  = new File(absPathToJarFile);
		try {
			URL url = file.toURI().toURL();  
			URL[] urls = new URL[]{url};
			ClassLoader cl = new URLClassLoader(urls);
			Class cls = cl.loadClass(className);
		}
		catch(MalformedURLException e) {}
		catch(ClassNotFoundException e) {}
	}
	*/
	
	public void buildMainMenu() {
		Log.message("buildMainMenu()");
		
		mainMenu = new JMenuBar();
		mainMenu.removeAll();
		
		JMenu menu;
		
		menu = new JMenu(APP_TITLE);
		menu.add(new JMenuItem(new CommandNew(this)));        	
		menu.add(new JMenuItem(new CommandOpen(this)));
		menu.add(new JMenuItem(new CommandSaveAs(this)));
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new CommandQuit(this)));
		mainMenu.add(menu);
		
		menu = new JMenu("Demos");
		menu.add(new JMenuItem(new CommandDemo(this,new SixiDemo())));
		menu.add(new JMenuItem(new CommandDemo(this,new SkycamDemo())));
		menu.add(new JMenuItem(new CommandDemo(this,new StewartPlatformDemo())));
        mainMenu.add(menu);
		
        menu = new JMenu("Edit");
        menu.add(new JMenuItem(commandUndo));
        menu.add(new JMenuItem(commandRedo));
        mainMenu.add(menu);
    	
        menu = new JMenu("Help");
        menu.add(new JMenuItem(new CommandAboutControls()));
		menu.add(new JMenuItem(new CommandForums()));
		menu.add(new JMenuItem(new CommandCheckForUpdate()));
		menu.add(new JMenuItem(new CommandAbout()));
        mainMenu.add(menu);
    	
    	// done
        mainMenu.updateUI();
	}
	
    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
        // set up the projection matrix
        viewport.setCanvasWidth(glCanvas.getSurfaceWidth());
        viewport.setCanvasHeight(glCanvas.getSurfaceHeight());
    }
    
    @Override
    public void init( GLAutoDrawable drawable ) {
        Log.message("gl init");

        GL gl = drawable.getGL();

    	final boolean glDebug=false;
    	if(glDebug) useGLDebugPipeline(gl);
    	final boolean glTrace=false;
        if(glTrace) useTracePipeline(gl); 
        
        Log.message("...get gl2");
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
        
        int buf[] = new int[1];
        int sbuf[] = new int[1];
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

    private void useTracePipeline(GL gl) {
        try {
            gl = gl.getContext().setGL( GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[] { System.err } ) );
        } catch (Exception e) {
        	e.printStackTrace();
        }		
	}

	private void useGLDebugPipeline(GL gl) {
        Log.message("using GL debug pipeline");
        try {
            gl = gl.getContext().setGL( GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null) );
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	@Override
    public void dispose( GLAutoDrawable drawable ) {}
    
    /**
     * Draw the 3D scene.  Also does other update tasks and polls input.
     */
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

	private void pickStep(GL2 gl2) {
        //viewport.showPickingTest(gl2);
		//pickNow=true;
        if(pickNow) {
	        pickNow=false;
	        int pickName = findItemUnderCursor(gl2);
        	Entity next = scene.pickPhysicalEntityWithName(pickName);
        	if(next!=null) {
        		undoableEditHappened(new UndoableEditEvent(this,new ActionEntitySelect(this,selectedEntities,next) ) );
        	}
        }
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
        viewport.renderChosenProjection(gl2);
        scene.render(gl2);
        // overlays
		dragTool.render(gl2);
		viewCube.render(gl2);
	}

	private void updateStep(double dt) {
    	frameDelay+=dt;
    	if(frameDelay>frameLength) {
   			frameDelay-=frameLength;
	    	InputManager.update(isMouseIn);
	    	this.update( frameLength );
    	}
	}

	/**
     * Use glRenderMode(GL_SELECT) to ray pick the item under the cursor.
     * https://github.com/sgothel/jogl-demos/blob/master/src/demos/misc/Picking.java
     * http://web.engr.oregonstate.edu/~mjb/cs553/Handouts/Picking/picking.pdf
     * @param gl2 the openGL render context
     */
    protected int findItemUnderCursor(GL2 gl2) {
    	IntBuffer pickBuffer = Buffers.newDirectIntBuffer(PICK_BUFFER_SIZE);
        gl2.glSelectBuffer(PICK_BUFFER_SIZE, pickBuffer);

		gl2.glRenderMode( GL2.GL_SELECT );
		// wipe the select buffer
		gl2.glInitNames();

		viewport.renderPick(gl2,pickPoint.x,pickPoint.y);
		
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
		
		String msg="  names="+nameCount+" zMin="+z1+" zMax="+z2+": ";
		String add="";
		int pickName;
		for(int j=0;j<nameCount;++j) {
			pickName = pickBuffer.get(index++);
			msg+=add+pickName;
    		add=", ";
		}
		Log.message(msg);
	}

	public void updateEntityTree() {
    	entityTree.update();
    }
    
    /**
     * An entity in the 3D scene has been "picked" (aka double-clicked).  This begins the process
     * to select that entity.  Entities selected through other means should not call pickEntity() as it would
     * cause an infinite loop.
     * 
     * @param e
     */
    protected void pickEntity(Entity e) {
		//Log.message( "Picked "+((e==null)?"nothing":e.getFullPath()) );
		if(selectedEntities.contains(e)) return;

		entityTree.setSelection(e);
	}
	
	/**
	 * The selected entities have changed, either through scene picking or tree clicking
	 * @param e
	 */
    public void updateSelectEntities(ArrayList<Entity> entityList) {
    	if( entityList != null && entityList.size()>0) {
	    	boolean removable = true;
	    	boolean moveable = true;
	    	
	    	for(Entity e1 : entityList) {
	    		if(!(e1 instanceof Removable)) removable=false;
	    		if(!(e1 instanceof Moveable)) moveable=false;
	    		//if(e1 instanceof EntityFocusListener) ((EntityFocusListener)e1).lostFocus();
	    		if(e1 instanceof EntityFocusListener) ((EntityFocusListener)e1).gainedFocus();
	    	}
			if(renameEntity!=null) renameEntity.setEnabled(entityList.size()==1 && entityList.get(0).canBeRenamed());
			if(removeEntity!=null) removeEntity.setEnabled(removable);
					
			if(moveable) {
				if(entityList.size()==1) {
					Entity e = entityList.get(0);
					if(e instanceof Moveable) {
						dragTool.setSubject((Moveable)e);
					} else if(e==null) {
						dragTool.setSubject(null);
					}
				} else {
					// TODO group all selected poseEntities so they can be moved as one?
					dragTool.setSubject(null);
				}
			} else {
				dragTool.setSubject(null);
			}
    	}	
		updateSelectedEntityPanel(entityList);
	}
	
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
	@Override
	public void mouseDragged(MouseEvent e) {
        viewport.setCursor(e.getX(),e.getY());
	}
	@Override
	public void mouseMoved(MouseEvent e) {
        viewport.setCursor(e.getX(),e.getY());
	}

	
	protected void saveWindowSizeAndPosition() {
		// remember window location for next time.
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

			// Log.end() should be the very last call.  mainFrame.dispose() kills the thread, so this is as close as I can get.
			Log.end();
			
        	// Run this on another thread than the AWT event queue to
	        // make sure the call to Animator.stop() completes before
	        // exiting
	        new Thread(new Runnable() {
	            public void run() {
					animator.stop();
					mainFrame.dispose();
	            }
	        }).start();
        }
	}

	public static void main(String[] argv) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	new RobotOverlord();
	        }
	    });
	}
		
	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		undoManager.addEdit(e.getEdit());
		commandUndo.updateUndoState();
		commandRedo.updateRedoState();
	}
	
	/**
	 * Deep search for a child with this name.
	 * @param name
	 * @return the entity.  null if nothing found.
	 */
	public Entity findChildWithName(String name) {
		ArrayList<Entity> list = new ArrayList<Entity>();
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
}
