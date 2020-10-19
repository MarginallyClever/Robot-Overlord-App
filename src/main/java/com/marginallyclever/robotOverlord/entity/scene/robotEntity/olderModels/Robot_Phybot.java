package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;


public class Robot_Phybot extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8578641402989287093L;
	public transient boolean isFirstTime;
	public MaterialEntity material;
	DHRobotModel live;
	
	public Robot_Phybot() {
		super();
		setName("Phybot");

		live = new DHRobotModel();
		live.setIKSolver(new DHIKSolver_RTTRTR());
		setupLinks(live);
		
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotModel robot) {
		robot.setNumLinks(8);
		// roll
		robot.getLink(0).setD(25);
		robot.getLink(0).flags = LinkAdjust.THETA;
		robot.getLink(0).setRangeMin(-120);
		robot.getLink(0).setRangeMax(120);
		// tilt
		robot.getLink(1).flags = LinkAdjust.ALPHA;
		robot.getLink(1).setRangeMin(-72);
		// tilt
		robot.getLink(2).setD(25);
		robot.getLink(2).flags = LinkAdjust.ALPHA;
		robot.getLink(2).setRangeMin(-83.369);
		robot.getLink(2).setRangeMax(86);

		// interim point
		robot.getLink(3).setD(5);
		robot.getLink(3).setAlpha(90);
		robot.getLink(3).flags = LinkAdjust.NONE;
		// roll
		robot.getLink(4).setD(10);
		robot.getLink(4).flags = LinkAdjust.THETA;
		robot.getLink(4).setRangeMin(-90);
		robot.getLink(4).setRangeMax(90);

		// tilt
		robot.getLink(5).setD(10);
		robot.getLink(5).flags = LinkAdjust.ALPHA;
		robot.getLink(5).setRangeMin(-90);
		robot.getLink(5).setRangeMax(90);
		// roll
		robot.getLink(6).setD(3.9527);
		robot.getLink(6).flags = LinkAdjust.THETA;
		robot.getLink(6).setRangeMin(-90);
		robot.getLink(6).setRangeMax(90);
		
		robot.getLink(7).flags = LinkAdjust.NONE;

		robot.refreshPose();
	}
	
	public void setupModels(DHRobotModel robot) {
		material = new MaterialEntity();
		float r=0.75f;
		float g=0.15f;
		float b=0.15f;
		material.setDiffuseColor(r,g,b,1);
		
		try {
			robot.getLink(0).setModelFilename("/Sixi/anchor.stl");
			robot.getLink(1).setModelFilename("/Sixi/shoulder.stl");
			robot.getLink(2).setModelFilename("/Sixi/bicep.stl");
			robot.getLink(3).setModelFilename("/Sixi/elbow.stl");
			robot.getLink(5).setModelFilename("/Sixi/forearm.stl");
			robot.getLink(6).setModelFilename("/Sixi/wrist.stl");
			robot.getLink(7).setModelFilename("/Sixi/hand.stl");

			for(int i=0;i<robot.getNumLinks();++i) 
				robot.getLink(i).setModelScale(0.1f);
			
			robot.getLink(1).getModel().adjustOrigin(new Vector3d(0, 0, -25));
			robot.getLink(2).getModel().adjustOrigin(new Vector3d(0, -5, -25));
			robot.getLink(2).getModel().adjustRotation(new Vector3d(-11.3,0,0));
			
			robot.getLink(5).getModel().adjustOrigin(new Vector3d(0, 0, -60));
			robot.getLink(6).getModel().adjustOrigin(new Vector3d(0, 0, -70));
			robot.getLink(7).getModel().adjustOrigin(new Vector3d(0, 0, -74));

			Matrix4d rot = new Matrix4d();
			Matrix4d rotX = new Matrix4d();
			Matrix4d rotY = new Matrix4d();
			Matrix4d rotZ = new Matrix4d();
			rot.setIdentity();
			rotX.rotX((float)Math.toRadians(90));
			rotY.rotY((float)Math.toRadians(0));
			rotZ.rotZ((float)Math.toRadians(0));
			rot.set(rotX);
			rot.mul(rotY);
			rot.mul(rotZ);
			Matrix4d pose = new Matrix4d(rot);
			Vector3d adjustPos = new Vector3d(0, 5, -50);
			pose.transform(adjustPos);
			
			robot.getLink(3).getModel().adjustOrigin(adjustPos);
			robot.getLink(3).getModel().adjustRotation(new Vector3d(90, 0, 0));
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

		material.render(gl2);
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);			
			live.render(gl2);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	public DHIKSolver getIKSolver() {
		return new DHIKSolver_RTTRTR();
	}

	public void sendNewStateToRobot(PoseFK keyframe) {}

	@Override
	public Memento createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
