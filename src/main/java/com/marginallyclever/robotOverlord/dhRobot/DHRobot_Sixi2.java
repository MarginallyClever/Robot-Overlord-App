package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;


public class DHRobot_Sixi2 extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean isFirstTime;
	public Material material;
	
	public DHRobot_Sixi2() {
		super();
		setDisplayName("Sixi 2");
		isFirstTime=true;
	}
	
	@Override
	public void setupLinks() {
		setNumLinks(8);
		// roll
		links.get(0).d=13.44;
		links.get(0).theta=0;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-120;
		links.get(0).rangeMax=120;
		// tilt
		links.get(1).alpha=0;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).rangeMin=-72;
		// tilt
		links.get(2).d=44.55;
		links.get(2).alpha=0;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).rangeMin=-83.369;
		links.get(2).rangeMax=86;
		// interim point
		links.get(3).d=4.7201;
		links.get(3).alpha=90;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		links.get(4).d=28.805;
		links.get(4).theta=0;
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).rangeMin=-90;
		links.get(4).rangeMax=90;

		// tilt
		links.get(5).d=11.8;
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
		float r=1;
		float g=217f/255f;
		float b=33f/255f;
		material.setDiffuseColor(r,g,b,1);
		
		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm.stl",0.1f);
			links.get(5).model = ModelFactory.createModelFromFilename("/Sixi2/tuningFork.stl",0.1f);
			links.get(6).model = ModelFactory.createModelFromFilename("/Sixi2/picassoBox.stl",0.1f);
			links.get(7).model = ModelFactory.createModelFromFilename("/Sixi2/hand.stl",0.1f);

			double ELBOW_TO_ULNA_Y = -28.805f;
			double ELBOW_TO_ULNA_Z = 4.7201f;
			double ULNA_TO_WRIST_Y = -11.800f;
			double ULNA_TO_WRIST_Z = 0;
			double ELBOW_TO_WRIST_Y = ELBOW_TO_ULNA_Y + ULNA_TO_WRIST_Y;
			double ELBOW_TO_WRIST_Z = ELBOW_TO_ULNA_Z + ULNA_TO_WRIST_Z;
			//double WRIST_TO_HAND = 8.9527;

			links.get(0).model.adjustOrigin(new Vector3d(0, 0, 5.150f));
			links.get(1).model.adjustOrigin(new Vector3d(0, 0, 8.140f-13.44f));
			links.get(2).model.adjustOrigin(new Vector3d(-1.82f, 0, 9));
			links.get(3).model.adjustOrigin(new Vector3d(0, (float)ELBOW_TO_WRIST_Y, (float)ELBOW_TO_WRIST_Z));
			links.get(5).model.adjustOrigin(new Vector3d(0, 0, (float)-ULNA_TO_WRIST_Y));
			links.get(7).model.adjustOrigin(new Vector3d(0,0,-3.9527f));

			links.get(0).model.adjustRotation(new Vector3d(90,90,0));
			links.get(1).model.adjustRotation(new Vector3d(90,0,0));
			links.get(2).model.adjustRotation(new Vector3d(90,0,0));
			links.get(3).model.adjustRotation(new Vector3d(90,180,0));
			links.get(5).model.adjustRotation(new Vector3d(180,0,180));
			links.get(6).model.adjustRotation(new Vector3d(180,0,180));
			links.get(7).model.adjustRotation(new Vector3d(180,0,180));
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
	
	@Override
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_RTTRTR();
	}
	
	@Override
	public void sendPoseToRobot(DHKeyframe keyframe) {
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

	}
}
