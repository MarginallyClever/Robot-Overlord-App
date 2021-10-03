package com.marginallyclever.robotOverlord.robots;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.dhRobotEntity.solvers.DHIKSolver_RTT;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;

/**
 * DHRobot version of Arm3, a palletizing robot I built long ago.  Incomplete!
 * @author Dan Royer
 *
 */
@Deprecated
public class Robot_Arm3 extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2954608403808864189L;
	DHRobotModel live;

	public Robot_Arm3() {
		super();
		setName("Arm3");
		live = new DHRobotModel();
		live.setIKSolver(new DHIKSolver_RTT());
		setupLinks(live);
	}
	
	protected void setupLinks(DHRobotModel robot) {
		// setup sixi2 as default.
		robot.setNumLinks(5);
		// roll
		robot.getLink(0).setD(13.44);
		robot.getLink(0).setTheta(0);
		robot.getLink(0).flags = LinkAdjust.THETA;
		robot.getLink(0).setRangeMin(-160);
		robot.getLink(0).setRangeMax(160);
		// tilt
		robot.getLink(1).setAlpha(0);
		robot.getLink(1).flags = LinkAdjust.ALPHA;
		robot.getLink(2).setRangeMin(-72);
		// tilt
		robot.getLink(2).setD(44.55);
		robot.getLink(2).setAlpha(0);
		robot.getLink(2).flags = LinkAdjust.ALPHA;
		// interim point
		robot.getLink(3).setD(40);
		robot.getLink(3).setAlpha(0);
		robot.getLink(3).flags = LinkAdjust.NONE;
		// end effector
		robot.getLink(4).flags = LinkAdjust.NONE;
/*
		try {
			getLink(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor.stl",0.1f);
			getLink(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder.stl",0.1f);
			getLink(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep.stl",0.1f);
			getLink(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm.stl",0.1f);
			getLink(4).model = ModelFactory.createModelFromFilename("/Sixi2/hand.stl",0.1f);

			getLink(0).model.adjustOrigin(new Vector3d(0, 0, 5.150f));
			getLink(0).model.adjustRotation(new Vector3d(90,90,0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.getPose());

			// Draw models
			float g=1;
			float r=217f/255f;
			float b=33f/255f;
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
