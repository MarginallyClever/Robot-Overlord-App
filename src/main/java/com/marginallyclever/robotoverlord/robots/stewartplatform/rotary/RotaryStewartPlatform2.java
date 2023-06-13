package com.marginallyclever.robotoverlord.robots.stewartplatform.rotary;

import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;

/**
 * a rotary stewart platform is a 6DOF robot that can move in X, Y, Z, and rotate around X, Y, Z.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Deprecated
public class RotaryStewartPlatform2 extends RotaryStewartPlatform {
	public final String hello = "HELLO WORLD! I AM STEWART PLATFORM V4.2";
	// machine dimensions
	private final MeshFromFile baseModel;
	private final MeshFromFile eeModel;
	private final MeshFromFile armModel;

	public RotaryStewartPlatform2() {
		super();
		
		// load models and fix scale/orientation.
		baseModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/rotary/base.stl");
		//baseModel.setShapeScale(0.1);
		eeModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/rotary/endEffector.stl");
		//eeModel.setShapeScale(0.1);
		//eeModel.setShapeRotation(new Vector3d(0,0,-30));
		armModel = new MeshFromFile("/com/marginallyclever/robotoverlord/robots/stewartplatform/rotary/arm.stl");
		//armModel.setShapeScale(0.1);

		//eeModel.setShapeRotation(180,0,30);
		//baseModel.setShapeRotation(0,90,90);
		//baseModel.setShapeOrigin(0,0,BASE_Z.get() + 0.6);
	}
}
