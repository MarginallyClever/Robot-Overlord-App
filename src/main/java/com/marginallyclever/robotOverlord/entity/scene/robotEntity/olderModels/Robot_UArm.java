package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_RTT;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotKeyframe;

/**
 * Unfinished UArm implementation of DHRobot.
 * @author Dan Royer
 * See https://buildmedia.readthedocs.org/media/pdf/uarmdocs/latest/uarmdocs.pdf
 */
public class Robot_UArm extends RobotEntity {
/**
	 * 
	 */
	private static final long serialVersionUID = 7937797399289936308L;

	/*
	private transient Model linkA1;
	private transient Model linkA2;
	private transient Model linkA3;
	private transient Model linkB1;
	private transient Model linkB2;
*/
	public transient boolean isFirstTime;
	
	DHRobotEntity live;

	public Robot_UArm() {
		super();
		setName("UArm");

		live = new DHRobotEntity();
		live.setIKSolver(new DHIKSolver_RTT());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotEntity robot) {
		robot.setNumLinks(6);
		// roll
		robot.links.get(0).setD(2.4);
		robot.links.get(0).setR(2.0728);
		robot.links.get(0).flags = LinkAdjust.THETA;
		robot.links.get(0).setRangeMin(-160);
		robot.links.get(0).setRangeMax(160);
		// tilt
		robot.links.get(1).setD(9.5267-2.4);
		robot.links.get(1).setTheta(90);
		robot.links.get(1).flags = LinkAdjust.ALPHA;
		robot.links.get(1).setRangeMin(-72);
		// tilt
		robot.links.get(2).setD(14.8004);
		robot.links.get(2).flags = LinkAdjust.ALPHA;
		robot.links.get(2).setRangeMin(-10);
		robot.links.get(2).setRangeMax(150);
		
		// interim point
		robot.links.get(3).setD(16.0136);
		robot.links.get(3).flags = LinkAdjust.NONE;
		// end effector
		robot.links.get(4).setD(3.545);
		robot.links.get(4).setTheta(-90);
		robot.links.get(4).setR(1);
		robot.links.get(4).flags = LinkAdjust.ALPHA;

		robot.links.get(5).setR(4);
		robot.links.get(5).flags = LinkAdjust.NONE;
	}
	
	public void setupModels(DHRobotEntity robot) {
		try {
			robot.links.get(0).setModelFilename("/uArm/base.STL");
			robot.links.get(1).setModelFilename("/uArm/shoulder.STL");
			robot.links.get(2).setModelFilename("/uArm/bicep.STL");
			robot.links.get(3).setModelFilename("/uArm/forearm.STL");
			robot.links.get(4).setModelFilename("/uArm/wrist.STL");
			robot.links.get(5).setModelFilename("/uArm/hand.STL");	
			
			robot.links.get(0).getModel().adjustOrigin(new Vector3d(0,0,1.65f));
			robot.links.get(1).getModel().adjustOrigin(new Vector3d(-2.0728f,0,1.65f-2.4f));
			robot.links.get(1).getModel().adjustRotation(new Vector3d(0,0,-180));
			robot.links.get(2).getModel().adjustOrigin(new Vector3d(-0.25f,0,1.65f));
			robot.links.get(2).getModel().adjustRotation(new Vector3d(0,0,90));
			robot.links.get(3).getModel().adjustOrigin(new Vector3d(-0.25f,0,0));//z23.511,x27.727
			robot.links.get(3).getModel().adjustRotation(new Vector3d(0,0,90));
			robot.links.get(4).getModel().adjustOrigin(new Vector3d(-0.25f,0,0));
			robot.links.get(4).getModel().adjustRotation(new Vector3d(-90,0,90));
			robot.links.get(5).getModel().adjustRotation(new Vector3d(0,-90,90));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels(live);
		}
		live.links.get(2).setRangeMin(20);
		live.links.get(2).setRangeMax(165);
		
		// TODO calculate me in the solver?
		live.links.get(3).setAlpha(
				90
				-live.links.get(1).getAlpha()
				-live.links.get(2).getAlpha()
				);
		
		live.refreshPose();
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
			
			// Draw models
			MaterialEntity mat = new MaterialEntity();
			mat.setDiffuseColor(
					0.75f*247.0f/255.0f,
					0.75f*233.0f/255.0f,
					0.75f*215.0f/255.0f, 1);
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
