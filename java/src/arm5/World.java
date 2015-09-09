package arm5;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


public class World
implements ActionListener {
	/* menus */
	JMenuItem buttonRescan, buttonDisconnect;
	
	/* world contents */
	Camera camera = new Camera();
	Arm5Robot robot0 = new Arm5Robot("0");
	//Arm3Robot robot1 = new Arm3Robot("1");
	
	final int NUM_ROBOTS = 1;
	protected int activeRobot=0;

	
	public World() {
		//robot0.MoveBase(new Vector3f(-25f,0f,0f));
		robot0.FinalizeMove();
		/*
		robot1.MoveBase(new Vector3f(25f,0f,0f));
		robot1.RotateBase(180f,0f);
		robot1.FinalizeMove();
		*/
	}
	

    protected void setup( GL2 gl2 ) {
		gl2.glDepthFunc(GL2.GL_LESS);
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthMask(true);
    }
    

    public void mouseClicked(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {
    	camera.mouseDragged(e);
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
    	camera.mousePressed(e);
    }
    public void mouseReleased(MouseEvent e) {
    	camera.mouseReleased(e);
    }
    public void mouseWheelMoved(MouseEvent e) {}
    
    public void keyPressed(KeyEvent e) {
    	if(e.getKeyCode() == KeyEvent.VK_SPACE) {
    		activeRobot=(activeRobot+1)%NUM_ROBOTS;
    	}
    	camera.keyPressed(e);
    	switch(activeRobot) {
    	case 0:    	robot0.keyPressed(e); break;
    	//case 1:    	robot1.keyPressed(e); break;
    	}
    }
    
    public void keyReleased(KeyEvent e) {
    	camera.keyReleased(e);
    	switch(activeRobot) {
    	case 0:    	robot0.keyReleased(e); break;
    	//case 1:    	robot1.keyReleased(e); break;
    	}
    }
    

	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		if(subject==buttonRescan) {
			robot0.DetectSerialPorts();
			//robot1.arduino.DetectSerialPorts();
			//TODO tell RobotTrainer to update all menus
			MainGUI.getSingleton().updateMenu();
			return;
		}
		if(subject==buttonDisconnect) {
			robot0.ClosePort();
			//robot1.arduino.ClosePort();
			MainGUI.getSingleton().updateMenu();
			return;
		}
	}
	
    public JMenu updateMenu() {
    	JMenu menu, subMenu;
        
        // connection menu
        menu = new JMenu("Connection(s)");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Connection settings.");
        
    	subMenu=robot0.getMenu();
        subMenu.setText("Arm 0");
        menu.add(subMenu);
/*
     	subMenu=robot1.getMenu();
        subMenu.setText("Arm 1");
        menu.add(subMenu);
*/
        buttonRescan = new JMenuItem("Rescan Ports",KeyEvent.VK_R);
        buttonRescan.getAccessibleContext().setAccessibleDescription("Rescan the available ports.");
        buttonRescan.addActionListener(this);
        menu.add(buttonRescan);

        menu.addSeparator();
        
        buttonDisconnect = new JMenuItem("Disconnect",KeyEvent.VK_D);
        buttonDisconnect.addActionListener(this);
        menu.add(buttonDisconnect);
        
        return menu;
    }
	
	public void render(GL2 gl2, float dt ) {
    	gl2.glClearColor(212.0f/255.0f, 233.0f/255.0f, 255.0f/255.0f, 0.0f);
        // Special handling for the case where the GLJPanel is translucent
        // and wants to be composited with other Java 2D content
        if (GLProfile.isAWTAvailable() &&
            (gl2 instanceof javax.media.opengl.awt.GLJPanel) &&
            !((javax.media.opengl.awt.GLJPanel) gl2).isOpaque() &&
            ((javax.media.opengl.awt.GLJPanel) gl2).shouldPreserveColorBufferIfTranslucent()) {
          gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
        } else {
          gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        }
        
    	
		//gl2.glEnable(GL2.GL_CULL_FACE);
		
		gl2.glPushMatrix();
			camera.update(dt);
			camera.render(gl2);

			gl2.glDisable(GL2.GL_LIGHTING);
			PrimitiveSolids.drawGrid(gl2,50,5);
			 // Enable lighting
			gl2.glEnable(GL2.GL_LIGHTING);
			gl2.glEnable(GL2.GL_LIGHT0);
			gl2.glEnable(GL2.GL_COLOR_MATERIAL);
			/*
			FloatBuffer position = ByteBuffer.allocateDirect(16).asFloatBuffer();
		    position.mark();
		    position.put(new float[] { -10f, 10f, 50f, 0f }); // even values about 10e3 for the first three parameters aren't changing anything
		    position.reset();
			gl2.glLight(GL2.GL_LIGHT0, GL2.GL_POSITION, position);

		    FloatBuffer ambient = ByteBuffer.allocateDirect(16).asFloatBuffer();
		    ambient.mark();
		    ambient.put(new float[] { 0.85f, 0.85f, 0.85f, 1f });
		    ambient.reset();
		    gl2.glLight(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient);

		    FloatBuffer diffuse = ByteBuffer.allocateDirect(16).asFloatBuffer();
		    diffuse.mark();
		    diffuse.put(new float[] { 1.0f, 1.0f, 1.0f, 1f });
		    diffuse.reset();
		    gl2.glLight(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse);
*/
			robot0.PrepareMove(dt);
			//robot1.PrepareMove(dt);
			//if(WillCollide(robot0,robot1) == false) 
			{
				robot0.FinalizeMove();
				//robot1.FinalizeMove();
			}
			
			robot0.render(gl2);
			//robot1.render(gl2);
			
		gl2.glPopMatrix();
	}

	boolean WillCollide(Arm5Robot a,Arm5Robot b) {
		// TODO complete me
		//Get the cylinders for each robot
		BoundingVolume [] from = a.GetBoundingVolumes();
		BoundingVolume [] to = b.GetBoundingVolumes();
		// test cylinder/cylinder intersection
		for(int i=0;i<from.length;++i) {
			for(int j=0;j<to.length;++j) {
				if(IntersectionTester.CylinderCylinder((Cylinder)from[i],(Cylinder)to[i])) {
					return true;
				}
			}
		}
		// if there is any hit, return true.
		return false;
	}
}
