package com.marginallyclever.robotOverlord.Camera;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.jogamp.opengl.GL2
;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class Camera extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7511310951758205827L;
	
	/** position of camera */
	protected Vector3f forward = new Vector3f(0,1,0);
	protected Vector3f up = new Vector3f(0,0,1);
	protected Vector3f right = new Vector3f(1,0,0);
	protected int prevMouseX, prevMouseY;
	protected boolean mouseRButtonDown = false;
	// angles
	protected float pan, tilt;

	protected int pan_dir=0;
	protected int tilt_dir=0;
	
	protected int move_up=0;
	protected int move_left=0;
	protected int move_forward=0;
	protected int move_down=0;
	protected int move_right=0;
	protected int move_back=0;
	
	CameraControlPanel cameraPanel;
	
	
	public Camera() {
		super();
		
		setDisplayName("Camera");
				
		setPosition(new Vector3f(0,40,-20));
		pan=0;
		tilt=90;
	}

	
	public ArrayList<JPanel> getControlPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getControlPanels(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		cameraPanel = new CameraControlPanel(gui,this);
		list.add(cameraPanel);
		
		return list;
	}
	
	
	public void mousePressed(MouseEvent e) {
        prevMouseX = e.getXOnScreen();
        prevMouseY = e.getYOnScreen();
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        	mouseRButtonDown = true;
        }
	}
	
	
	public void mouseReleased(MouseEvent e) {
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        	mouseRButtonDown = false;
        }
	}
	
	public void lostFocus() {
		move_forward=0;
		move_left=0;
		move_up=0;
	}
	
	
	public void mouseDragged(MouseEvent e) {
		if (mouseRButtonDown) {
	        int x = e.getXOnScreen();
	        int y = e.getYOnScreen();
			pan  += x - prevMouseX;
			tilt -= y - prevMouseY;
			mouseRButtonDown=false;
			try {
				new Robot().mouseMove(prevMouseX, prevMouseY);
			} catch (AWTException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			mouseRButtonDown=true;
		    
			if(tilt < 1) tilt=1;
			if(tilt > 179) tilt= 179;
		}
	}
	
	
	public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        switch(kc) {
        case KeyEvent.VK_W: move_forward=1;  break;
        case KeyEvent.VK_A: move_left	=1;  break;
        case KeyEvent.VK_S: move_back	=1;  break;
        case KeyEvent.VK_D: move_right	=1;  break;
        case KeyEvent.VK_Q: move_down	=1;  break;
        case KeyEvent.VK_E: move_up		=1;  break;
        }
	}
	
	
	public void keyReleased(KeyEvent e) {
        int kc = e.getKeyCode();
        switch(kc) {
        case KeyEvent.VK_W: move_forward=0;  break;
        case KeyEvent.VK_A: move_left	=0;  break;
        case KeyEvent.VK_S: move_back	=0;  break;
        case KeyEvent.VK_D: move_right	=0;  break;
        case KeyEvent.VK_Q: move_down	=0;  break;
        case KeyEvent.VK_E: move_up		=0;  break;
        }
	}
	
	
	public void update(float dt) {
		pan += pan_dir*10*dt;
		tilt += tilt_dir*10*dt;
		
		if(tilt < 1) tilt=1;
		if(tilt > 179) tilt= 179;

		// calculate new vectors for translation based on pan/tilt angles
		forward.y = (float)Math.sin(Math.toRadians(-pan-90)) * (float)Math.cos(Math.toRadians(90-tilt));
		forward.x = (float)Math.cos(Math.toRadians(-pan-90)) * (float)Math.cos(Math.toRadians(90-tilt));
		forward.z =                                            (float)Math.sin(Math.toRadians(90-tilt));
		
		up.set(0,0,1);

		right.cross(forward, up);
		right.normalize();
		up.cross(right, forward);
		up.normalize();

		// move the camera
		Vector3f temp = new Vector3f();
		Vector3f direction = new Vector3f(0,0,0);
		float vel = 10f * dt;
		boolean changed = false;
		
		// which way do we want to move?
		float delta = 1;
		
		int move_fb = move_forward - move_back;
		if(move_fb!=0) {
			// forward/back
			temp.set(forward);
			temp.scale(delta * move_fb);
			direction.add(temp);
			changed = true;
		}
		int move_lr = move_right - move_left;
		if(move_lr!=0) {
			// strafe left/right
			temp.set(right);
			temp.scale(delta * move_lr);
			direction.add(temp);
			changed = true;
		}
		int move_ud = move_up - move_down;
		if(move_ud!=0) {
			// strafe up/down
			temp.set(up);
			temp.scale(-delta * move_ud);
			direction.add(temp);
			changed = true;
		}
		if(changed) {
			direction.normalize();
			direction.scale(vel);

			Vector3f p = getPosition();
			p.add(direction);
			setPosition(p);
		}	
	}
	
	public void render(GL2 gl2) {
		// move camera
		gl2.glRotatef(tilt, -1, 0, 0);
		gl2.glRotatef(pan,0,0,1);
		Vector3f p = getPosition();
		gl2.glTranslatef(p.x,p.y,p.z);
	}


	public Vector3f getForward() {
		return forward;
	}


	public Vector3f getUp() {
		return up;
	}


	public Vector3f getRight() {
		return right;
	}
}
