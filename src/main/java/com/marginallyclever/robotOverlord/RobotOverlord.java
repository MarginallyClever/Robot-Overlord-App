package com.marginallyclever.robotOverlord;


import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import com.jogamp.opengl.util.Animator;
import com.marginallyclever.robotOverlord.PropertiesFileHelper;
import com.marginallyclever.robotOverlord.Camera.Camera;
import com.marginallyclever.robotOverlord.world.World;

/**
 * MainGUI is the root window object.
 * @author danroyer
 *
 */
public class RobotOverlord 
implements ActionListener, MouseListener, MouseMotionListener, KeyListener, GLEventListener, WindowListener
{
	static final String APP_TITLE = "Robot Overlord";
	static final String APP_URL = "https://github.com/MarginallyClever/Robot-Overlord";
	
	// select picking
	static final int SELECT_BUFFER_SIZE=256;
	protected IntBuffer selectBuffer = null;
	protected boolean pickNow;
	protected double pickX, pickY;
	
	static final long serialVersionUID=1;
	/// used for checking the application version with the github release, for "there is a new version available!" notification
	public static final String VERSION = PropertiesFileHelper.getVersionPropertyValue();

    /// the world within the simulator and all that it contains.
	protected World world = null;

	// menus
    /// main menu bar
	protected JMenuBar mainMenu;
	/// load a new world
	protected JMenuItem buttonNew;
    /// show the load level dialog
	protected JMenuItem buttonLoad;
    /// show the save level dialog
	protected JMenuItem buttonSave;
    /// show the about dialog
	protected JMenuItem buttonAbout;
    /// check the version against github and notify the user if they wer up to date or not
	protected JMenuItem buttonCheckForUpdate;
    /// quit the application
	protected JMenuItem buttonQuit;
	
	protected JMenuItem buttonUndo, buttonRedo;
	
    /// The animator keeps things moving
    private Animator animator;
    
    // timing for animations
    protected long startTime;
    protected long lastTime;
    private float frameDelay=0;
    private float frameLength=1.0f/30.0f;
    
	// settings
    protected Preferences prefs;
	protected String[] recentFiles = {"","","","","","","","","",""};

	protected boolean checkStackSize;

	/// The main frame of the GUI
    protected JFrame mainFrame; 
	// the main view
	protected Splitter splitLeftRight;
	protected GLJPanel glCanvas;
	protected JScrollPane contextMenu;

	protected GLU glu;
	
	// undo/redo system
	private UndoManager commandSequence;
	private UndoHelper undoHelper;
	
	private boolean isMouseIn;
	private boolean hasLeftDragDeadZone;
	private int cursorStartX,cursorStartY;
	
 	protected RobotOverlord() {
		prefs = Preferences.userRoot().node("Evil Overlord");
		
		commandSequence = new UndoManager();
		undoHelper = new UndoHelper(this);
		checkStackSize=false;
		
		isMouseIn=false;
		hasLeftDragDeadZone=false;
		
		glu = new GLU();
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

      	animator = new Animator();
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
        pickNow=false;
        pickCamera();

        buildMenu();
        
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
	

	public JFrame getMainFrame() {
		return mainFrame;
	}
	
	
	public UndoManager getUndoManager() {
		return commandSequence;
	}
	
	
	public UndoHelper getUndoHelper() {
		return undoHelper;
	}
	
	
	public void setContextMenu(JPanel panel,String title) {
		JPanel container = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty=0;
		c.weightx=1;
		c.gridy=0;
		c.gridx=0;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
        container.add(new JLabel(title,JLabel.CENTER),c);
        c.gridy++;
        container.add(new JSeparator(),c);
		c.weighty=1;
		c.gridy++;
        container.add(panel,c);
        
        contextMenu.setViewportView(container);
	}
	
	// see http://www.javacoffeebreak.com/text-adventure/tutorial3/tutorial3.html
	void loadWorldFromFile(String filename) {
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
	void saveWorldToFile(String filename) {
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
	
	void saveWorldDialog() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(this.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            saveWorldToFile(fc.getSelectedFile().getAbsolutePath());
		}
	}
	
	void loadWorldDialog() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this.getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            loadWorldFromFile(fc.getSelectedFile().getAbsolutePath());
		}
	}
	
	void newWorld() {
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
		
        JMenu menu = new JMenu(APP_TITLE);
        
        	buttonNew = new JMenuItem("New",KeyEvent.VK_N);
        	buttonNew.addActionListener(this);
	        menu.add(buttonNew);
        	
        	buttonLoad = new JMenuItem("Load...",KeyEvent.VK_L);
        	buttonLoad.addActionListener(this);
	        menu.add(buttonLoad);

        	buttonSave = new JMenuItem("Save As...",KeyEvent.VK_S);
        	buttonSave.addActionListener(this);
	        menu.add(buttonSave);

	        menu.add(new JSeparator());
	        
            buttonAbout = new JMenuItem("About",KeyEvent.VK_A);
	        buttonAbout.getAccessibleContext().setAccessibleDescription("About this program");
	        buttonAbout.addActionListener(this);
	        menu.add(buttonAbout);
	        
	        buttonCheckForUpdate = new JMenuItem("Check for update",KeyEvent.VK_U);
	        buttonCheckForUpdate.addActionListener(this);
	        menu.add(buttonCheckForUpdate);
	        
	        buttonQuit = new JMenuItem("Quit",KeyEvent.VK_Q);
	        buttonQuit.getAccessibleContext().setAccessibleDescription("Goodbye...");
	        buttonQuit.addActionListener(this);
	        menu.add(buttonQuit);
       
        mainMenu.add(menu);
        
        JMenu menuEdit = new JMenu("Edit");
    	buttonUndo = new JMenuItem("Undo",KeyEvent.VK_Z);
    	buttonUndo.addActionListener(this);
    	menuEdit.add(buttonUndo);
    	buttonRedo = new JMenuItem("Redo",KeyEvent.VK_Y);
    	buttonRedo.addActionListener(this);
    	menuEdit.add(buttonRedo);
        mainMenu.add(menuEdit);

        mainMenu.add(world.buildMenu());
        mainMenu.updateUI();
        
        updateMenu();
	}


	public void updateMenu() {
        buttonUndo.setText(commandSequence.getUndoPresentationName());
		buttonRedo.setText(commandSequence.getRedoPresentationName());
		buttonUndo.getParent().validate();
		buttonUndo.setEnabled(commandSequence.canUndo());
	    buttonRedo.setEnabled(commandSequence.canRedo());
    }
	
	
	public void checkForUpdate() {
		String updateURL = "https://github.com/MarginallyClever/Robot-Overlord/releases/latest";
		try {
			URL github = new URL(updateURL);
			HttpURLConnection conn = (HttpURLConnection) github.openConnection();
			conn.setInstanceFollowRedirects(false);  //you still need to handle redirect manully.
			HttpURLConnection.setFollowRedirects(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

			String inputLine;
			if ((inputLine = in.readLine()) != null) {
				// parse the URL in the text-only redirect
				String matchStart = "<a href=\"";
				String matchEnd = "\">";
				int start = inputLine.indexOf(matchStart);
				int end = inputLine.indexOf(matchEnd);
				if (start != -1 && end != -1) {
					inputLine = inputLine.substring(start + matchStart.length(), end);
					// parse the last part of the redirect URL, which contains the release tag (which is the VERSION)
					inputLine = inputLine.substring(inputLine.lastIndexOf("/") + 1);

					System.out.println("last release: " + inputLine);
					System.out.println("your VERSION: " + VERSION);
					//System.out.println(inputLine.compareTo(VERSION));

					if (inputLine.compareTo(VERSION) > 0) {
						JOptionPane.showMessageDialog(null, "A new version of this software is available.  The latest version is "+inputLine+"\n"
								+"Please visit http://www.marginallyclever.com/ to get the new hotness.");
					} else {
						JOptionPane.showMessageDialog(null, "This version is up to date.");
					}
				}
			} else {
				throw new Exception();
			}
			in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Sorry, I failed.  Please visit "+updateURL+" to check yourself.");
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if( subject == buttonNew ) this.newWorld();
		else if( subject == buttonLoad ) this.loadWorldDialog();
		else if( subject == buttonSave ) this.saveWorldDialog();
		else if( subject == buttonAbout ) doAbout();
		else if( subject == buttonCheckForUpdate ) checkForUpdate();
		else if( subject == buttonQuit ) onClose();
		else if( subject == buttonRedo ) redo();
		else if( subject == buttonUndo ) undo();
	}

	private void doAbout() {
		JOptionPane.showMessageDialog(null,"<html><body>"
				+"<h1>"+APP_TITLE+" "+VERSION+"</h1>"
				+"<h3><a href='http://www.marginallyclever.com/'>http://www.marginallyclever.com/</a></h3>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>To get the latest version please visit<br><a href='"+APP_URL+"'>"+APP_URL+"</a></p><br>"
				+"<p>This program is open source and free.  If this was helpful<br> to you, please buy me a thank you beer through Paypal.</p>"
				+"</body></html>");
	}

	
	private void undo() {
		try {
			commandSequence.undo();
		} catch (CannotUndoException ex) {
			ex.printStackTrace();
		} finally {
			updateMenu();
		}
	}
	
	
	private void redo() {
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
			if( recentFiles[i]==null ) recentFiles[i] = new String("");
			if( recentFiles[i].isEmpty()==false ) {
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
        gl2.setSwapInterval(1);

    	gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		setPerspectiveMatrix();
        
        world.setup( gl2 );
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

    }
    
    
    @Override
    public void dispose( GLAutoDrawable drawable ) {}
    
    
    @Override
    public void display( GLAutoDrawable drawable ) {
        long now_time = System.currentTimeMillis();
        float dt = (now_time - lastTime)*0.001f;
    	lastTime = now_time;
    	//System.out.println(dt);
    	
		// Clear The Screen And The Depth Buffer
    	GL2 gl2 = drawable.getGL().getGL2();
        
    	if(frameDelay<frameLength) {
    		frameDelay+=dt;
    	} else {
    		if(checkStackSize) {
	    		IntBuffer v = IntBuffer.allocate(1);
	    		gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,v);
	    		System.out.print("start = "+v.get(0));
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
				System.out.println(" end = "+v.get(0));
    		}
    	}
    	frameDelay+=dt;
    }

    
    protected void setPerspectiveMatrix() {
        glu.gluPerspective(60, (float)glCanvas.getSurfaceWidth()/(float)glCanvas.getSurfaceHeight(), 1.0f, 1000.0f);
    }
    
    /**
     * Use glRenderMode(GL_SELECT) to ray pick the item under the cursor.
     * https://github.com/sgothel/jogl-demos/blob/master/src/demos/misc/Picking.java
     * http://web.engr.oregonstate.edu/~mjb/cs553/Handouts/Picking/picking.pdf
     * @param gl2
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

        boolean pickFound=false;
        if(hits!=0) {
        	int index=0;
        	int i;
        	for(i=0;i<hits;++i) {
        		int names=selectBuffer.get(index++);
//                float z1 = (float) (selectBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;
//                float z2 = (float) (selectBuffer.get(index++) & 0xffffffffL) / (float)0x7fffffff;
        		selectBuffer.get(index++); // near z
        		selectBuffer.get(index++); // far z
//                System.out.println("zMin:"+z1);
//                System.out.println("zMaz:"+z2);
//    			System.out.println("names:"+names);
    			if(names>0) {
        			int name = selectBuffer.get(index++);
    				ObjectInWorld newObject = world.pickObjectWithName(name);
    				setContextMenu(newObject.buildPanel(this),newObject.getDisplayName());
   					pickFound=true;
                	return;
        		}
        	}
        }
        if(pickFound==false) {
        	pickCamera();
        }
        
    }
	
	public void pickCamera() {
		Camera camera = world.getCamera();
		if(camera!=null) {
			setContextMenu(camera.buildPanel(this),camera.getDisplayName());
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
		onClose();
	}
	
	private void onClose() {
        int result = JOptionPane.showConfirmDialog(
                mainFrame,
                "Please confirm",
                "Are you sure you want to quit?",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
        	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	// Run this on another thread than the AWT event queue to
	        // make sure the call to Animator.stop() completes before
	        // exiting
	        new Thread(new Runnable() {
	            public void run() {
	              animator.stop();
	              System.exit(0);
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
		// TODO Auto-generated method stub
		if(isMouseIn) {
			world.getCamera().keyPressed(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if(isMouseIn) {
			world.getCamera().keyReleased(e);
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
