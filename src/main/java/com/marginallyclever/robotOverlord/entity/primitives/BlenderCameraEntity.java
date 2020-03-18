package com.marginallyclever.robotOverlord.entity.primitives;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.swingInterface.InputManager;
import com.marginallyclever.robotOverlord.swingInterface.view.View;

/**
 * Blender style camera controls
 * @author Dan Royer
 * @since 1.6.0
 */
public class BlenderCameraEntity extends CameraEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected double zoom=75;

	public BlenderCameraEntity() {
		super();
		setName("Blender Camera");
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
	
	@Override
	public void getView(View view) {
		super.getView(view);
		view.addReadOnly("Zoom="+zoom);
	}
}
