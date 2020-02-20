package com.marginallyclever.robotOverlord;


import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.IntBuffer;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLPipelineFactory;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.communications.NetworkConnectionManager;
import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.engine.undoRedo.UndoHelper;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandAbout;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandAboutControls;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandCheckForUpdate;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandForums;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandLoad;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandNew;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandQuit;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandRedo;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandSaveAs;
import com.marginallyclever.robotOverlord.engine.undoRedo.commands.UserCommandUndo;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.camera.Camera;
import com.marginallyclever.robotOverlord.entity.world.World;
import com.marginallyclever.robotOverlord.uiElements.FooterBar;
import com.marginallyclever.robotOverlord.uiElements.InputManager;
import com.marginallyclever.robotOverlord.uiElements.SoundSystem;
import com.marginallyclever.robotOverlord.uiElements.Splitter;
import com.marginallyclever.util.PropertiesFileHelper;

/**
 * MainGUI is the root window object.
 * @author danroyer
 *
 */
public class RobotOverlord 
implements MouseListener, MouseMotionListener, KeyListener, GLEventListener, WindowListener
{
	public static final String APP_TITLE = "Robot Overlord";
	public static final  String APP_URL = "https://github.com/MarginallyClever/Robot-Overlord";
	
	// used for checking the application version with the github release, for "there is a new version available!" notification
	final static public String VERSION = PropertiesFileHelper.getVersionPropertyValue();
	// select picking
	static final public int SELECT_BUFFER_SIZE=256;
	static final public int DEFAULT_FRAMES_PER_SECOND = 30;
	
	protected transient NetworkConnectionManager connectionManager;
	protected World world;

	// select picking
	protected transient IntBuffer selectBuffer = null;
	protected transient boolean pickNow;
	protected transient double pickX, pickY;
	protected transient Entity pickedEntity; 
	protected transient int pickedHandle;

	// menus
    // main menu bar
	protected transient JMenuBar mainMenu;
	// edit menu
	protected transient JMenuItem buttonUndo,buttonRedo;
	
    // The animator keeps things moving
    private FPSAnimator animator;
    
    // timing for animations
    protected long startTime;
    protected long lastTime;
    private double frameDelay;
    private double frameLength;
	
	// settings
    protected Preferences prefs;
	protected String[] recentFiles = {"","","","","","","","","",""};

	protected boolean checkStackSize;

	// The main frame of the GUI
    protected JFrame mainFrame; 
	// the main view
	protected Splitter splitLeftRight;
	protected Splitter splitUpDown;
	
	protected GLJPanel glCanvas;
	protected JScrollPane contextMenu;
	//protected SecondaryPanel secondaryPanel;
	protected FooterBar footerBar;
	
	// undo/redo system
	private UndoManager commandSequence;
	private UndoHelper undoHelper;

	// mouse steering controls
	private boolean isMouseIn;
	//private int cursorStartX, cursorStartY;
	//private int cursorPreviousX, cursorPreviousY;
	
	// opengl rendering context
	private GLU glu;
	
	
 	protected RobotOverlord() {
		prefs = Preferences.userRoot().node("Evil Overlord");

		//System.out.println("\n\n*** CLASSPATH="+System.getProperty("java.class.path")+" ***\n\n");
		
		Translator.start();
		SoundSystem.start();
		InputManager.start();
		
		commandSequence = new UndoManager();
		undoHelper = new UndoHelper(this);
		checkStackSize=false;
		
		isMouseIn=false;
		
		connectionManager = new NetworkConnectionManager();
/*
		try {
			String s = getPath(this.getClass());
			System.out.println("enumerating "+s);
			EnumerateJarContents(s);
		}
		catch(IOException e) {
			System.out.println("failed to enumerate");
		}
*/
		// start the main application frame - the largest visible rectangle on the screen with the minimize/maximize/close buttons.
        mainFrame = new JFrame( APP_TITLE ); 
    	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setSize( 1224, 768 );
        mainFrame.setLayout(new java.awt.BorderLayout());
    	
        // add to the frame a menu bar
        mainMenu = new JMenuBar();
        mainFrame.setJMenuBar(mainMenu);

        // setup the animation system.
        frameDelay=0;
        frameLength=1.0f/(float)DEFAULT_FRAMES_PER_SECOND;
      	animator = new FPSAnimator(DEFAULT_FRAMES_PER_SECOND*2);

      	// this class listens to the window
        mainFrame.addWindowListener(this);

        // some opengl stuff
        GLCapabilities caps = new GLCapabilities(null);
        caps.setSampleBuffers(true);
        caps.setHardwareAccelerated(true);
        caps.setNumSamples(4);
        glCanvas = new GLJPanel(caps);
        animator.add(glCanvas);
        glCanvas.addGLEventListener(this);  // this class also listens to the glcanvas (messy!) 
        glCanvas.addMouseListener(this);  // this class also listens to the mouse button clicks.
        glCanvas.addMouseMotionListener(this);  // this class also listens to the mouse movement.
        
        // the right hand context-sensitive menu
        contextMenu = new JScrollPane();
        contextMenu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contextMenu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        Dimension minimumSize = new Dimension(300,300);
        contextMenu.setMinimumSize(minimumSize);
        glCanvas.setMinimumSize(minimumSize);  // not sure what good this does here...

        world = new World();
        world.createDefaultWorld();

        // now that we have everything built, set up the menus.
        buildMainMenu();
        
        // initialize the screen picking system (to click on a robot and get its context sensitive menu)
        pickNow = false;
        selectBuffer = Buffers.newDirectIntBuffer(RobotOverlord.SELECT_BUFFER_SIZE);
        pickedEntity = null;
        pickNothing();
        
        // Keys typed while the focus is on the glcanvas will be heard by this class.  That way we can fly WASD.
		glCanvas.addKeyListener(this);
		
//		frame.setFocusable(true);
//		frame.requestFocusInWindow();
/*
		// focus not returning after modal dialog boxes
		// http://stackoverflow.com/questions/5150964/java-keylistener-does-not-listen-after-regaining-focus
		frame.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent e){
                //System.out.println("Focus GAINED:"+e);
            }
            public void focusLost(FocusEvent e){
                //System.out.println("Focus LOST:"+e);

                // FIX FOR GNOME/XWIN FOCUS BUG
                e.getComponent().requestFocus();
            }
        });
*/
        // split the mainframe in two vertically
        splitLeftRight = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
        // on the left put the 3D view
        splitLeftRight.add(glCanvas);
        // on the right put the context sensitive menu container
        splitLeftRight.add(contextMenu);
        
        // Also split up/down
        splitUpDown = new Splitter(JSplitPane.VERTICAL_SPLIT);
        splitUpDown.add(splitLeftRight);
        //splitUpDown.add(secondaryPanel = new SecondaryPanel());
        splitUpDown.add(footerBar = new FooterBar(mainFrame));
        
		// add the split panel to the main frame
        mainFrame.add(splitUpDown);
        //mainFrame.add(splitLeftRight);
        // make it visible
        mainFrame.setVisible(true);
        // start the main application loop.  it will call display() repeatedly.
        animator.start();
        // record the start time of the application, also the end of the core initialization process.
        lastTime = startTime = System.currentTimeMillis();
    }
	

 	public NetworkConnectionManager getConnectionManager() {
 		return connectionManager;
 	}
 	
	public JFrame getMainFrame() {
		return mainFrame;
	}
	
	
	public UndoManager getUndoManager() {
		return commandSequence;
	}
	
	
	public UndoHelper getUndoHelper() {
		return undoHelper;
	}
	

	public World getWorld() {
		return world;
	}
	
	public Entity getPickedEntity() {
		return pickedEntity;
	}
	
	/**
	 * Change the right-side context menu.  contextMenu is already a JScrollPane
	 * @param panel
	 * @param title
	 */
	public void setContextPanel(JComponent panel,String title) {
		contextMenu.setViewportView(panel);
	}

	public void saveWorldToFile(String filename) {
		saveWorldToFileJSON(filename);
		//saveWorldToFileSerializable(filename);
	}
	
	public void loadWorldFromFile(String filename) {
		loadWorldFromFileJSON(filename);
		//loadWorldFromFileSerializable(filename);
	}
	
	
	public void saveWorldToFileJSON(String filename) {
		ObjectOutputStream objectOut=null;
		try {			
			JSONObject jWorld = world.toJSON();
			FileWriter fw = new FileWriter(filename);
			fw.write(jWorld.toString());
			fw.flush();
			fw.close();
		} catch(IOException e) {
			System.out.println("World save failed.");
			e.printStackTrace();
		} finally {
			if(objectOut!=null) {
				try {
					objectOut.close();
				} catch(IOException e) {}
			}
		}
	}
	
	public void loadWorldFromFileJSON(String filename) {
		FileReader fr;
		try {
			fr = new FileReader(filename);
			JSONParser parser = new JSONParser();
			JSONObject jWorld = (JSONObject)parser.parse(fr);
			world.fromJSON(jWorld);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			objectOut.writeObject(world);
		} catch(java.io.NotSerializableException e) {
			System.out.println("World can't be serialized.");
			e.printStackTrace();
		} catch(IOException e) {
			System.out.println("World save failed.");
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
			this.world = (World) objectIn.readObject();
			updateMenu();
		} catch(IOException e) {
			System.out.println("World load failed (file io).");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			System.out.println("World load failed (class not found)");
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

	public void newWorld() {
		this.world = new World();
		pickNothing();
		updateMenu();
	}
	
	/*
	
	// stuff for trying to find and load plugins, part of future expansion
	 
	private String getPath(Class cls) {
	    String cn = cls.getName();
	    //System.out.println("cn "+cn);
	    String rn = cn.replace('.', '/') + ".class";
	    //System.out.println("rn "+rn);
	    String path = getClass().getClassLoader().getResource(rn).getPath();
	    //System.out.println("path "+path);
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
       System.out.println(name + "\t" + size + "\t" + compressedSize);
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
		mainMenu.removeAll();
		
		JMenu menu;
		
		menu = new JMenu(APP_TITLE);
		menu.add(new UserCommandNew(this));        	
		menu.add(new UserCommandLoad(this));
		menu.add(new UserCommandSaveAs(this));
		menu.add(new JSeparator());
		menu.add(new UserCommandQuit(this));
		mainMenu.add(menu);
        
        menu = new JMenu("Edit");
        menu.add(buttonUndo = new UserCommandUndo(this));
        menu.add(buttonRedo = new UserCommandRedo(this));
        mainMenu.add(menu);

        menu = new JMenu("Help");
        menu.add(new UserCommandAboutControls(this));
		menu.add(new UserCommandForums(this));
		menu.add(new UserCommandCheckForUpdate(this));
		menu.add(new UserCommandAbout(this));
        mainMenu.add(menu);
    	
    	// done
        mainMenu.updateUI();
        updateMenu();
	}


	public void updateMenu() {
        if(buttonUndo==null || buttonRedo==null) return;
        
        buttonUndo.setText(commandSequence.getUndoPresentationName());
		buttonRedo.setText(commandSequence.getRedoPresentationName());
		buttonUndo.getParent().validate();
		buttonUndo.setEnabled(commandSequence.canUndo());
	    buttonRedo.setEnabled(commandSequence.canRedo());
    }

	
	public void undo() {
		try {
			commandSequence.undo();
		} catch (CannotUndoException ex) {
			ex.printStackTrace();
		} finally {
			updateMenu();
		}
	}
	
	
	public void redo() {
		try {
			commandSequence.redo();
		} catch (CannotRedoException ex) {
			ex.printStackTrace();
		} finally {
			updateMenu();
		}
	}
	
	
	/**
	 * changes the order of the recent files list in the File submenu, saves the updated prefs, and refreshes the menus.
	 * @param filename the file to push to the top of the list.
	 */
	public void updateRecentFiles(String filename) {
		int cnt = recentFiles.length;
		String [] newFiles = new String[cnt];
		
		newFiles[0]=filename;
		
		int i,j=1;
		for(i=0;i<cnt;++i) {
			if(!filename.equals(recentFiles[i]) && recentFiles[i] != "") {
				newFiles[j++] = recentFiles[i];
				if(j == cnt ) break;
			}
		}

		recentFiles=newFiles;

		// update prefs
		for(i=0;i<cnt;++i) {
			if( recentFiles[i]==null ) recentFiles[i] = "";
			if( !recentFiles[i].isEmpty() ) {
				prefs.put("recent-files-"+i, recentFiles[i]);
			}
		}
		
		updateMenu();
	}
	
	// A file failed to load.  Remove it from recent files, refresh the menu bar.
	public void removeRecentFile(String filename) {
		int i;
		for(i=0;i<recentFiles.length-1;++i) {
			if(recentFiles[i]==filename) {
				break;
			}
		}
		for(;i<recentFiles.length-1;++i) {
			recentFiles[i]=recentFiles[i+1];
		}
		recentFiles[recentFiles.length-1]="";

		// update prefs
		for(i=0;i<recentFiles.length;++i) {
			if(!recentFiles[i].isEmpty()) {
				prefs.put("recent-files-"+i, recentFiles[i]);
			}
		}
		
		updateMenu();
	}

	/**
	 * Open a gcode file to run on a robot.  This doesn't make sense if there's more than one robot!
	 * @param filename the file to open
	 */
	public void openFile(String filename) {
		
	}

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) {
    	GL2 gl2 = drawable.getGL().getGL2();
    	// turn on vsync
        gl2.setSwapInterval(1);

        // set up the projection matrix
    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		setPerspectiveMatrix();

		// set opengl options
		gl2.glDepthFunc(GL2.GL_LESS);
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthMask(true);

    	gl2.glEnable(GL2.GL_LINE_SMOOTH);      
        gl2.glEnable(GL2.GL_POLYGON_SMOOTH);
        gl2.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
        
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // TODO add a settings toggle for this option, it really slows down older machines.
        gl2.glEnable(GL2.GL_MULTISAMPLE);
        
        int buf[] = new int[1];
        int sbuf[] = new int[1];
        gl2.glGetIntegerv(GL2.GL_SAMPLES, buf, 0);
        gl2.glGetIntegerv(GL2.GL_SAMPLE_BUFFERS, sbuf, 0);
    }
    
    
    @Override
    public void init( GLAutoDrawable drawable ) {
    	// Use debug pipeline
    	boolean glDebug=false;
    	boolean glTrace=false;
    	
        GL gl = drawable.getGL();
        
        if(glDebug) {
            try {
                // Debug ..
                gl = gl.getContext().setGL( GLPipelineFactory.create("com.jogamp.opengl.Debug", null, gl, null) );
            } catch (Exception e) {e.printStackTrace();}
        }

        if(glTrace) {
            try {
                // Trace ..
                gl = gl.getContext().setGL( GLPipelineFactory.create("com.jogamp.opengl.Trace", null, gl, new Object[] { System.err } ) );
            } catch (Exception e) {e.printStackTrace();}
        }
        glu = GLU.createGLU(gl);
    }
    
    
    @Override
    public void dispose( GLAutoDrawable drawable ) {}
    
    
    /**
     * Draw the 3D scene.  Called ~30/s. Also does other update tasks and polls input.
     */
    @Override
    public void display( GLAutoDrawable drawable ) {
        long nowTime = System.currentTimeMillis();
        double dt = (nowTime - lastTime)*0.001;  // to seconds
    	lastTime = nowTime;
    	//System.out.println(dt);
    	
    	// UPDATE STEP
    	
    	frameDelay+=dt;
    	if(frameDelay>frameLength) {
   			frameDelay-=frameLength;
   			
	    	InputManager.update(isMouseIn);

   	        // if playing, read the input from the recording
	    	// if recording, write the input changes to the recording
	    	//RecordingManager.step();
	    	//RecordingManager.manageArrayOfDoubles(InputManager.keyState);
    	
   			world.update( frameLength );
   			
   			if( pickedEntity != null ) {
   				String statusMessage = pickedEntity.getStatusMessage();
   				footerBar.setStatusLabelText(statusMessage);
   			}
    	}

    	// RENDER STEP

    	GL2 gl2 = drawable.getGL().getGL2();

		if(checkStackSize) {
    		IntBuffer stackDepth = IntBuffer.allocate(1);
    		gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,stackDepth);
    		System.out.print("stack depth start = "+stackDepth.get(0));
		}	
		
		world.render(gl2);
		
        int pickName = 0;
        if(pickNow) {
        	pickName = findItemUnderCursor(gl2);
        	System.out.println(System.currentTimeMillis()+" pickName="+pickName);
    		
	        pickNow=false;
        	//System.out.println("pickedHandle="+pickedHandle);
        	if(pickedHandle==0 && pickName>0 && pickName<10) {
        		//System.out.println("new pickedHandle="+pickName);
        		pickedHandle=pickName;
        	} else {
        		pickIntoWorld(pickName);
        	}
        }
		
		if(checkStackSize) {
    		IntBuffer stackDepth = IntBuffer.allocate(1);
			gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,stackDepth);
			System.out.println("stack depth end = "+stackDepth.get(0));
		}
    }

    
    protected void setPerspectiveMatrix() {
        glu.gluPerspective(60, (float)glCanvas.getSurfaceWidth()/(float)glCanvas.getSurfaceHeight(), 5.0f, 2000.0f);
        Camera cam =world.getCamera();
        cam.setCanvasWidth(glCanvas.getSurfaceWidth());
        cam.setCanvasHeight(glCanvas.getSurfaceHeight());
        cam.setMinZ(5.0);
        cam.setMaxZ(2000.0);
        cam.setFOV(60.0);
    }
    
    /**
     * Use glRenderMode(GL_SELECT) to ray pick the item under the cursor.
     * https://github.com/sgothel/jogl-demos/blob/master/src/demos/misc/Picking.java
     * http://web.engr.oregonstate.edu/~mjb/cs553/Handouts/Picking/picking.pdf
     * @param gl2 the openGL render context
     */
    protected int findItemUnderCursor(GL2 gl2) {
    	// set up the buffer that will hold the names found under the cursor in furthest to closest.
        gl2.glSelectBuffer(SELECT_BUFFER_SIZE, selectBuffer);
        // change the render mode
		gl2.glRenderMode( GL2.GL_SELECT );
		// wipe the select buffer
		gl2.glInitNames();
        // get the current viewport dimensions to set up the projection matrix
        int[] viewport = new int[4];
		gl2.glGetIntegerv(GL2.GL_VIEWPORT,viewport,0);
		// Set up a tiny viewport that only covers the area behind the cursor.  Tiny viewports are faster?
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glPushMatrix();
        gl2.glLoadIdentity();
		glu.gluPickMatrix(pickX, viewport[3]-pickY, 5.0, 5.0, viewport,0);
		setPerspectiveMatrix();

        // render in selection mode, without advancing time in the simulation.
        world.render(gl2);

        // return the projection matrix to its old state.
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glPopMatrix();
        gl2.glFlush();
        //gl2.glPushName(0);

        // get the picking results and return the render mode to the default 
        int hits = gl2.glRenderMode( GL2.GL_RENDER );

        //gl2.glPopName();

		//System.out.println("\n"+hits+" PICKS @ "+pickX+","+pickY);
        float z1;
		//float z2;
		
        float zMinBest = Float.MAX_VALUE;
    	int i, j, index=0, nameCount, pickName, bestPickName=0;
    	
    	for(i=0;i<hits;++i) {
    		nameCount=selectBuffer.get(index++);
    		z1 = (float) (selectBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;
    		
    		//z2 = (float) (selectBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;
    		index++;
    		
    		//System.out.print("  names="+nameCount+" zMin="+z1);//+" zMax="+z2);
    		//String add=": ";
			for(j=0;j<nameCount-1;++j) {
    			pickName = selectBuffer.get(index++);
        		//System.out.print(add+pickName);
        		//add=", ";
			}
			if(nameCount>0) {
				pickName = selectBuffer.get(index++);
        		//System.out.print(add+pickName);
        		if(zMinBest > z1) {
        			zMinBest = z1;
        			bestPickName = pickName;
        		}
    		}
    		//System.out.println();
    	}
    	return bestPickName;
    }
    
    public void pickIntoWorld(int pickName) {
    	Entity newlyPickedEntity = world.pickObjectWithName(pickName);
    	
    	if(newlyPickedEntity==null || newlyPickedEntity == pickedEntity) {
			//System.out.println(" NO PICK");
    		pickNothing();
        } else {
			//System.out.print(" PICKED");
			pickEntity(newlyPickedEntity);
		}
    }
	
	public void pickEntity(Entity arg0) {
		unPick();
		arg0.pick();
		pickedEntity=arg0;
		pickedHandle=0;
		
		setContextPanel(arg0.getAllContextPanels(this),arg0.getDisplayName());
	}
    
    public void pickNothing() {
		unPick();
    	pickedEntity=null;
    	setContextPanel(world.getAllContextPanels(this),Translator.get("Everything"));
    }
	
    public void unPick() {
		if(pickedEntity!=null) {
			pickedEntity.unPick();
			pickedEntity=null;
			pickedHandle=0;
		}
    }
    
	public void pickCamera() {
		Camera camera = world.getCamera();
		if(camera!=null) {
			pickEntity(camera);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// if they dragged the cursor around before unclicking, don't pick.
		if (e.getClickCount() == 2) {
			pickX=e.getX();
			pickY=e.getY();
			pickNow=true;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//cursorPreviousX=cursorStartX=this.mainFrame.getWidth()/2;
		//cursorPreviousY=cursorStartY=this.mainFrame.getHeight()/2;

		hideCursor();
		pickX=e.getX();
		pickY=e.getY();
		pickedHandle=0;
		
		world.getCamera().pressed();
	}
	
	private void hideCursor() {/*
		// Hide cursor
		// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
		    cursorImg, new Point(0, 0), "blank cursor");
		// Set the blank cursor to the JFrame.
		glCanvas.setCursor(blankCursor);*/
	}
	
	private void showCursor() {
		//glCanvas.setCursor(Cursor.getDefaultCursor());
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		showCursor();
		world.getCamera().released();
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
        Camera cam = world.getCamera();
        cam.setCursor(e.getX(),e.getY());
        
		if(pickedHandle>0) {
			//int x = e.getX();
			//int y = e.getY();
			
			//int dx = x-cursorPreviousX;
			//int dy = y-cursorPreviousY;
			//cursorPreviousX=x;
			//cursorPreviousY=y;
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
        Camera cam = world.getCamera();
        cam.setCursor(e.getX(),e.getY());
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
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	
	
	@Override
	public void windowClosing(WindowEvent arg0) {
		confirmClose();
	}
	
	public void confirmClose() {
        int result = JOptionPane.showConfirmDialog(
                mainFrame,
                "Are you sure you want to quit?",
                "Exit",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
        	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

	public int getPickedHandle() {
		return pickedHandle;
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
			System.out.print(" REVERT TO CAMERA");
			unPick();
			pickNothing();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
