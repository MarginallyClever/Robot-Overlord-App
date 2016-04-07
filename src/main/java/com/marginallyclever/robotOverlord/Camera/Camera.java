package com.marginallyclever.robotOverlord.Camera;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.ObjectInWorld;

import com.jogamp.opengl.GL2
;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class Camera extends ObjectInWorld {
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
	protected int move_ud=0;
	protected int move_lr=0;
	protected int move_fb=0;
	
	CameraControlPanel cameraPanel;
	
	
	public Camera() {
		super();
		
		displayName="Camera";
				
		position.set(0,40,-20);
		pan=0;
		tilt=90;
	}

	
	public ArrayList<JPanel> getControlPanels() {
		ArrayList<JPanel> list = super.getControlPanels();
		if(list==null) list = new ArrayList<JPanel>();
		
		cameraPanel = new CameraControlPanel(this);
		list.add(cameraPanel);
		
		return list;
	}
	
	
	public void mousePressed(MouseEvent e) {
        prevMouseX = e.getX();
        prevMouseY = e.getY();
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        	mouseRButtonDown = true;
        }
	}
	
	
	public void mouseReleased(MouseEvent e) {
        if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
        	mouseRButtonDown = false;
        }
	}
	
	
	public void mouseDragged(MouseEvent e) {
		if (mouseRButtonDown==true) {
	        int x = e.getX();
	        int y = e.getY();
			pan  += x - prevMouseX;
			tilt -= y - prevMouseY;
			prevMouseX = x;
			prevMouseY = y;
			if(tilt < 1) tilt=1;
			if(tilt > 179) tilt= 179;
		}
	}
	
	
	public void keyPressed(KeyEvent e) {/*
        int kc = e.getKeyCode();
        switch(kc) {
        case KeyEvent.VK_W: wDown=true;  break;
        case KeyEvent.VK_A: aDown=true;  break;
        case KeyEvent.VK_S: sDown=true;  break;
        case KeyEvent.VK_D: dDown=true;  break;
        case KeyEvent.VK_Q: qDown=true;  break;
        case KeyEvent.VK_E: eDown=true;  break;
        }*/
	}
	
	
	public void keyReleased(KeyEvent e) {/*
        int kc = e.getKeyCode();
        switch(kc) {
        case KeyEvent.VK_W: wDown=false;  break;
        case KeyEvent.VK_A: aDown=false;  break;
        case KeyEvent.VK_S: sDown=false;  break;
        case KeyEvent.VK_D: dDown=false;  break;
        case KeyEvent.VK_Q: qDown=false;  break;
        case KeyEvent.VK_E: eDown=false;  break;
        }*/
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
		
		if(move_fb!=0) {
			// forward/back
			temp.set(forward);
			temp.scale(delta * move_fb);
			direction.add(temp);
			changed = true;
		}
		if(move_lr!=0) {
			// strafe left/right
			temp.set(right);
			temp.scale(-delta * move_lr);
			direction.add(temp);
			changed = true;
		}
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
			position.add(direction);
		}	
	}
	
	public void render(GL2 gl2) {
		// move camera
		gl2.glRotatef(tilt, -1, 0, 0);
		gl2.glRotatef(pan,0,0,1);
		gl2.glTranslatef(position.x,position.y,position.z);
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
