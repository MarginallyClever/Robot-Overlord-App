package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.material.Material;

/**
 * DHRobot version of Arm3, a palletizing robot I built long ago.  Incomplete!
 * @author Dan Royer
 *
 */
public class DHRobot_Arm3 extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public DHRobot_Arm3() {
		super();
		setDisplayName("Arm3");
	}
	
	@Override
	public void setupLinks() {
		// setup sixi2 as default.
		setNumLinks(5);
		// roll
		links.get(0).d=13.44;
		links.get(0).theta=0;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-160;
		links.get(0).rangeMax=160;
		// tilt
		links.get(1).alpha=0;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).rangeMin=-72;
		// tilt
		links.get(2).d=44.55;
		links.get(2).alpha=0;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		// interim point
		links.get(3).d=40;
		links.get(3).alpha=0;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// end effector
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
/*
		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm.stl",0.1f);
			links.get(4).model = ModelFactory.createModelFromFilename("/Sixi2/hand.stl",0.1f);

			links.get(0).model.adjustOrigin(new Vector3d(0, 0, 5.150f));
			links.get(0).model.adjustRotation(new Vector3d(90,90,0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			float g=1;
			float r=217f/255f;
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

	@Override
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_RTT();
	}

	@Override
	public void sendNewStateToRobot(DHKeyframe keyframe) {}
	
	@Override
	public boolean canEERotateX() {
		return false;
	}
	@Override
	public boolean canEERotateY() {
		return false;
	}
}
