package com.marginallyclever.robotOverlord;


import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.IntBuffer;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

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
import com.marginallyclever.robotOverlord.SoundSystem;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.camera.Camera;
import com.marginallyclever.robotOverlord.commands.UserCommandAbout;
import com.marginallyclever.robotOverlord.commands.UserCommandCheckForUpdate;
import com.marginallyclever.robotOverlord.commands.UserCommandLoad;
import com.marginallyclever.robotOverlord.commands.UserCommandNew;
import com.marginallyclever.robotOverlord.commands.UserCommandQuit;
import com.marginallyclever.robotOverlord.commands.UserCommandRedo;
import com.marginallyclever.robotOverlord.commands.UserCommandSaveAs;
import com.marginallyclever.robotOverlord.commands.UserCommandUndo;
import com.marginallyclever.robotOverlord.world.World;
import com.marginallyclever.util.PropertiesFileHelper;

/**
 * MainGUI is the root window object.
 * @author danroyer
 *
 */
public class RobotOverlord 
implements MouseListener, MouseMotionListener, KeyListener, GLEventListener, WindowListener
{
	static final public String APP_TITLE = "Robot Overlord";
	static final public String APP_URL = "https://github.com/MarginallyClever/Robot-Overlord";
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

	// menus
    // main menu bar
	protected transient JMenuBar mainMenu;
	protected transient JMenuItem buttonUndo,buttonRedo;
	
    // The animator keeps things moving
    private FPSAnimator animator;
    
    // timing for animations
    protected long startTime;
    protected long lastTime;
    private float frameDelay;
    private float frameLength;
    
	// settings
    protected Preferences prefs;
	protected String[] recentFiles = {"","","","","","","","","",""};

	protected boolean checkStackSize;

	// The main frame of the GUI
    protected JFrame mainFrame; 
	// the main view
	protected Splitter splitLeftRight;
	protected GLJPanel glCanvas;
	protected JScrollPane contextMenu;
	
	// undo/redo system
	private UndoManager commandSequence;
	private UndoHelper undoHelper;

	// mouse steering controls
	private boolean isMouseIn;
	private boolean hasLeftDragDeadZone;
	private int cursorStartX,cursorStartY;
	
	// opengl rendering context
	private GLU glu;
	
	
 	protected RobotOverlord() {
		prefs = Preferences.userRoot().node("Evil Overlord");

		System.out.println("\n\n*** CLASSPATH="+System.getProperty("java.class.path")+" ***\n\n");
		
		Translator.start();
		SoundSystem.start();
		
		commandSequence = new UndoManager();
		undoHelper = new UndoHelper(this);
		checkStackSize=false;
		
		isMouseIn=false;
		hasLeftDragDeadZone=false;
		
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
		
        mainFrame = new JFrame( APP_TITLE ); 
    	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setSize( 1224, 768 );
        mainFrame.setLayout(new java.awt.BorderLayout());
    	
        mainMenu = new JMenuBar();
        mainFrame.setJMenuBar(mainMenu);

        frameDelay=0;
        frameLength=1.0f/(float)DEFAULT_FRAMES_PER_SECOND;
      	animator = new FPSAnimator(DEFAULT_FRAMES_PER_SECOND);
      	
        mainFrame.addWindowListener(this);

        GLCapabilities caps = new GLCapabilities(null);
        caps.setSampleBuffers(true);
        caps.setHardwareAccelerated(true);
        caps.setNumSamples(4);
        glCanvas = new GLJPanel(caps);
        animator.add(glCanvas);
        glCanvas.addGLEventListener(this);
        glCanvas.addMouseListener(this);
        glCanvas.addMouseMotionListener(this);
        
        contextMenu = new JScrollPane();

        splitLeftRight = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
        splitLeftRight.add(glCanvas);
        splitLeftRight.add(contextMenu);

        world = new World();

        buildMenu();
        
        pickNow = false;
        selectBuffer = null;
        pickedEntity = null;
        pickNothing();
        
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
        mainFrame.add(splitLeftRight);
        mainFrame.validate();
        mainFrame.setVisible(true);
        animator.start();

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
	
	
	public void setContextPanel(JPanel panel,String title) {
		JPanel container = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty=0;
		c.weightx=1;
		c.gridy=0;
		c.gridx=0;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weighty=1;
		c.gridy++;
        container.add(panel,c);
        
        contextMenu.setViewportView(container);
	}
	
	// see http://www.javacoffeebreak.com/text-adventure/tutorial3/tutorial3.html
	public void loadWorldFromFile(String filename) {
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

	// see http://www.javacoffeebreak.com/text-adventure/tutorial3/tutorial3.html
	public void saveWorldToFile(String filename) {
		FileOutputStream fout=null;
		ObjectOutputStream objectOut=null;
		try {
			fout = new FileOutputStream(filename);
			objectOut = new ObjectOutputStream(fout);
			objectOut.writeObject(world);
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
	
	public void newWorld() {
		this.world = new World();
		pickCamera();
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
	
	public void buildMenu() {
		mainMenu.removeAll();
		
		JMenu menu;
		
		menu = new JMenu(APP_TITLE);
		menu.add(new UserCommandNew(this));        	
		menu.add(new UserCommandLoad(this));
		menu.add(new UserCommandSaveAs(this));
		menu.add(new JSeparator());
		menu.add(new UserCommandAbout(this));
		menu.add(new UserCommandCheckForUpdate(this));
		menu.add(new UserCommandQuit(this));
		mainMenu.add(menu);
        
        menu = new JMenu("Edit");
        menu.add(buttonUndo = new UserCommandUndo(this));
        menu.add(buttonRedo = new UserCommandRedo(this));
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
    
    
    @Override
    public void display( GLAutoDrawable drawable ) {
        long nowTime = System.currentTimeMillis();
        float dt = (nowTime - lastTime)*0.001f;
    	lastTime = nowTime;
    	//System.out.println(dt);
    	
    	GL2 gl2 = drawable.getGL().getGL2();

    	frameDelay+=dt;
    	if(frameDelay>frameLength) {
    		if(checkStackSize) {
	    		IntBuffer v = IntBuffer.allocate(1);
	    		gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,v);
	    		System.out.print("stack depth start = "+v.get(0));
    		}		
	        // draw the world
    		if( world !=null ) {
    			world.render( gl2, frameLength );
    		}
	        frameDelay-=frameLength;

	        if(pickNow) {
		        pickNow=false;
		        pickIntoWorld(gl2);
    		}
			
    		if(checkStackSize) {
	    		IntBuffer v = IntBuffer.allocate(1);
				gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,v);
				System.out.println("stack depth end = "+v.get(0));
    		}
    	}
    }

    
    protected void setPerspectiveMatrix() {
        glu.gluPerspective(60, (float)glCanvas.getSurfaceWidth()/(float)glCanvas.getSurfaceHeight(), 1.0f, 1000.0f);
    }
    
    /**
     * Use glRenderMode(GL_SELECT) to ray pick the item under the cursor.
     * https://github.com/sgothel/jogl-demos/blob/master/src/demos/misc/Picking.java
     * http://web.engr.oregonstate.edu/~mjb/cs553/Handouts/Picking/picking.pdf
     * @param gl2 the openGL render context
     */
    protected void pickIntoWorld(GL2 gl2) {
    	// set up the buffer that will hold the names found under the cursor in furthest to closest.
        selectBuffer = Buffers.newDirectIntBuffer(RobotOverlord.SELECT_BUFFER_SIZE);
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
        world.render( gl2, 0 );

        // return the projection matrix to it's old state.
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glPopMatrix();
        gl2.glFlush();

        // get the picking results and return the render mode to the default 
        int hits = gl2.glRenderMode( GL2.GL_RENDER );

		System.out.println("\n"+hits+" PICKS");
        float z1,z2, zMinBest = Float.MAX_VALUE;
    	int i, j, index=0, nameCount, pickName;
    	Entity newlyPickedEntity = null;
    	
    	for(i=0;i<hits;++i) {
    		nameCount=selectBuffer.get(index++);
    		z1 = (float) (selectBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;
    		z2 = (float) (selectBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;
    		System.out.print("  names="+nameCount+" zMin="+z1+" zMax="+z2);
    		String add=": ";
			for(j=0;j<nameCount-1;++j) {
    			pickName = selectBuffer.get(index++);
        		System.out.print(add+pickName);
        		add=", ";
			}
			if(nameCount>0) {
				pickName = selectBuffer.get(index++);
        		System.out.print(add+pickName);
        		if(zMinBest > z1) {
        			zMinBest = z1;
        			
        			newlyPickedEntity = world.pickObjectWithName(pickName);
        		}
    		}
    		System.out.println();
    	}

    	if(newlyPickedEntity==null) {
			//System.out.println(" NO PICK");
    		unPick();
    		pickNothing();
        } else if(newlyPickedEntity!=pickedEntity) {
			//System.out.print(" PICKED");
			unPick();
			pickEntity(newlyPickedEntity);
		}
    }
	
	public void pickEntity(Entity arg0) {
		pickedEntity=arg0;
		setContextPanel(arg0.getAllContextPanels(this),arg0.getDisplayName());
	}
	
    public void unPick() {
		if(pickedEntity!=null) {
			pickedEntity.unPick();
			pickedEntity=null;
		}
    }
    
    public void pickNothing() {
    	pickedEntity=null;
    	setContextPanel(world.getControlPanel(this),Translator.get("Everything"));
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
		hasLeftDragDeadZone=false;
		cursorStartX=this.mainFrame.getWidth()/2;
		cursorStartY=this.mainFrame.getHeight()/2;

		hideCursor();
		
		world.getCamera().mousePressed(e);
	}
	
	private void hideCursor() {
		// Hide cursor
		// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		// Create a new blank cursor.
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
		    cursorImg, new Point(0, 0), "blank cursor");
		// Set the blank cursor to the JFrame.
		glCanvas.setCursor(blankCursor);
	}
	
	private void showCursor() {
		glCanvas.setCursor(Cursor.getDefaultCursor());
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		world.getCamera().mouseReleased(e);
		showCursor();
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		isMouseIn=true;
		glCanvas.requestFocus();
	}
	@Override
	public void mouseExited(MouseEvent e) {
		isMouseIn=false;
		world.getCamera().lostFocus();
	}


	public static final int DRAG_DEAD_ZONE = 10;
	
	@Override
	public void mouseDragged(MouseEvent e) {		
		int x = e.getX();
		int y = e.getY();
		
		if(hasLeftDragDeadZone) {
			if(Math.abs(cursorStartX - x) > DRAG_DEAD_ZONE ||
					Math.abs(cursorStartY - y ) > DRAG_DEAD_ZONE ) {
				hasLeftDragDeadZone = true;
			}
		}
		
		world.getCamera().mouseDragged(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {}
	

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


	@Override
	public void windowDeactivated(WindowEvent arg0) {}


	@Override
	public void windowDeiconified(WindowEvent arg0) {}


	@Override
	public void windowIconified(WindowEvent arg0) {}


	@Override
	public void windowOpened(WindowEvent arg0) {}


	@Override
	public void keyPressed(KeyEvent e) {
		if(isMouseIn) {
			world.getCamera().keyPressed(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(isMouseIn) {
			world.getCamera().keyReleased(e);
		}
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
			System.out.print(" REVERT TO CAMERA");
			unPick();
			pickNothing();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}
}
