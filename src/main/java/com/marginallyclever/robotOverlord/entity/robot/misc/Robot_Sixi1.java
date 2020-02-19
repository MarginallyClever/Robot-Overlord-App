package com.marginallyclever.robotOverlord.entity.robot.misc;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.engine.dhRobot.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.engine.model.ModelFactory;
import com.marginallyclever.robotOverlord.entity.material.Material;
import com.marginallyclever.robotOverlord.entity.robot.Robot;
import com.marginallyclever.robotOverlord.entity.robot.RobotKeyframe;


public class Robot_Sixi1 extends Robot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected transient boolean isFirstTime;
	protected Material material;
	protected DHRobot live;

	public Robot_Sixi1() {
		super();
		setDisplayName("Sixi 1");
		live = new DHRobot();
		live.setIKSolver(new DHIKSolver_RTTRTR());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobot robot) {
		robot.setNumLinks(8);
		// roll
		robot.links.get(0).setD(25);
		robot.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(0).setRangeMin(-120);
		robot.links.get(0).setRangeMax(120);
		// tilt
		robot.links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(1).setRangeMin(-72);
		// tilt
		robot.links.get(2).setD(25);
		robot.links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(2).setRangeMin(-83.369);
		robot.links.get(2).setRangeMax(86);

		// interim point
		robot.links.get(3).setD(5);
		robot.links.get(3).setAlpha(90);
		robot.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		robot.links.get(4).setD(10);
		robot.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(4).setRangeMin(-90);
		robot.links.get(4).setRangeMax(90);

		// tilt
		robot.links.get(5).setD(10);
		robot.links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(5).setRangeMin(-90);
		robot.links.get(5).setRangeMax(90);
		// roll
		robot.links.get(6).setD(3.9527);
		robot.links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(6).setRangeMin(-90);
		robot.links.get(6).setRangeMax(90);
		
		robot.links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		robot.refreshPose();
	}
	
	public void setupModels(DHRobot robot) {
		material = new Material();
		float r=0.75f;
		float g=0.15f;
		float b=0.15f;
		material.setDiffuseColor(r,g,b,1);
		
		try {
			robot.links.get(0).model = ModelFactory.createModelFromFilename("/Sixi/anchor.stl",0.1f);
			robot.links.get(1).model = ModelFactory.createModelFromFilename("/Sixi/shoulder.stl",0.1f);
			robot.links.get(2).model = ModelFactory.createModelFromFilename("/Sixi/bicep.stl",0.1f);
			robot.links.get(3).model = ModelFactory.createModelFromFilename("/Sixi/elbow.stl",0.1f);
			robot.links.get(5).model = ModelFactory.createModelFromFilename("/Sixi/forearm.stl",0.1f);
			robot.links.get(6).model = ModelFactory.createModelFromFilename("/Sixi/wrist.stl",0.1f);
			robot.links.get(7).model = ModelFactory.createModelFromFilename("/Sixi/hand.stl",0.1f);

			robot.links.get(1).model.adjustOrigin(new Vector3d(0, 0, -25));
			robot.links.get(2).model.adjustOrigin(new Vector3d(0, -5, -25));
			robot.links.get(2).model.adjustRotation(new Vector3d(-11.3,0,0));
			
			robot.links.get(5).model.adjustOrigin(new Vector3d(0, 0, -60));
			robot.links.get(6).model.adjustOrigin(new Vector3d(0, 0, -70));
			robot.links.get(7).model.adjustOrigin(new Vector3d(0, 0, -74));

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
			
			robot.links.get(3).model.adjustOrigin(adjustPos);
			robot.links.get(3).model.adjustRotation(new Vector3d(90, 0, 0));
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
			MatrixHelper.applyMatrix(gl2, this.matrix);
			material.render(gl2);
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
