package com.marginallyclever.robotOverlord.robots;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.dhRobotEntity.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.uiExposedTypes.MaterialEntity;


public class Robot_Thor extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1098202067875588529L;
	public transient boolean isFirstTime;
	public MaterialEntity material;
	protected DHRobotModel live;
	
	public Robot_Thor() {
		super();
		setName("Thor");

		live = new DHRobotModel();
		live.setIKSolver(new DHIKSolver_RTTRTR());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotModel robot) {
		robot.setNumLinks(8);

		// roll
		robot.getLink(0).setD(20.2);
		robot.getLink(0).flags = LinkAdjust.THETA;
		robot.getLink(0).setRangeMin(-120);
		robot.getLink(0).setRangeMax(120);
		// tilt
		robot.getLink(1).flags = LinkAdjust.ALPHA;
		robot.getLink(1).setRangeMin(-72);
		// tilt
		robot.getLink(2).setD(16.0);
		robot.getLink(2).flags = LinkAdjust.ALPHA;
		robot.getLink(2).setRangeMin(-83.369);
		robot.getLink(2).setRangeMax(86);
		// interim point
		robot.getLink(3).setD(0.001);
		robot.getLink(3).setAlpha(90);
		robot.getLink(3).flags = LinkAdjust.NONE;
		// roll
		robot.getLink(4).setD(1.4);
		robot.getLink(4).flags = LinkAdjust.THETA;
		robot.getLink(4).setRangeMin(-90);
		robot.getLink(4).setRangeMax(90);

		// tilt
		robot.getLink(5).setD(18.1);
		robot.getLink(5).flags = LinkAdjust.ALPHA;
		robot.getLink(5).setRangeMin(-90);
		robot.getLink(5).setRangeMax(90);
		// roll
		robot.getLink(6).setD(5.35);
		robot.getLink(6).flags = LinkAdjust.THETA;
		robot.getLink(6).setRangeMin(-90);
		robot.getLink(6).setRangeMax(90);
		
		robot.getLink(7).flags = LinkAdjust.NONE;
		
		robot.refreshDHMatrixes();
	}
	
	public void setupModels(DHRobotModel robot) {
		material = new MaterialEntity();
		float r=1;
		float g=0f/255f;
		float b=0f/255f;
		material.setDiffuseColor(r,g,b,1);
		
		try {
			robot.getLink(0).setShapeFilename("/Thor/Thor0.stl");
			robot.getLink(1).setShapeFilename("/Thor/Thor1.stl");
			robot.getLink(2).setShapeFilename("/Thor/Thor2.stl");
			robot.getLink(3).setShapeFilename("/Thor/Thor3.stl");
			robot.getLink(5).setShapeFilename("/Thor/Thor4.stl");
			robot.getLink(6).setShapeFilename("/Thor/Thor5.stl");
			robot.getLink(7).setShapeFilename("/Thor/Thor6.stl");

			robot.getLink(1).setShapeOrigin(new Vector3d(0,0,-15.35));
			robot.getLink(1).setShapeRotation(new Vector3d(0,0,90));
			robot.getLink(2).setShapeOrigin(new Vector3d(0,0,-6.5));
			robot.getLink(2).setShapeRotation(new Vector3d(0,0,90));
			robot.getLink(3).setShapeRotation(new Vector3d(90,0,90));
			robot.getLink(3).setShapeOrigin(new Vector3d(0,6,0));
			robot.getLink(5).setShapeOrigin(new Vector3d(0,0,0));
			robot.getLink(5).setShapeRotation(new Vector3d(0,0,90));
			robot.getLink(6).setShapeOrigin(new Vector3d(0,0,-4.75));
			robot.getLink(6).setShapeRotation(new Vector3d(0,0,90));
			robot.getLink(7).setShapeOrigin(new Vector3d(0,0,0));
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
			MatrixHelper.applyMatrix(gl2, pose);
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
	public Memento createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
