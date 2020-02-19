package com.marginallyclever.robotOverlord.entity.robot.misc;

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


/**
 * D-H robot modified for Andreas Hoelldorfer's MANTIS
 * @author Dan Royer
 * See https://hackaday.io/project/3800-3d-printable-robot-arm
 *
 */
public class Robot_Mantis extends Robot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient boolean isFirstTime;
	DHRobot live;
	
	public Robot_Mantis() {
		super();
		setDisplayName("Mantis");
		
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
		robot.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(0).setRangeMin(-120);
		robot.links.get(0).setRangeMax(120);

		// tilt
		robot.links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(1).setRangeMin(-72);

		// tilt
		robot.links.get(2).setD(13.9744 + 8.547);
		robot.links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(2).setRangeMin(-83.369);
		robot.links.get(2).setRangeMax(86);
		// interim point
		robot.links.get(3).setD(0.001);  // TODO explain why this couldn't just be zero.  Solver hates it for some reason.
		robot.links.get(3).setAlpha(90);
		robot.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		robot.links.get(4).setD(8.547);
		robot.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(4).setRangeMin(-90);
		robot.links.get(4).setRangeMax(90);

		// tilt
		robot.links.get(5).setD(14.6855f);
		robot.links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(5).setRangeMin(-90);
		robot.links.get(5).setRangeMax(90);
		// roll
		robot.links.get(6).setD(5.0f);
		robot.links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(6).setRangeMin(-90);
		robot.links.get(6).setRangeMax(90);
		
		robot.links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		robot.refreshPose();
	}


	public void setupModels(DHRobot robot) {
		try {
			robot.links.get(0).model = ModelFactory.createModelFromFilename("/AH/rotBaseCase.stl",0.1f);
			robot.links.get(1).model = ModelFactory.createModelFromFilename("/AH/Shoulder_r1.stl",0.1f);
			robot.links.get(2).model = ModelFactory.createModelFromFilename("/AH/Elbow.stl",0.1f);
			robot.links.get(3).model = ModelFactory.createModelFromFilename("/AH/Forearm.stl",0.1f);
			robot.links.get(5).model = ModelFactory.createModelFromFilename("/AH/Wrist_r1.stl",0.1f);
			robot.links.get(6).model = ModelFactory.createModelFromFilename("/AH/WristRot.stl",0.1f);
			
			robot.links.get(0).model.adjustOrigin(new Vector3d(0,0,2.7));
			robot.links.get(1).model.adjustRotation(new Vector3d(0,0,90));
			robot.links.get(1).model.adjustOrigin(new Vector3d(0,0,0));
			robot.links.get(2).model.adjustRotation(new Vector3d(90,90,90));
			robot.links.get(2).model.adjustOrigin(new Vector3d(0,0.476,2.7+(13.9744 + 8.547)/2));
			robot.links.get(3).model.adjustRotation(new Vector3d(180,90,90));
			robot.links.get(3).model.adjustOrigin(new Vector3d(0,-5.7162,0));//0.3488,0.3917
			robot.links.get(5).model.adjustOrigin(new Vector3d(0,0,0));
			robot.links.get(6).model.adjustRotation(new Vector3d(-180,90,0));

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
			MatrixHelper.applyMatrix(gl2, this.matrix);
		
			Material material = new Material();
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
