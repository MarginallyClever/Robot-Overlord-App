package com.marginallyclever.robotOverlord.entity.scene;

import java.util.Observable;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
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
	protected DoubleEntity pan = new DoubleEntity("Pan",0);
	protected DoubleEntity tilt = new DoubleEntity("Tilt",0);
	protected DoubleEntity zoom = new DoubleEntity("Zoom",100);

	// snap system
	protected DoubleEntity snapDeadZone = new DoubleEntity("Snap dead zone",100);
	protected DoubleEntity snapDegrees = new DoubleEntity("Snap degrees",45);
	protected boolean hasSnappingStarted=false;
	protected double sumDx;
	protected double sumDy;
	
	
	public CameraEntity() {
		super();
		setName("Camera");
		
		addChild(snapDeadZone);
		addChild(snapDegrees);
		
		pan.addObserver(this);
		tilt.addObserver(this);
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
	
	@Override
	public void update(Observable o, Object arg) {
		setRotation(buildPanTiltMatrix(pan.get(),tilt.get()));
		super.update(o, arg);
	}

	@Override
	public void update(double dt) {
		// Move the camera
		Matrix4d m = pose.get();
		
        double dz = InputManager.getRawValue(InputManager.Source.MOUSE_Z);
        if(dz!=0) { 
        	double oldZoom = zoom.get();
        	double newZoom = oldZoom;
        	
        	newZoom -= dz*3;
        	newZoom = Math.max(1,newZoom);

        	if(oldZoom!=newZoom) {
        		zoom.set(newZoom);
				// adjust the camera position to orbit around a point 'zoom' in front of the camera
				Vector3d oldZ = MatrixHelper.getZAxis(m);
				Vector3d newZ = new Vector3d(oldZ); 

				oldZ.scale(oldZoom);
				newZ.scale(zoom.get());
	
				Vector3d p = getPosition();
				p.sub(oldZ);
				p.add(newZ);
				setPosition(p);
        	}
        	//System.out.println(dz+"\t"+zoom);
        }
        
		if (InputManager.isOn(InputManager.Source.MOUSE_MIDDLE)) {
	        double dx = InputManager.getRawValue(InputManager.Source.MOUSE_X);
	        double dy = InputManager.getRawValue(InputManager.Source.MOUSE_Y);

			if(dx!=0 || dy!=0) {
				// snap system
		        boolean isSnapHappeningNow=
		        		(InputManager.isOn(InputManager.Source.KEY_LALT) || InputManager.isOn(InputManager.Source.KEY_RALT));
		        if(isSnapHappeningNow) {
		        	if(!hasSnappingStarted) {
						sumDx=0;
						sumDy=0;
						hasSnappingStarted=true;
					}
				}
		        hasSnappingStarted = isSnapHappeningNow;
		        //System.out.println("Snap="+isSnapHappeningNow);
				
		        //
				if( InputManager.isOn(InputManager.Source.KEY_LSHIFT) ||
					InputManager.isOn(InputManager.Source.KEY_RSHIFT) ) {
					// translate relative to camera's current orientation
					Vector3d vx = MatrixHelper.getXAxis(m);
					Vector3d vy = MatrixHelper.getYAxis(m);
					Vector3d p = getPosition();
					double zSq = Math.sqrt(zoom.get())*0.01;
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
				} else if( isSnapHappeningNow ) {
					sumDx+=dx;
					sumDy+=dy;
					if(Math.abs(sumDx)>snapDeadZone.get() || Math.abs(sumDy)>snapDeadZone.get()) {
						double degrees = snapDegrees.get();
						if(Math.abs(sumDx) > Math.abs(sumDy)) {
							double a=getPan();
							if(sumDx>0)	a+=degrees;	// snap CCW
							else		a-=degrees;	// snap CW
							setPan(Math.round(a/degrees)*degrees);
						} else {
							double a=getTilt();
							if(sumDy>0)	a-=degrees;	// snap down
							else		a+=degrees;	// snap up
							setTilt(Math.round(a/degrees)*degrees);
						}
						
						Matrix3d rot = buildPanTiltMatrix(pan.get(),tilt.get());
						setRotation(rot);
						sumDx=0;
						sumDy=0;
					}
				} else {
					// orbit around the focal point
					setPan(getPan()+dx);
					setTilt(getTilt()-dy);

					// do updateMatrix() but keep the rotation matrix
					Matrix3d rot = buildPanTiltMatrix(pan.get(),tilt.get());
					setRotation(rot);
					
					// adjust the camera position to orbit around a point 'zoom' in front of the camera
					Vector3d oldZ = MatrixHelper.getZAxis(m);
					oldZ.scale(zoom.get());

					Vector3d newZ = new Vector3d(rot.m02,rot.m12,rot.m22);
					newZ.scale(zoom.get());

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
		return pan.get();
	}
	
	public double getTilt() {
		return tilt.get();
	}
	
	public void setPan(double arg0) {
		//arg0 = Math.min(Math.max(arg0, 0), 360);
		pan.set(arg0);
	}
	
	public void setTilt(double arg0) {
		arg0 = Math.min(Math.max(arg0, 1), 179);
		tilt.set(arg0);
	}
	
	public void setZoom(double arg0) {
		arg0 = Math.min(Math.max(arg0, 0), 500);
		zoom.set(arg0);
	}
	
	public double getZoom() {
		return zoom.get();
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Ca", "Camera");
		view.add(snapDeadZone);
		view.add(snapDegrees);
		view.add(pan).setReadOnly(true);
		view.add(tilt).setReadOnly(true);
		view.add(zoom).setReadOnly(true);
		view.popStack();
		super.getView(view);
	}
}
