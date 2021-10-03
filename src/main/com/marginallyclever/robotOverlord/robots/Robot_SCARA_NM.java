package com.marginallyclever.robotOverlord.robots;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

/**
 * FANUC cylindrical coordinate robot GMF M-100
 * @author Dan Royer
 *
 */
@Deprecated
public class Robot_SCARA_NM extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4687585577640559775L;
	public transient boolean isFirstTime;
	protected DHRobotModel live;
	
	public Robot_SCARA_NM() {
		super();
		setName("SCARA NM");

		live = new DHRobotModel();
		//live.setIKSolver(new DHIKSolver_SCARA());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotModel robot) {
		robot.setNumLinks(5);

		// roll
		robot.getLink(0).setD(13.784);
		robot.getLink(0).setR(15);
		robot.getLink(0).flags = LinkAdjust.THETA;
		robot.getLink(0).setRangeMin(-40);
		robot.getLink(0).setRangeMax(240);
		
		// roll
		robot.getLink(1).setR(13.0);
		robot.getLink(1).flags = LinkAdjust.THETA;		
		robot.getLink(1).setRangeMin(-120);
		robot.getLink(1).setRangeMax(120);
		// slide
		robot.getLink(2).setD(-8);
		robot.getLink(2).flags = LinkAdjust.D;
		live.getLink(2).setRangeMax(-10.92600+7.574);
		live.getLink(2).setRangeMin(-10.92600-0.5);//-18.5+7.574;
		// roll
		robot.getLink(3).flags = LinkAdjust.THETA;
		robot.getLink(3).setRangeMin(-180);
		robot.getLink(3).setRangeMax(180);

		robot.getLink(4).flags = LinkAdjust.NONE;
		robot.getLink(4).setRangeMin(0);
		robot.getLink(4).setRangeMax(0);
		
		robot.refreshDHMatrixes();
	}

	public void setupModels(DHRobotModel robot) {
		try {
			robot.getLink(0).setShapeFilename("/SCARA_NM/Scara_base.stl");
			robot.getLink(1).setShapeFilename("/SCARA_NM/Scara_arm1.stl");
			robot.getLink(2).setShapeFilename("/SCARA_NM/Scara_arm2.stl");
			robot.getLink(4).setShapeFilename("/SCARA_NM/Scara_screw.stl");
			
			robot.getLink(0).setShapeOrigin(new Vector3d(-8,0,0));
			robot.getLink(1).setShapeOrigin(new Vector3d(-15,8,-13.784));
			robot.getLink(1).setShapeRotation(new Vector3d(0,0,-90));

			robot.getLink(2).setShapeOrigin(new Vector3d(-13,8,-13.784));
			robot.getLink(2).setShapeRotation(new Vector3d(0,0,-90));

			robot.getLink(4).setShapeOrigin(new Vector3d(-8,0,-13.784));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels(live);
		}
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, myPose);
			
			// Draw models
			float r=0.5f;
			float g=0.5f;
			float b=0.5f;
			MaterialEntity mat = new MaterialEntity();
			mat.setDiffuseColor(r,g,b,1);
			mat.render(gl2);
			live.render(gl2);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}

	@Override
	public Memento createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
