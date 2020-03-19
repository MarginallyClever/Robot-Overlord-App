package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.solvers.DHIKSolver_RTTRTR;


public class Robot_Thor extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4804145940106986459L;
	public transient boolean isFirstTime;
	public MaterialEntity material;
	protected DHRobotEntity live;
	
	public Robot_Thor() {
		super();
		setName("Thor");

		live = new DHRobotEntity();
		live.setIKSolver(new DHIKSolver_RTTRTR());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotEntity robot) {
		robot.setNumLinks(8);

		// roll
		robot.links.get(0).setD(20.2);
		robot.links.get(0).flags = LinkAdjust.THETA;
		robot.links.get(0).setRangeMin(-120);
		robot.links.get(0).setRangeMax(120);
		// tilt
		robot.links.get(1).flags = LinkAdjust.ALPHA;
		robot.links.get(1).setRangeMin(-72);
		// tilt
		robot.links.get(2).setD(16.0);
		robot.links.get(2).flags = LinkAdjust.ALPHA;
		robot.links.get(2).setRangeMin(-83.369);
		robot.links.get(2).setRangeMax(86);
		// interim point
		robot.links.get(3).setD(0.001);
		robot.links.get(3).setAlpha(90);
		robot.links.get(3).flags = LinkAdjust.NONE;
		// roll
		robot.links.get(4).setD(1.4);
		robot.links.get(4).flags = LinkAdjust.THETA;
		robot.links.get(4).setRangeMin(-90);
		robot.links.get(4).setRangeMax(90);

		// tilt
		robot.links.get(5).setD(18.1);
		robot.links.get(5).flags = LinkAdjust.ALPHA;
		robot.links.get(5).setRangeMin(-90);
		robot.links.get(5).setRangeMax(90);
		// roll
		robot.links.get(6).setD(5.35);
		robot.links.get(6).flags = LinkAdjust.THETA;
		robot.links.get(6).setRangeMin(-90);
		robot.links.get(6).setRangeMax(90);
		
		robot.links.get(7).flags = LinkAdjust.NONE;
		
		robot.refreshPose();
	}
	
	public void setupModels(DHRobotEntity robot) {
		material = new MaterialEntity();
		float r=1;
		float g=0f/255f;
		float b=0f/255f;
		material.setDiffuseColor(r,g,b,1);
		
		try {
			robot.links.get(0).setModelFilename("/Thor/Thor0.stl");
			robot.links.get(1).setModelFilename("/Thor/Thor1.stl");
			robot.links.get(2).setModelFilename("/Thor/Thor2.stl");
			robot.links.get(3).setModelFilename("/Thor/Thor3.stl");
			robot.links.get(5).setModelFilename("/Thor/Thor4.stl");
			robot.links.get(6).setModelFilename("/Thor/Thor5.stl");
			robot.links.get(7).setModelFilename("/Thor/Thor6.stl");

			robot.links.get(1).getModel().adjustOrigin(new Vector3d(0,0,-15.35));
			robot.links.get(1).getModel().adjustRotation(new Vector3d(0,0,90));
			robot.links.get(2).getModel().adjustOrigin(new Vector3d(0,0,-6.5));
			robot.links.get(2).getModel().adjustRotation(new Vector3d(0,0,90));
			robot.links.get(3).getModel().adjustRotation(new Vector3d(90,0,90));
			robot.links.get(3).getModel().adjustOrigin(new Vector3d(0,6,0));
			robot.links.get(5).getModel().adjustOrigin(new Vector3d(0,0,0));
			robot.links.get(5).getModel().adjustRotation(new Vector3d(0,0,90));
			robot.links.get(6).getModel().adjustOrigin(new Vector3d(0,0,-4.75));
			robot.links.get(6).getModel().adjustRotation(new Vector3d(0,0,90));
			robot.links.get(7).getModel().adjustOrigin(new Vector3d(0,0,0));
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
			material.render(gl2);
			MatrixHelper.applyMatrix(gl2, this.pose.get());
			live.render(gl2);		
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	/*
	@Override
	public void sendNewStateToRobot(DHKeyframe keyframe) {
		// If the wiring on the robot is reversed, these parameters must also be reversed.
		// This is a software solution to a hardware problem.
		final double SCALE_0=-1;
		final double SCALE_1=-1;
		final double SCALE_2=-1;
		//final double SCALE_3=-1;
		//final double SCALE_4=1;
		//final double SCALE_5=1;

		sendLineToRobot("G0"
    		+" X"+StringHelper.formatDouble(keyframe.fkValues[0]*SCALE_0)
    		+" Y"+StringHelper.formatDouble(keyframe.fkValues[1]*SCALE_1)
    		+" Z"+StringHelper.formatDouble(keyframe.fkValues[2]*SCALE_2)
    		//+" U"+StringHelper.formatDouble(keyframe.fkValues[3]*SCALE_3)
    		//+" V"+StringHelper.formatDouble(keyframe.fkValues[4]*SCALE_4)
    		//+" W"+StringHelper.formatDouble(keyframe.fkValues[5]*SCALE_5)
			);

	}*/

	@Override
	public RobotKeyframe createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
