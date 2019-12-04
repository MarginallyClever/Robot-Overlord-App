package com.marginallyclever.robotOverlord.camera;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.InputManager;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.physicalObject.PhysicalObject;
import com.jogamp.opengl.GL2;

import java.util.ArrayList;

/**
 * Camera in the world.  Has no physical presence.  Has location and direction.
 * TODO confirm the calculated pose matches the forward/up/right values
 * @author Dan Royer
 */
public class Camera extends PhysicalObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7511310951758205827L;
	
	// orientation
	protected Vector3d forward = new Vector3d(1,0,0);
	protected Vector3d left = new Vector3d(0,1,0);
	protected Vector3d up = new Vector3d(0,0,1);
	
	// angles
	protected float pan, tilt;
	
	protected CameraMount mount;
	
	protected int canvasWidth, canvasHeight;
	
	CameraControlPanel cameraPanel;
	
	
	public Camera() {
		super();
		
		setDisplayName("Camera");
	}

	
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		cameraPanel = new CameraControlPanel(gui,this);
		list.add(cameraPanel);
		
		return list;
	}
	
	
	public int getCanvasWidth() {
		return canvasWidth;
	}


	public void setCanvasWidth(int canvasWidth) {
		this.canvasWidth = canvasWidth;
	}


	public int getCanvasHeight() {
		return canvasHeight;
	}


	public void setCanvasHeight(int canvasHeight) {
		this.canvasHeight = canvasHeight;
	}
	
	private void updateMatrix() {
		Matrix3d a = new Matrix3d();
		Matrix3d b = new Matrix3d();
		Matrix3d c = new Matrix3d();
		a.rotZ(Math.toRadians(pan));
		b.rotX(Math.toRadians(-tilt));
		c.mul(b,a);

		left.x=-c.m00;
		left.y=-c.m01;
		left.z=-c.m02;

		up.x=c.m10;
		up.y=c.m11;
		up.z=c.m12;
		
		forward.x=c.m20;
		forward.y=c.m21;
		forward.z=c.m22;
		
		c.transpose();
		setRotation(c);
	}
	
	public void update(double dt) {
		updateMatrix();
		
		// move the camera
		Vector3d temp = new Vector3d();
		Vector3d direction = new Vector3d(0,0,0);
		double vel = 20.0 * dt;
		boolean changed = false;

		int runSpeed = 1;//(move_run==1)?3:1;

		// pan/tilt
		if (InputManager.isOn(InputManager.MOUSE_RIGHT)) {
	        int dx = (int)(InputManager.rawValue(InputManager.MOUSE_X)*0.5);
	        int dy = (int)(InputManager.rawValue(InputManager.MOUSE_Y)*0.5);
			setPan(getPan()+dx);
			setTilt(getTilt()-dy);

			updateMatrix();
		}

		// linear moves
		double move_fb = (InputManager.rawValue(InputManager.KEY_S)-InputManager.rawValue(InputManager.KEY_W));
		if(move_fb!=0) {
			// forward/back
			temp.set(forward);
			temp.scale(move_fb*runSpeed);
			direction.add(temp);
			changed = true;
		}
		double move_lr = InputManager.rawValue(InputManager.KEY_A)-InputManager.rawValue(InputManager.KEY_D);
		if(move_lr!=0) {
			// strafe left/right
			temp.set(left);
			temp.scale(move_lr*runSpeed);
			direction.add(temp);
			changed = true;
		}
		double move_ud = InputManager.rawValue(InputManager.KEY_E)-InputManager.rawValue(InputManager.KEY_Q);
		if(move_ud!=0) {
			// strafe up/down
			temp.set(up);
			temp.scale(move_ud*runSpeed);
			direction.add(temp);
			changed = true;
		}
		
		if(changed) {
			runSpeed=3;
			//direction.normalize();
			direction.scale(vel*runSpeed);

			Vector3d p = getPosition();
			p.add(direction);
			setPosition(p);
		}	
	}
	
	// OpenGL camera: -Z=forward, +X=right, +Y=up
	public void render(GL2 gl2) {
		Vector3d p = getPosition();
		
		Matrix4d c = new Matrix4d(matrix);
		c.setTranslation(new Vector3d(0,0,0));
/*
		Matrix4d a = new Matrix4d();
		Matrix4d b = new Matrix4d();
		Matrix4d c = new Matrix4d();
		a.rotZ(Math.toRadians(pan));
		b.rotX(Math.toRadians(-tilt));
		c.mul(b,a);
		c.transpose();*/
		
		Matrix4d mFinal = c;
		mFinal.setTranslation(p);
		mFinal.invert();
		MatrixHelper.applyMatrix(gl2, mFinal);
		
		//c.m03=0;
		//c.m13=0;
		//c.m23=0;
		//MatrixHelper.drawMatrix(gl2, c, 4);
	}


	public Vector3d getForward() {
		return forward;
	}


	public Vector3d getUp() {
		return up;
	}


	public Vector3d getRight() {
		return left;
	}
	
	public float getPan() {
		return pan;
	}
	
	public float getTilt() {
		return tilt;
	}
	
	public void setPan(float arg0) {
		pan=arg0;
	}
	
	public void setTilt(float arg0) {
		tilt=arg0;
	    
		if(tilt < 1) tilt=1;
		if(tilt > 179) tilt= 179;
	}
}
