package com.marginallyclever.robotOverlord.dhRobot.robots;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver_Cylindrical;
import com.marginallyclever.robotOverlord.material.Material;

/**
 * FANUC cylindrical coordinate robot GMF M-100
 * @author Dan Royer
 *
 */
public class DHRobot_GMF_M100 extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public DHRobot_GMF_M100() {
		super(new DHIKSolver_Cylindrical());
		setDisplayName("FANUC GMF M-100");
		//only here until there are models to render.
		drawAsSelected=true;
	}
	
	@Override
	protected void setupLinks(DHRobot robot) {
		robot.setNumLinks(5);
		// roll
		robot.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(0).rangeMin=-120;
		robot.links.get(0).rangeMax=120;
		// slide
		robot.links.get(1).alpha=90;
		robot.links.get(1).flags = DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(1).rangeMin=0;
		robot.links.get(1).rangeMin=1300;
		// slide
		robot.links.get(2).alpha=90;
		robot.links.get(2).flags = DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(2).rangeMin=0;
		robot.links.get(2).rangeMax=1100;
		// roll
		robot.links.get(3).theta=0;
		robot.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(3).rangeMin=-90;
		robot.links.get(3).rangeMax=90;

		robot.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		robot.refreshPose();
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			float r=1;
			float g=217f/255f;
			float b=33f/255f;
			Material mat = new Material();
			mat.setDiffuseColor(r,g,b,1);
			mat.render(gl2);
			
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
}
