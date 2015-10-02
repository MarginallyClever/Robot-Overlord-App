package com.marginallyclever.evilOverlord;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.prefs.Preferences;

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
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLPipelineFactory;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.Animator;

/**
 * MainGUI is the root window object.
 * @author danroyer
 *
 */
public class MainGUI 
implements ActionListener, GLEventListener
{
	static final String APP_TITLE = "Evil Overlord";
	
	static final long serialVersionUID=1;
	/// used for checking the application version with the github release, for "there is a new version available!" notification
	static final String version="2";

    /// the world within the simulator and all that it contains.
	protected World world;

	// menus
    /// main menu bar
	protected JMenuBar mainMenu;
    /// show the about dialog
	protected JMenuItem buttonAbout;
    /// check the version against github and notify the user if they wer up to date or not
	protected JMenuItem buttonCheckForUpdate;
    /// quit the application
	protected JMenuItem buttonQuit;

	// TODO move all these to a context sensitive menu
	protected JMenuItem buttonStart;
	protected JMenuItem buttonStartAt;
	protected JMenuItem buttonPause;
	protected JMenuItem buttonHalt;
	
	
	/// the main frame of the GUI
    protected JFrame frame; 
    /// the animator keeps things moving
    protected Animator animator = new Animator();
    
    /* timing for animations */
    protected long start_time;
    protected long last_time;
    
	// settings
    protected Preferences prefs;
	protected String[] recentFiles = {"","","","","","","","","",""};
	
	// the main view
	protected Splitter split_left_right;
	protected GLJPanel glCanvas;
	protected JScrollPane contextMenu;
    

	public JFrame GetMainFrame() {
		return frame;
	}
	
	
	protected MainGUI() {
		prefs = Preferences.userRoot().node("Evil Overlord");
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
		
		LoadConfig();
		
        frame = new JFrame( APP_TITLE ); 
        frame.setSize( 1224, 768 );
        frame.setLayout(new java.awt.BorderLayout());

        mainMenu = new JMenuBar();
        frame.setJMenuBar(mainMenu);

        
        final Animator animator = new Animator();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
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
          });

        glCanvas = new GLJPanel();
        animator.add(glCanvas);
        glCanvas.addGLEventListener(this);
        
        contextMenu = new JScrollPane();

        split_left_right = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
        split_left_right.add(glCanvas);
        split_left_right.add(contextMenu);

        world = new World(this);

        updateMenu();
        
