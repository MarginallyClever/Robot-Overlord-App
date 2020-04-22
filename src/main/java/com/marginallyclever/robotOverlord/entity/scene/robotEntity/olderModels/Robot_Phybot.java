package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotKeyframe;


public class Robot_Phybot extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5976403627830834543L;
	public transient boolean isFirstTime;
	public MaterialEntity material;
	DHRobotEntity live;
	
	public Robot_Phybot() {
		super();
		setName("Phybot");

		live = new DHRobotEntity();
		live.setIKSolver(new DHIKSolver_RTTRTR());
		setupLinks(live);
		
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotEntity robot) {
		robot.setNumLinks(8);
		// roll
		robot.links.get(0).setD(25);
		robot.links.get(0).flags = LinkAdjust.THETA;
		robot.links.get(0).setRangeMin(-120);
		robot.links.get(0).setRangeMax(120);
		// tilt
		robot.links.get(1).flags = LinkAdjust.ALPHA;
		robot.links.get(1).setRangeMin(-72);
		// tilt
		robot.links.get(2).setD(25);
		robot.links.get(2).flags = LinkAdjust.ALPHA;
		robot.links.get(2).setRangeMin(-83.369);
		robot.links.get(2).setRangeMax(86);

		// interim point
		robot.links.get(3).setD(5);
		robot.links.get(3).setAlpha(90);
		robot.links.get(3).flags = LinkAdjust.NONE;
		// roll
		robot.links.get(4).setD(10);
		robot.links.get(4).flags = LinkAdjust.THETA;
		robot.links.get(4).setRangeMin(-90);
		robot.links.get(4).setRangeMax(90);

		// tilt
		robot.links.get(5).setD(10);
		robot.links.get(5).flags = LinkAdjust.ALPHA;
		robot.links.get(5).setRangeMin(-90);
		robot.links.get(5).setRangeMax(90);
		// roll
		robot.links.get(6).setD(3.9527);
		robot.links.get(6).flags = LinkAdjust.THETA;
		robot.links.get(6).setRangeMin(-90);
		robot.links.get(6).setRangeMax(90);
		
		robot.links.get(7).flags = LinkAdjust.NONE;

		robot.refreshPose();
	}
	
	public void setupModels(DHRobotEntity robot) {
		material = new MaterialEntity();
		float r=0.75f;
		float g=0.15f;
		float b=0.15f;
		material.setDiffuseColor(r,g,b,1);
		
		try {
			robot.links.get(0).model.setModelFilename("/Sixi/anchor.stl");
			robot.links.get(1).model.setModelFilename("/Sixi/shoulder.stl");
			robot.links.get(2).model.setModelFilename("/Sixi/bicep.stl");
			robot.links.get(3).model.setModelFilename("/Sixi/elbow.stl");
			robot.links.get(5).model.setModelFilename("/Sixi/forearm.stl");
			robot.links.get(6).model.setModelFilename("/Sixi/wrist.stl");
			robot.links.get(7).model.setModelFilename("/Sixi/hand.stl");

			for( DHLink link : robot.links ) link.model.setModelScale(0.1f);
			robot.links.get(1).model.getModel().adjustOrigin(new Vector3d(0, 0, -25));
			robot.links.get(2).model.getModel().adjustOrigin(new Vector3d(0, -5, -25));
			robot.links.get(2).model.getModel().adjustRotation(new Vector3d(-11.3,0,0));
			
			robot.links.get(5).model.getModel().adjustOrigin(new Vector3d(0, 0, -60));
			robot.links.get(6).model.getModel().adjustOrigin(new Vector3d(0, 0, -70));
			robot.links.get(7).model.getModel().adjustOrigin(new Vector3d(0, 0, -74));

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
			
			robot.links.get(3).model.getModel().adjustOrigin(adjustPos);
			robot.links.get(3).model.getModel().adjustRotation(new Vector3d(90, 0, 0));
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
			MatrixHelper.applyMatrix(gl2, this.pose.get());			
			live.render(gl2);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	public DHIKSolver getIKSolver() {
		return new DHIKSolver_RTTRTR();
	}

	public void sendNewStateToRobot(DHKeyframe keyframe) {}

	@Override
	public RobotKeyframe createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
