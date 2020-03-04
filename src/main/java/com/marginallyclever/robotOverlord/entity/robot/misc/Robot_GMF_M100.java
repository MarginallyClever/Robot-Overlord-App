package com.marginallyclever.robotOverlord.entity.robot.misc;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.engine.dhRobot.solvers.DHIKSolver_Cylindrical;
import com.marginallyclever.robotOverlord.entity.material.Material;
import com.marginallyclever.robotOverlord.entity.robot.Robot;
import com.marginallyclever.robotOverlord.entity.robot.RobotKeyframe;

/**
 * FANUC cylindrical coordinate robot GMF M-100
 * @author Dan Royer
 *
 */
public class Robot_GMF_M100 extends Robot {
	DHRobot live;

	public Robot_GMF_M100() {
		super();
		setName("FANUC GMF M-100");

		live = new DHRobot();
		live.setIKSolver(new DHIKSolver_Cylindrical());
		setupLinks(live);
	}
	
	protected void setupLinks(DHRobot robot) {
		robot.setNumLinks(5);
		// roll
		robot.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(0).setRangeMin(-120);
		robot.links.get(0).setRangeMax(120);
		// slide
		robot.links.get(1).setAlpha(90);
		robot.links.get(1).flags = DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(1).setRangeMin(0);
		robot.links.get(1).setRangeMin(1300);
		// slide
		robot.links.get(2).setAlpha(90);
		robot.links.get(2).flags = DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(2).setRangeMin(0);
		robot.links.get(2).setRangeMax(1100);
		// roll
		robot.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(3).setRangeMin(-90);
		robot.links.get(3).setRangeMax(90);

		robot.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		robot.refreshPose();
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.getPose());	
			// Draw models
			float r=1;
			float g=217f/255f;
			float b=33f/255f;
			Material mat = new Material();
			mat.setDiffuseColor(r,g,b,1);
			mat.render(gl2);
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
