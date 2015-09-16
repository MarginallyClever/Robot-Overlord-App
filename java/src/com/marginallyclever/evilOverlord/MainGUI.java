package com.marginallyclever.evilOverlord;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLPipelineFactory;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;

import Generators.GcodeGenerator;
import Generators.LoadGCodeGenerator;
import Generators.HilbertCurveGenerator;
import Generators.YourMessageHereGenerator;

import com.jogamp.opengl.util.Animator;


public class MainGUI 
implements ActionListener, GLEventListener, MouseListener, MouseMotionListener, KeyListener
{
	static final long serialVersionUID=1;
	static final String version="2";
    static MainGUI __singleton;

	World world;

	/** menus */
	private JMenuBar mainMenu;
    private JMenuItem buttonStart, buttonStartAt, buttonPause, buttonHalt;
    private JMenuItem buttonAbout, buttonCheckForUpdate;
    private JMenuItem buttonQuit;
	
	/* window management */
    final JFrame frame; 
    /* animation system */
    final Animator animator = new Animator();
    
    /* timing for animations */
    long start_time;
    long last_time;
    

	// settings
	private Preferences prefs;
	private String[] recentFiles = {"","","","","","","","","",""};
	
	// Generators
	GcodeGenerator [] generators;
	JMenuItem generatorButtons[];
    
	
	final GLJPanel glCanvas;

    private JPanel cameraPanel=null;
    private JPanel arm5Panel=null;

    public JTabbedPane contextMenu;
    public Splitter split_left_right;
    

	
	static public MainGUI getSingleton() {
		if(__singleton==null) {
			__singleton = new MainGUI();
		}
		return __singleton;
	}
	
	
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
		LoadGenerators();
		
        frame = new JFrame( "Evil Overlord" ); 
        frame.setSize( 1224, 768 );
        frame.setLayout(new java.awt.BorderLayout());


        world = new World();

        mainMenu = new JMenuBar();
        frame.setJMenuBar(mainMenu);
        updateMenu();

        
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
        
        cameraPanel = world.camera.getControlPanel();
        arm5Panel = world.robot0.getControlPanel();
        
        contextMenu = new JTabbedPane();
        JScrollPane p;
        p = new JScrollPane();
        p.setViewportView(cameraPanel);
        contextMenu.addTab("Camera",null,p,null);
        p = new JScrollPane();
        p.setViewportView(arm5Panel);
        contextMenu.addTab("Arm",null,p,null);

        split_left_right = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
        split_left_right.add(glCanvas);
        split_left_right.add(contextMenu);
        
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
	
	protected void LoadGenerators() {
		// TODO find the generator jar files and load them.
		
		generators = new GcodeGenerator[3];
		generators[0] = new LoadGCodeGenerator();
		generators[1] = new YourMessageHereGenerator();
		generators[2] = new HilbertCurveGenerator();
		
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
    	buttonStart.setEnabled(world.robot0.isConfirmed() && !world.robot0.isRunning());
        menu.add(buttonStart);

        buttonStartAt = new JMenuItem("Start at...");
        buttonStartAt.addActionListener(this);
        buttonStartAt.setEnabled(world.robot0.isConfirmed() && !world.robot0.isRunning());
        menu.add(buttonStartAt);

        buttonPause = new JMenuItem("Pause");
        buttonPause.addActionListener(this);
        buttonPause.setEnabled(world.robot0.isConfirmed() && world.robot0.isRunning());
        menu.add(buttonPause);

        buttonHalt = new JMenuItem(("Halt"),KeyEvent.VK_H);
        buttonHalt.addActionListener(this);
        buttonHalt.setEnabled(world.robot0.isConfirmed() && world.robot0.isRunning());
        menu.add(buttonHalt);
        
        return menu;
	}
	
	
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

        mainMenu.add(LoadGenerateMenu());
        
        mainMenu.add(LoadDrawMenu());
        
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
					+"<h1>Evil Overlord v"+version+"</h1>"
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

	}

	protected void LoadConfig() {
		GetRecentFiles();
	}

	protected void SaveConfig() {
		GetRecentFiles();
	}
	
	
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
	

	@Override
	public void keyPressed(java.awt.event.KeyEvent e) {
    	world.keyPressed(e);
	}

	@Override
	public void keyReleased(java.awt.event.KeyEvent e) {
		world.keyReleased(e);				
	}

	@Override
	public void keyTyped(java.awt.event.KeyEvent e) {
		// TODO Auto-generated method stub
	}
	
	public void mousePressed(MouseEvent e) {
		world.mousePressed(e);
    }
        
    public void mouseReleased(MouseEvent e) {
    	world.mouseReleased(e);
    }
        
    public void mouseDragged(MouseEvent e) {
    	world.mouseDragged(e);
	}
    public void mouseMoved(MouseEvent e) {
    	world.mouseMoved(e);
	}

    public void mouseClicked(MouseEvent e) {
    	world.mouseClicked(e);
    }
        
    public void mouseEntered(MouseEvent e) {
    	world.mouseEntered(e);
    }
        
    public void mouseExited(MouseEvent e) {
    	world.mouseExited(e);
    }
}
