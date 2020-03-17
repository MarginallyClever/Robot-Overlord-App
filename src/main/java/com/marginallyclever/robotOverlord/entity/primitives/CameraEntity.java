package com.marginallyclever.robotOverlord.entity.primitives;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;
import com.marginallyclever.robotOverlord.swingInterface.view.View;
import com.jogamp.opengl.GL2;

/**
 * Camera in the world.  Has no physical presence.  Has location and direction.
 * @author Dan Royer
 */
public class CameraEntity extends PhysicalEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8808107560966888107L;
	// orientation
	protected Vector3d forward = new Vector3d(1,0,0);
	protected Vector3d right = new Vector3d(0,1,0);
	protected Vector3d up = new Vector3d(0,0,1);
	
	// angles
	protected double pan, tilt;

	public CameraEntity() {
		super();
		
		setName("Camera");
	}
	
	protected Matrix3d buildPanTiltMatrix(double panDeg,double tiltDeg) {
		Matrix3d a = new Matrix3d();
		Matrix3d b = new Matrix3d();
		Matrix3d c = new Matrix3d();
		a.rotZ(Math.toRadians(panDeg));
		b.rotX(Math.toRadians(-tiltDeg));
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
		
		return c;
	}
	
	protected void updateMatrix() {
		setRotation(buildPanTiltMatrix(pan,tilt));
	}

	@Override
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
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose.get());
			PrimitiveSolids.drawStar(gl2, 10);
		gl2.glPopMatrix();
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
	
	@Override
	public void getView(View view) {
		super.getView(view);
		view.addReadOnly("Pan="+pan);
		view.addReadOnly("Tilt="+tilt);
	}
}