//		frame.addKeyListener(this);
//		glCanvas.addMouseListener(this);
//		glCanvas.addMouseMotionListener(this);
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
        frame.add(split_left_right);
        frame.validate();
        frame.setVisible(true);
        animator.start();

        last_time = start_time = System.currentTimeMillis();
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
	
	/*
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
	
	/*
	protected void LoadGenerators() {
		// TODO find the generator jar files and load them.
		
		generators = new GcodeGenerator[3];
		generators[0] = new LoadGCodeGenerator(this);
		generators[1] = new YourMessageHereGenerator(this);
		generators[2] = new HilbertCurveGenerator(this);
		
		generatorButtons = new JMenuItem[generators.length];
	}
	
	protected JMenu LoadGenerateMenu() {
		JMenu menu = new JMenu("Gcode");
        menu.setEnabled(!world.robot0.isRunning());
        
        for(int i=0;i<generators.length;++i) {
        	generatorButtons[i] = new JMenuItem(generators[i].GetMenuName());
        	generatorButtons[i].addActionListener(this);
        	menu.add(generatorButtons[i]);
        }
        
        return menu;
	}

	
	public JMenu LoadDrawMenu() {
        // Draw menu
        JMenu menu = new JMenu("Action");

        buttonStart = new JMenuItem("Start",KeyEvent.VK_S);
        buttonStart.addActionListener(this);
    	buttonStart.setEnabled(world.robot0.isPortConfirmed() && !world.robot0.isRunning());
        menu.add(buttonStart);

        buttonStartAt = new JMenuItem("Start at...");
        buttonStartAt.addActionListener(this);
        buttonStartAt.setEnabled(world.robot0.isPortConfirmed() && !world.robot0.isRunning());
        menu.add(buttonStartAt);

        buttonPause = new JMenuItem("Pause");
        buttonPause.addActionListener(this);
        buttonPause.setEnabled(world.robot0.isPortConfirmed() && world.robot0.isRunning());
        menu.add(buttonPause);

        buttonHalt = new JMenuItem(("Halt"),KeyEvent.VK_H);
        buttonHalt.addActionListener(this);
        buttonHalt.setEnabled(world.robot0.isPortConfirmed() && world.robot0.isRunning());
        menu.add(buttonHalt);
        
        return menu;
	}
	*/
	
	public void updateMenu() {
		mainMenu.removeAll();
		
        JMenu menu = new JMenu("RobotTrainer");
        
            buttonAbout = new JMenuItem("About",KeyEvent.VK_A);
	        buttonAbout.getAccessibleContext().setAccessibleDescription("About this program");
	        buttonAbout.addActionListener(this);
	        menu.add(buttonAbout);
	        
	        buttonCheckForUpdate = new JMenuItem("Check for update",KeyEvent.VK_A);
	        buttonCheckForUpdate.addActionListener(this);
	        menu.add(buttonCheckForUpdate);
	        
	        buttonQuit = new JMenuItem("Quit");
	        buttonQuit.getAccessibleContext().setAccessibleDescription("Goodbye...");
	        buttonQuit.addActionListener(this);
	        menu.add(buttonQuit);
        
        mainMenu.add(menu);
        
        mainMenu.add(world.updateMenu());
        
        mainMenu.updateUI();
    }
	
	
	public void CheckForUpdate() {
		try {
		    // Get Github info?
			URL github = new URL("https://www.marginallyclever.com/other/software-update-check.php?id=3");
	        BufferedReader in = new BufferedReader(new InputStreamReader(github.openStream()));

	        String inputLine;
	        if((inputLine = in.readLine()) != null) {
	        	if( inputLine.compareTo(version) !=0 ) {
	        		JOptionPane.showMessageDialog(null,"A new version of this software is available.  The latest version is "+inputLine+"\n"
	        											+"Please visit http://www.marginallyclever.com/ to get the new hotness.");
	        	} else {
	        		JOptionPane.showMessageDialog(null,"This version is up to date.");
	        	}
	        } else {
	        	throw new Exception();
	        }
	        in.close();
		} catch (Exception e) {
    		JOptionPane.showMessageDialog(null,"Sorry, I failed.  Please visit http://www.marginallyclever.com/ to check yourself.");
		}
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if( subject == buttonAbout ) {
			JOptionPane.showMessageDialog(null,"<html><body>"
					+"<h1>"+APP_TITLE+" v"+version+"</h1>"
					+"<h3><a href='http://www.marginallyclever.com/'>http://www.marginallyclever.com/</a></h3>"
					+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
					+"<p>To get the latest version please visit<br><a href='https://github.com/MarginallyClever/arm5'>https://github.com/MarginallyClever/arm5</a></p><br>"
					+"<p>This program is open source and free.  If this was helpful<br> to you, please buy me a thank you beer through Paypal.</p>"
					+"</body></html>");
			return;
		}
		if( subject == buttonCheckForUpdate ) {
			CheckForUpdate();
			return;
		}
		if( subject == buttonQuit ) {
			System.exit(0);
			return;
		}
		
		/*
		if(GeneratorMenuAction(e)) {
			return;
		}
		
		// Draw
		if( subject == buttonStart ) {
			world.robot0.Start();
			return;
		}
		if( subject == buttonStartAt ) {
			world.robot0.StartAt();
			return;
			
		}
		if( subject == buttonPause ) {
			world.robot0.Pause();
		}
		if( subject == buttonHalt ) {
			world.robot0.Halt();
			return;
		}
		*/
	}

	protected void LoadConfig() {
		GetRecentFiles();
	}

	protected void SaveConfig() {
		GetRecentFiles();
	}
	
	/*
	protected boolean GeneratorMenuAction(ActionEvent e) {
		Object subject = e.getSource();
		
        for(int i=0;i<generators.length;++i) {
        	if(subject==generatorButtons[i]) {
        		generators[i].Generate();
        		updateMenu();
        		return true;
        	}
		}
		return false;
	}
	*/
	
	
	/**
	 * changes the order of the recent files list in the File submenu, saves the updated prefs, and refreshes the menus.
	 * @param filename the file to push to the top of the list.
	 */
	public void UpdateRecentFiles(String filename) {
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
	public void RemoveRecentFile(String filename) {
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
	
	// Load recent files from prefs
	public void GetRecentFiles() {
		int i;
		for(i=0;i<recentFiles.length;++i) {
			recentFiles[i] = prefs.get("recent-files-"+i, recentFiles[i]);
		}
	}

	/**
	 * Open a gcode file to run on a robot.  This doesn't make sense if there's more than one robot!
	 * @param filename the file to open
	 */
	public void OpenFile(String filename) {
		
	}

    @Override
    public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
    	GL2 gl2 = glautodrawable.getGL().getGL2();
        gl2.setSwapInterval(1);

		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		//gl2.glOrtho(0, screen_width, 0, screen_height, 1, -1);
		GLU glu = new GLU();
        glu.gluPerspective(45, (float)width/(float)height, 1.0f, 1000.0f);
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		
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
                gl = gl.getContext().setGL( GLPipelineFactory.create("javax.media.opengl.Debug", null, gl, null) );
            } catch (Exception e) {e.printStackTrace();}
        }

        if(glTrace) {
            try {
                // Trace ..
                gl = gl.getContext().setGL( GLPipelineFactory.create("javax.media.opengl.Trace", null, gl, new Object[] { System.err } ) );
            } catch (Exception e) {e.printStackTrace();}
        }
    }
    
    
    @Override
    public void dispose( GLAutoDrawable glautodrawable ) {
    }
    
    
    private float frame_delay=0;
    private float frame_length=1.0f/30.0f;
    
    @Override
    public void display( GLAutoDrawable glautodrawable ) {
        long now_time = System.currentTimeMillis();
        float dt = (now_time - last_time)*0.001f;
    	last_time = now_time;
    	//System.out.println(dt);
    	
		// Clear The Screen And The Depth Buffer
    	GL2 gl2 = glautodrawable.getGL().getGL2();
    	
    	if(frame_delay<frame_length) {
    		frame_delay+=dt;
    	} else {
    		boolean checkStackSize=false;
    		if(checkStackSize) {
	    		IntBuffer v = IntBuffer.allocate(1);
	    		gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,v);
	    		System.out.print("start = "+v.get(0));
    		}    		
	        // draw the world
	        world.render( gl2, frame_length );
	        frame_delay-=frame_length;
			
    		if(checkStackSize) {
	    		IntBuffer v = IntBuffer.allocate(1);
				gl2.glGetIntegerv (GL2.GL_MODELVIEW_STACK_DEPTH,v);
				System.out.println(" end = "+v.get(0));
    		}
    	}
    	frame_delay+=dt;
    }
}
