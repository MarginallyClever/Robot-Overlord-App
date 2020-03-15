package com.marginallyclever.robotOverlord.entity.robotEntity.misc;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.engine.dhRobot.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.entity.materialEntity.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.robotEntity.RobotEntity;
import com.marginallyclever.robotOverlord.entity.robotEntity.RobotKeyframe;


/**
 * D-H robot modified for Andreas Hoelldorfer's MANTIS
 * @author Dan Royer
 * See https://hackaday.io/project/3800-3d-printable-robot-arm
 *
 */
public class Robot_Mantis extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3617214524612441513L;
	private transient boolean isFirstTime;
	DHRobot live;
	
	public Robot_Mantis() {
		super();
		setName("Mantis");
		
		live = new DHRobot();
		live.setIKSolver(new DHIKSolver_RTTRTR());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobot robot) {
		robot.setNumLinks(8);

		// roll
		//robot.links.get(0).d=13.44;
		robot.links.get(0).setD(24.5+2.7);
		robot.links.get(0).flags = LinkAdjust.THETA;
		robot.links.get(0).setRangeMin(-120);
		robot.links.get(0).setRangeMax(120);

		// tilt
		robot.links.get(1).flags = LinkAdjust.ALPHA;
		robot.links.get(1).setRangeMin(-72);

		// tilt
		robot.links.get(2).setD(13.9744 + 8.547);
		robot.links.get(2).flags = LinkAdjust.ALPHA;
		robot.links.get(2).setRangeMin(-83.369);
		robot.links.get(2).setRangeMax(86);
		// interim point
		robot.links.get(3).setD(0.001);  // TODO explain why this couldn't just be zero.  Solver hates it for some reason.
		robot.links.get(3).setAlpha(90);
		robot.links.get(3).flags = LinkAdjust.NONE;
		// roll
		robot.links.get(4).setD(8.547);
		robot.links.get(4).flags = LinkAdjust.THETA;
		robot.links.get(4).setRangeMin(-90);
		robot.links.get(4).setRangeMax(90);

		// tilt
		robot.links.get(5).setD(14.6855f);
		robot.links.get(5).flags = LinkAdjust.ALPHA;
		robot.links.get(5).setRangeMin(-90);
		robot.links.get(5).setRangeMax(90);
		// roll
		robot.links.get(6).setD(5.0f);
		robot.links.get(6).flags = LinkAdjust.THETA;
		robot.links.get(6).setRangeMin(-90);
		robot.links.get(6).setRangeMax(90);
		
		robot.links.get(7).flags = LinkAdjust.NONE;

		robot.refreshPose();
	}


	public void setupModels(DHRobot robot) {
		try {
			robot.links.get(0).setModelFilename("/AH/rotBaseCase.stl");
			robot.links.get(1).setModelFilename("/AH/Shoulder_r1.stl");
			robot.links.get(2).setModelFilename("/AH/Elbow.stl");
			robot.links.get(3).setModelFilename("/AH/Forearm.stl");
			robot.links.get(5).setModelFilename("/AH/Wrist_r1.stl");
			robot.links.get(6).setModelFilename("/AH/WristRot.stl");

			for( DHLink link : robot.links ) link.setModelScale(0.1f);
			
			robot.links.get(0).getModel().adjustOrigin(new Vector3d(0,0,2.7));
			robot.links.get(1).getModel().adjustRotation(new Vector3d(0,0,90));
			robot.links.get(1).getModel().adjustOrigin(new Vector3d(0,0,0));
			robot.links.get(2).getModel().adjustRotation(new Vector3d(90,90,90));
			robot.links.get(2).getModel().adjustOrigin(new Vector3d(0,0.476,2.7+(13.9744 + 8.547)/2));
			robot.links.get(3).getModel().adjustRotation(new Vector3d(180,90,90));
			robot.links.get(3).getModel().adjustOrigin(new Vector3d(0,-5.7162,0));//0.3488,0.3917
			robot.links.get(5).getModel().adjustOrigin(new Vector3d(0,0,0));
			robot.links.get(6).getModel().adjustRotation(new Vector3d(-180,90,0));
		} catch(Exception e) {
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
			MatrixHelper.applyMatrix(gl2, pose.get());
		
			MaterialEntity material = new MaterialEntity();
			float r=0.5f;
			float g=0.5f;
			float b=0.5f;
			material.setDiffuseColor(r,g,b,1);
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
