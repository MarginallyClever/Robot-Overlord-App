package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
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
		super();
		setDisplayName("GMF M-100");
	}
	
	@Override
	public void setupLinks() {
		setNumLinks(5);
		// roll
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-120;
		links.get(0).rangeMax=120;
		// slide
		links.get(1).alpha=90;
		links.get(1).flags = DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(1).rangeMin=0;
		links.get(1).rangeMin=1300;
		// slide
		links.get(2).alpha=90;
		links.get(2).flags = DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(2).rangeMin=0;
		links.get(2).rangeMax=1100;
		// roll
		links.get(3).theta=0;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(3).rangeMin=-90;
		links.get(3).rangeMax=90;

		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		// load models here
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
	
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_GMF_M100();
	}
}
