package com.marginallyclever.robotoverlord.robots.stewartplatform.rotary;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.swing.ViewPanelFactory;

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
	public void render(GL3 gl) {
		PoseComponent myPose = getEntity().getComponent(PoseComponent.class);

		gl.glPushMatrix();
			MatrixHelper.applyMatrix(gl, myPose.getLocal());

			baseModel.render(gl);
			
			// draw the end effector
			gl.glPushMatrix();
			MatrixHelper.applyMatrix(gl, getEndEffectorPose());
			eeModel.render(gl);
			gl.glPopMatrix();

			drawBiceps(gl);
			drawForearms(gl);
		gl.glPopMatrix();

		super.render(gl);
	}

	@Override
	protected void drawBiceps(GL3 gl) {
		for(int i=0;i<arms.length;++i) {
			int k = (i+arms.length-1)%arms.length;
			double j = (k/2)+1;

			gl.glPushMatrix();
				gl.glTranslated(arms[i].pShoulder.x,arms[i].pShoulder.y, arms[i].pShoulder.z);
				gl.glRotated(j*120, 0, 0, 1);
				gl.glRotated(-90, 0, 1, 0);
				gl.glRotated(-arms[i].angle, 0, 0, 1);
				armModel.render(gl);
			gl.glPopMatrix();
		}
	}

	@Override
	public void getView(ViewPanelFactory view) {
		super.getView(view);
	}
}
