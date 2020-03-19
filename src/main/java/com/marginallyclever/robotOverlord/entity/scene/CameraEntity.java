package com.marginallyclever.robotOverlord.entity.scene;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
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
public class CameraEntity extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8808107560966888107L;
	
	// orientation
	protected Vector3d forward = new Vector3d(1,0,0);
	protected Vector3d right = new Vector3d(0,1,0);
	protected Vector3d up = new Vector3d(0,0,1);
	
	// angles
	protected double pan;
	protected double tilt;
	protected double zoom=100;

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
		
		// Move the camera
		Matrix4d m = pose.get();
		
        double dz = InputManager.rawValue(InputManager.Source.MOUSE_Z);
        if(dz!=0) { 
        	double oldZoom = zoom;
        	
        	zoom -= dz*3;
        	zoom = Math.max(0.01,zoom);

        	if(oldZoom!=zoom) {
				// adjust the camera position to orbit around a point 'zoom' in front of the camera
				Vector3d oldZ = MatrixHelper.getZAxis(m);
				Vector3d newZ = new Vector3d(oldZ); 

				oldZ.scale(oldZoom);
				newZ.scale(zoom);
	
				Vector3d p = getPosition();
				p.sub(oldZ);
				p.add(newZ);
				setPosition(p);
        	}
        	//System.out.println(dz+"\t"+zoom);
        }
        
		if (InputManager.isOn(InputManager.Source.MOUSE_MIDDLE)) {
	        double dx = InputManager.rawValue(InputManager.Source.MOUSE_X);
	        double dy = InputManager.rawValue(InputManager.Source.MOUSE_Y);

			if(dx!=0 || dy!=0) {
				if( InputManager.isOn(InputManager.Source.KEY_LSHIFT) ||
					InputManager.isOn(InputManager.Source.KEY_RSHIFT) ) {
					// translate relative to camera's current orientation
					Vector3d vx = MatrixHelper.getXAxis(m);
					Vector3d vy = MatrixHelper.getYAxis(m);
					Vector3d p = getPosition();
					double zSq = Math.sqrt(zoom)*0.1;
					vx.scale(zSq*-dx);
					vy.scale(zSq* dy);
					p.add(vx);
					p.add(vy);
					setPosition(p);
					
					//System.out.println(dx+"\t"+dy+"\t"+zoom+"\t"+zSq);
				} else if(InputManager.isOn(InputManager.Source.KEY_LCONTROL) ||
						  InputManager.isOn(InputManager.Source.KEY_RCONTROL) ) {
					// up and down to fly forward and back
					Vector3d zAxis = MatrixHelper.getZAxis(m);
					zAxis.scale(dy);
					
					Vector3d p = getPosition();
					p.add(zAxis);
					setPosition(p);
				} else if(InputManager.isOn(InputManager.Source.KEY_LALT) ||
						  InputManager.isOn(InputManager.Source.KEY_RALT) ) {
					// snap system 
				} else {
					// orbit around the focal point
					setPan(getPan()+dx);
					setTilt(getTilt()-dy);

					// do updateMatrix() but keep the rotation matrix
					Matrix3d rot = buildPanTiltMatrix(pan,tilt);
					setRotation(rot);
					
					// adjust the camera position to orbit around a point 'zoom' in front of the camera
					Vector3d oldZ = MatrixHelper.getZAxis(m);
					oldZ.scale(zoom);

					Vector3d newZ = new Vector3d(rot.m02,rot.m12,rot.m22);
					newZ.scale(zoom);

					Vector3d p = getPosition();
					p.sub(oldZ);
					p.add(newZ);
					setPosition(p);
					
					//System.out.println(dx+"\t"+dy+"\t"+pan+"\t"+tilt+"\t"+oldZ+"\t"+newZ);
				}
			}
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
		view.pushStack("Ca", "Camera");
		view.addReadOnly("Pan="+pan);
		view.addReadOnly("Tilt="+tilt);
		view.addReadOnly("Zoom="+zoom);
		view.popStack();
		super.getView(view);
	}
}
