package com.marginallyclever.robotOverlord.robots.stewartplatform;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.RemoteEntity;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.io.Serial;

public class RotaryStewartPlatform2 extends RotaryStewartPlatform {
	@Serial
	private static final long serialVersionUID = 1L;

	public final String hello = "HELLO WORLD! I AM STEWART PLATFORM V4.2";
	// machine dimensions
	private final Shape baseModel;
	private final Shape eeModel;
	private final Shape armModel;

	public RotaryStewartPlatform2() {
		super("Rotary Stewart Platform 2");
		
		// load models and fix scale/orientation.
		baseModel = new Shape("Base","/rotaryStewartPlatform/base.stl");
		baseModel.setShapeScale(0.1);
		eeModel = new Shape("ee","/rotaryStewartPlatform/endEffector.stl");
		eeModel.setShapeScale(0.1);
		eeModel.setShapeRotation(new Vector3d(0,0,-30));
		armModel = new Shape("arm","/rotaryStewartPlatform/arm.stl");
		armModel.setShapeScale(0.1);

		eeModel.setShapeRotation(180,0,30);
		baseModel.setShapeRotation(0,90,90);
		baseModel.setShapeOrigin(0,0,BASE_Z.get() + 0.6);
	}

	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, myPose);

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
	public void getView(ViewPanel view) {
		super.getView(view);

		baseModel.getMaterial().getView(view);
		eeModel.getMaterial().getView(view);
		armModel.getMaterial().getView(view);
	}
}
