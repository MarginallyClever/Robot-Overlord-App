package com.marginallyclever.robotOverlord.dhRobot.robots;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver_RTT;
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
		super(new DHIKSolver_RTT());
		setDisplayName("Arm3");
	}
	
	protected void setupLinks(DHRobot robot) {
		// setup sixi2 as default.
		robot.setNumLinks(5);
		// roll
		robot.links.get(0).setD(13.44);
		robot.links.get(0).setTheta(0);
		robot.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(0).rangeMin=-160;
		robot.links.get(0).rangeMax=160;
		// tilt
		robot.links.get(1).setAlpha(0);
		robot.links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(2).rangeMin=-72;
		// tilt
		robot.links.get(2).setD(44.55);
		robot.links.get(2).setAlpha(0);
		robot.links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		// interim point
		robot.links.get(3).setD(40);
		robot.links.get(3).setAlpha(0);
		robot.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// end effector
		robot.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
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
	public boolean canTargetPoseRotateX() {
		return false;
	}
	@Override
	public boolean canTargetPoseRotateY() {
		return false;
	}
}
