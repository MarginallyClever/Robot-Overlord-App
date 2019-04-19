package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;


public class DHRobot_Sixi1 extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean isFirstTime;
	public Material material;

	public DHRobot_Sixi1() {
		super();
		setDisplayName("Sixi 1");
		isFirstTime=true;
	}
	
	@Override
	public void setupLinks() {		
		setNumLinks(8);
		// roll
		links.get(0).d=25;
		links.get(0).theta=0;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-120;
		links.get(0).rangeMax=120;
		// tilt
		links.get(1).alpha=0;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).rangeMin=-72;
		// tilt
		links.get(2).d=25;
		links.get(2).alpha=0;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).rangeMin=-83.369;
		links.get(2).rangeMax=86;

		// interim point
		links.get(3).d=5;
		links.get(3).alpha=90;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		links.get(4).d=10;
		links.get(4).theta=0;
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).rangeMin=-90;
		links.get(4).rangeMax=90;

		// tilt
		links.get(5).d=10;
		links.get(5).alpha=0;
		links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(5).rangeMin=-90;
		links.get(5).rangeMax=90;
		// roll
		links.get(6).d=3.9527;
		links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(6).rangeMin=-90;
		links.get(6).rangeMax=90;
		
		links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		this.refreshPose();
	}
	
	public void setupModels() {
		material = new Material();
		float r=0.75f;
		float g=0.15f;
		float b=0.15f;
		material.setDiffuseColor(r,g,b,1);
		
		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Sixi/anchor.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Sixi/shoulder.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Sixi/bicep.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Sixi/elbow.stl",0.1f);
			links.get(5).model = ModelFactory.createModelFromFilename("/Sixi/forearm.stl",0.1f);
			links.get(6).model = ModelFactory.createModelFromFilename("/Sixi/wrist.stl",0.1f);
			links.get(7).model = ModelFactory.createModelFromFilename("/Sixi/hand.stl",0.1f);

			links.get(1).model.adjustOrigin(new Vector3d(0, 0, -25));
			links.get(2).model.adjustOrigin(new Vector3d(0, -5, -25));
			links.get(2).model.adjustRotation(new Vector3d(-11.3,0,0));
			
			links.get(5).model.adjustOrigin(new Vector3d(0, 0, -60));
			links.get(6).model.adjustOrigin(new Vector3d(0, 0, -70));
			links.get(7).model.adjustOrigin(new Vector3d(0, 0, -74));

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
			
			links.get(3).model.adjustOrigin(adjustPos);
			links.get(3).model.adjustRotation(new Vector3d(90, 0, 0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void pick() {
		this.refreshPose();
		targetPose.set(endMatrix);
		drawSkeleton=true;
	}
	
	@Override
	public void unPick() {
		drawSkeleton=false;
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels();
		}

		material.render(gl2);
		
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			gl2.glPushMatrix();
				Iterator<DHLink> i = links.iterator();
				while(i.hasNext()) {
					DHLink link = i.next();
					link.renderModel(gl2);
				}
			gl2.glPopMatrix();

		gl2.glPopMatrix();
		
		super.render(gl2);
	}
	
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_RTTRTR();
	}

	public void sendPoseToRobot(DHKeyframe keyframe) {}
}
