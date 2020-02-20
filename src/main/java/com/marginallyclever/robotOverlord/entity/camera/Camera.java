package com.marginallyclever.robotOverlord.entity.camera;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.physicalObject.PhysicalObject;
import com.marginallyclever.robotOverlord.uiElements.InputManager;
import com.jogamp.opengl.GL2;

import java.util.ArrayList;

/**
 * Camera in the world.  Has no physical presence.  Has location and direction.
 * TODO confirm the calculated pose matches the forward/up/right values
 * @author Dan Royer
 */
public class Camera extends PhysicalObject {
	// orientation
	protected Vector3d forward = new Vector3d(1,0,0);
	protected Vector3d right = new Vector3d(0,1,0);
	protected Vector3d up = new Vector3d(0,0,1);
	
	// angles
	protected double pan, tilt;
	
	protected CameraMount mount;
	
	protected int canvasWidth, canvasHeight;
	protected double minZ, maxZ,fov;
	protected int cursorX,cursorY;
	protected boolean isPressed;
	
	CameraControlPanel cameraPanel;

	public Camera() {
		super();
		
		setDisplayName("Camera");
		
		isPressed=false;
		fov=60;
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

		right.x=c.m00;
		right.y=c.m01;
		right.z=c.m02;

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
		if (InputManager.isOn(InputManager.Source.MOUSE_RIGHT)) {
	        double dx = InputManager.rawValue(InputManager.Source.MOUSE_X);
	        double dy = InputManager.rawValue(InputManager.Source.MOUSE_Y);
	        if(dx!=0 || dy!=0) {
				setPan(getPan()+dx*0.5);
				setTilt(getTilt()-dy*0.5);
	        }
			updateMatrix();
		}


		// linear moves
		double move_fb = InputManager.rawValue(InputManager.Source.KEY_S)-InputManager.rawValue(InputManager.Source.KEY_W);
		double move_lr = InputManager.rawValue(InputManager.Source.KEY_D)-InputManager.rawValue(InputManager.Source.KEY_A);
		double move_ud = InputManager.rawValue(InputManager.Source.KEY_E)-InputManager.rawValue(InputManager.Source.KEY_Q);
		// middle mouse click + drag to slide
		if(InputManager.isOn(InputManager.Source.MOUSE_MIDDLE)) {
			double dx = InputManager.rawValue(InputManager.Source.MOUSE_X);
			double dy = InputManager.rawValue(InputManager.Source.MOUSE_Y);
			move_lr-=dx*0.25;
			move_ud+=dy*0.25;
		}
		

		if(move_fb!=0) {
			// forward/back
			temp.set(forward);
			temp.scale(move_fb);
			direction.add(temp);
			changed = true;
		}
		if(move_lr!=0) {
			// strafe left/right
			temp.set(right);
			temp.scale(move_lr);
			direction.add(temp);
			changed = true;
		}
		if(move_ud!=0) {
			// strafe up/down
			temp.set(up);
			temp.scale(move_ud);
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

		Matrix4d mFinal = c;
		mFinal.setTranslation(p);
		mFinal.invert();
		MatrixHelper.applyMatrix(gl2, mFinal);
	}


	public Vector3d getForward() {
		return forward;
	}


	public Vector3d getUp() {
		return up;
	}


	public Vector3d getRight() {
		return right;
	}
	
	public double getPan() {
		return pan;
	}
	
	public double getTilt() {
		return tilt;
	}
	
	public void setPan(double arg0) {
		pan=arg0;
	}
	
	public void setTilt(double arg0) {
		tilt=arg0;
	    
		if(tilt < 1) tilt=1;
		if(tilt > 179) tilt= 179;
	}
	
	// reach out from the camera into the world and find the nearest object (if any) that the ray intersects.
	public Vector3d rayPick() {		
		Vector3d vy = new Vector3d();
		vy.set(up);
		vy.scale(cursorY);

		Vector3d vx = new Vector3d();
		vx.set(right);
		vx.scale(+cursorX);
		
		Vector3d pickRay = new Vector3d(forward);
		pickRay.scale(-canvasHeight*Math.sin(Math.toRadians(fov)));
		pickRay.add(vx);
		pickRay.add(vy);
		pickRay.normalize();

		return pickRay;
	}


	public void setMinZ(double d) {
		minZ=d;
	}


	public void setMaxZ(double d) {
		maxZ=d;
	}


	public void setFOV(double d) {
		fov=d;
	}


	public void setCursor(int x,int y) {
		cursorX= x - canvasWidth/2;
		cursorY= canvasHeight/2 - y;
        //System.out.println("X"+cursorX+" Y"+cursorY);
	}


	public void pressed() {
		isPressed=true;
	}


	public void released() {
		isPressed=false;
	}
	
	public boolean isPressed() {
		return isPressed;
	}
}
