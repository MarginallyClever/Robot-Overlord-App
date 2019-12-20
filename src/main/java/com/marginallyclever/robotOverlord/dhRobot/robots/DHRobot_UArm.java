package com.marginallyclever.robotOverlord.dhRobot.robots;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver_RTT;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;

/**
 * Unfinished UArm implementation of DHRobot.
 * @author Dan Royer
 * See https://buildmedia.readthedocs.org/media/pdf/uarmdocs/latest/uarmdocs.pdf
 */
public class DHRobot_UArm extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient Model base;
	private transient Model shoulder;
	private transient Model bicep;
	private transient Model forearm;
	private transient Model wrist;
	private transient Model hand;
/*
	private transient Model linkA1;
	private transient Model linkA2;
	private transient Model linkA3;
	private transient Model linkB1;
	private transient Model linkB2;
*/
	public boolean isFirstTime;

	public DHRobot_UArm() {
		super(new DHIKSolver_RTT());
		isFirstTime=true;
		setDisplayName("UArm");
	}
	
	@Override
	protected void setupLinks(DHRobot robot) {
		robot.setNumLinks(6);
		// roll
		robot.links.get(0).setD(2.4);
		robot.links.get(0).setR(2.0728);
		robot.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(0).rangeMin=-160;
		robot.links.get(0).rangeMax=160;
		// tilt
		robot.links.get(1).setD(9.5267-2.4);
		robot.links.get(1).setTheta(90);
		robot.links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(1).rangeMin=-72;
		// tilt
		robot.links.get(2).setD(14.8004);
		robot.links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(2).rangeMin=-10;
		robot.links.get(2).rangeMax=150;
		
		// interim point
		robot.links.get(3).setD(16.0136);
		robot.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// end effector
		robot.links.get(4).setD(3.545);
		robot.links.get(4).setTheta(-90);
		robot.links.get(4).setR(1);
		robot.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_THETA;

		robot.links.get(5).setR(4);
		robot.links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
	}
	
	public void setupModels() {
		try {
			base     = ModelFactory.createModelFromFilename("/uArm/base.STL",0.1f);
			shoulder = ModelFactory.createModelFromFilename("/uArm/shoulder.STL",0.1f);
			bicep    = ModelFactory.createModelFromFilename("/uArm/bicep.STL",0.1f);
			forearm  = ModelFactory.createModelFromFilename("/uArm/forearm.STL",0.1f);
			wrist    = ModelFactory.createModelFromFilename("/uArm/wrist.STL",0.1f);
			hand     = ModelFactory.createModelFromFilename("/uArm/hand.STL",0.1f);/*
			linkA1 = ModelFactory.createModelFromFilename("/uArm/linkA1.STL",0.1f);
			linkA2 = ModelFactory.createModelFromFilename("/uArm/linkA2.STL",0.1f);
			linkA3 = ModelFactory.createModelFromFilename("/uArm/linkA3.STL",0.1f);
			linkB1 = ModelFactory.createModelFromFilename("/uArm/linkB1.STL",0.1f);
			linkB2 = ModelFactory.createModelFromFilename("/uArm/linkB2.STL",0.1f);*/

			links.get(0).model = base;
			links.get(1).model = shoulder;
			links.get(2).model = bicep;
			links.get(3).model = forearm;
			links.get(4).model = wrist;
			links.get(5).model = hand;


			links.get(0).model.adjustOrigin(new Vector3d(0,0,1.65f));
			links.get(1).model.adjustOrigin(new Vector3d(-2.0728f,0,1.65f-2.4f));
			links.get(1).model.adjustRotation(new Vector3d(0,0,-180));
			links.get(2).model.adjustOrigin(new Vector3d(-0.25f,0,1.65f));
			links.get(2).model.adjustRotation(new Vector3d(0,0,90));
			links.get(3).model.adjustOrigin(new Vector3d(-0.25f,0,0));//z23.511,x27.727
			links.get(3).model.adjustRotation(new Vector3d(0,0,90));
			links.get(4).model.adjustOrigin(new Vector3d(-0.25f,0,0));
			links.get(4).model.adjustRotation(new Vector3d(-90,0,90));
			links.get(5).model.adjustRotation(new Vector3d(0,-90,90));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) 
		{
			isFirstTime=false;
			setupModels();
		}
		links.get(2).rangeMin=20;
		links.get(2).rangeMax=165;
		
		// TODO calculate me in the solver?
		links.get(3).setAlpha(
				90
				-links.get(1).getAlpha()
				-links.get(2).getAlpha()
				);
		
		this.refreshPose();
		
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			Material mat = new Material();
			mat.setDiffuseColor(
					0.75f*247.0f/255.0f,
					0.75f*233.0f/255.0f,
					0.75f*215.0f/255.0f, 1);
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
