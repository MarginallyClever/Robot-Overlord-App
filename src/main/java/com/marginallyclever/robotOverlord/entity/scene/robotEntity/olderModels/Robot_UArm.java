package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_RTT;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;

/**
 * Unfinished UArm implementation of DHRobot.
 * @author Dan Royer
 * See https://buildmedia.readthedocs.org/media/pdf/uarmdocs/latest/uarmdocs.pdf
 */
public class Robot_UArm extends RobotEntity {
/**
	 * 
	 */
	private static final long serialVersionUID = -9001671002787113226L;

	/*
	private transient Model linkA1;
	private transient Model linkA2;
	private transient Model linkA3;
	private transient Model linkB1;
	private transient Model linkB2;
*/
	public transient boolean isFirstTime;
	
	DHRobotModel live;

	public Robot_UArm() {
		super();
		setName("UArm");

		live = new DHRobotModel();
		live.setIKSolver(new DHIKSolver_RTT());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotModel robot) {
		robot.setNumLinks(6);
		// roll
		robot.getLink(0).setD(2.4);
		robot.getLink(0).setR(2.0728);
		robot.getLink(0).flags = LinkAdjust.THETA;
		robot.getLink(0).setRangeMin(-160);
		robot.getLink(0).setRangeMax(160);
		// tilt
		robot.getLink(1).setD(9.5267-2.4);
		robot.getLink(1).setTheta(90);
		robot.getLink(1).flags = LinkAdjust.ALPHA;
		robot.getLink(1).setRangeMin(-72);
		// tilt
		robot.getLink(2).setD(14.8004);
		robot.getLink(2).flags = LinkAdjust.ALPHA;
		robot.getLink(2).setRangeMin(-10);
		robot.getLink(2).setRangeMax(150);
		
		// interim point
		robot.getLink(3).setD(16.0136);
		robot.getLink(3).flags = LinkAdjust.NONE;
		// end effector
		robot.getLink(4).setD(3.545);
		robot.getLink(4).setTheta(-90);
		robot.getLink(4).setR(1);
		robot.getLink(4).flags = LinkAdjust.ALPHA;

		robot.getLink(5).setR(4);
		robot.getLink(5).flags = LinkAdjust.NONE;
	}
	
	public void setupModels(DHRobotModel robot) {
		try {
			robot.getLink(0).setShapeFilename("/uArm/base.STL");
			robot.getLink(1).setShapeFilename("/uArm/shoulder.STL");
			robot.getLink(2).setShapeFilename("/uArm/bicep.STL");
			robot.getLink(3).setShapeFilename("/uArm/forearm.STL");
			robot.getLink(4).setShapeFilename("/uArm/wrist.STL");
			robot.getLink(5).setShapeFilename("/uArm/hand.STL");	
			
			robot.getLink(0).setShapeOrigin(new Vector3d(0,0,1.65f));
			robot.getLink(1).setShapeOrigin(new Vector3d(-2.0728f,0,1.65f-2.4f));
			robot.getLink(1).setShapeRotation(new Vector3d(0,0,-180));
			robot.getLink(2).setShapeOrigin(new Vector3d(-0.25f,0,1.65f));
			robot.getLink(2).setShapeRotation(new Vector3d(0,0,90));
			robot.getLink(3).setShapeOrigin(new Vector3d(-0.25f,0,0));//z23.511,x27.727
			robot.getLink(3).setShapeRotation(new Vector3d(0,0,90));
			robot.getLink(4).setShapeOrigin(new Vector3d(-0.25f,0,0));
			robot.getLink(4).setShapeRotation(new Vector3d(-90,0,90));
			robot.getLink(5).setShapeRotation(new Vector3d(0,-90,90));
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
		live.getLink(2).setRangeMin(20);
		live.getLink(2).setRangeMax(165);
		
		// TODO calculate me in the solver?
		live.getLink(3).setAlpha(
				90
				-live.getLink(1).getAlpha()
				-live.getLink(2).getAlpha()
				);
		
		live.refreshDHMatrixes();
		
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
	public Memento createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
