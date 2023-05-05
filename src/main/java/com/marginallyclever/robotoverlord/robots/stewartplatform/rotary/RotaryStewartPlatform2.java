package com.marginallyclever.robotoverlord.robots.stewartplatform.rotary;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

import java.io.Serial;

/**
 * a rotary stewart platform is a 6DOF robot that can move in X, Y, Z, and rotate around X, Y, Z.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Deprecated
public class RotaryStewartPlatform2 extends RotaryStewartPlatform {
	@Serial
	private static final long serialVersionUID = 1L;

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

	@Override
	public void render(GL2 gl2) {
		PoseComponent myPose = getEntity().getComponent(PoseComponent.class);

		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, myPose.getLocal());

			baseModel.render(gl2);
			
			// draw the end effector
			gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, getEndEffectorPose());
			eeModel.render(gl2);
			gl2.glPopMatrix();

			drawBiceps(gl2);
			drawForearms(gl2);
		gl2.glPopMatrix();

		super.render(gl2);
	}

	@Override
	protected void drawBiceps(GL2 gl2) {
		for(int i=0;i<arms.length;++i) {
			int k = (i+arms.length-1)%arms.length;
			double j = (k/2)+1;

			gl2.glPushMatrix();
				gl2.glTranslated(arms[i].pShoulder.x,arms[i].pShoulder.y, arms[i].pShoulder.z);
				gl2.glRotated(j*120, 0, 0, 1);
				gl2.glRotated(-90, 0, 1, 0);
				gl2.glRotated(-arms[i].angle, 0, 0, 1);
				armModel.render(gl2);
			gl2.glPopMatrix();
		}
	}

	@Override
	public void getView(ComponentPanelFactory view) {
		super.getView(view);
	}
}
