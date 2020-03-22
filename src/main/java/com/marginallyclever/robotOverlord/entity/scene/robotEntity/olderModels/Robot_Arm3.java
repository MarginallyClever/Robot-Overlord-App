package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_RTT;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotKeyframe;

/**
 * DHRobot version of Arm3, a palletizing robot I built long ago.  Incomplete!
 * @author Dan Royer
 *
 */
public class Robot_Arm3 extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -506969443681048196L;
	DHRobotEntity live;

	public Robot_Arm3() {
		super();
		setName("Arm3");
		live = new DHRobotEntity();
		live.setIKSolver(new DHIKSolver_RTT());
		setupLinks(live);
	}
	
	protected void setupLinks(DHRobotEntity robot) {
		// setup sixi2 as default.
		robot.setNumLinks(5);
		// roll
		robot.links.get(0).setD(13.44);
		robot.links.get(0).setTheta(0);
		robot.links.get(0).flags = LinkAdjust.THETA;
		robot.links.get(0).setRangeMin(-160);
		robot.links.get(0).setRangeMax(160);
		// tilt
		robot.links.get(1).setAlpha(0);
		robot.links.get(1).flags = LinkAdjust.ALPHA;
		robot.links.get(2).setRangeMin(-72);
		// tilt
		robot.links.get(2).setD(44.55);
		robot.links.get(2).setAlpha(0);
		robot.links.get(2).flags = LinkAdjust.ALPHA;
		// interim point
		robot.links.get(3).setD(40);
		robot.links.get(3).setAlpha(0);
		robot.links.get(3).flags = LinkAdjust.NONE;
		// end effector
		robot.links.get(4).flags = LinkAdjust.NONE;
/*
		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm.stl",0.1f);
			links.get(4).model = ModelFactory.createModelFromFilename("/Sixi2/hand.stl",0.1f);

			links.get(0).model.adjustOrigin(new Vector3d(0, 0, 5.150f));
			links.get(0).model.adjustRotation(new Vector3d(90,90,0));
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
	public RobotKeyframe createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
