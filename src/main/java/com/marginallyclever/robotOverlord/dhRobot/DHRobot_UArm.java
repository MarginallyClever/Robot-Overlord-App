package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;

/**
 * Unfinished UArm implementation of DHRobot.
 * @author Dan Royer
 * @see https://buildmedia.readthedocs.org/media/pdf/uarmdocs/latest/uarmdocs.pdf
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
		super();
		isFirstTime=true;
		setDisplayName("UArm");
	}
	
	@Override
	protected void setupLinks() {
		setNumLinks(6);
		// roll
		links.get(0).d=2.4;
		links.get(0).r=2.0728;
		links.get(0).alpha=0;
		links.get(0).theta=0;
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).rangeMin=-160;
		links.get(0).rangeMax=160;
		// tilt
		links.get(1).d=9.5267-2.4f;
		links.get(1).r=0;
		links.get(1).theta=90;
		links.get(1).alpha=0;
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).rangeMin=-72;
		// tilt
		links.get(2).d=14.8004;
		links.get(2).theta=0;
		links.get(2).alpha=0;
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).rangeMin=-10;
		links.get(2).rangeMax=150;
		
		// interim point
		links.get(3).d=16.0136;
		links.get(3).alpha=0;
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// end effector
		links.get(4).d=3.545;
		links.get(4).theta=-90;
		links.get(4).alpha=0;
		links.get(4).r=1;
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_THETA;

		links.get(5).d=0;
		links.get(5).r=4;
		links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
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
		links.get(3).alpha=90-links.get(1).alpha-links.get(2).alpha;
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
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_RTT();
	}

	@Override
	public void sendNewStateToRobot(DHKeyframe keyframe) {}

	@Override
	public boolean canTargetPoseRotateX() {
		return false;
	}
	@Override
	public boolean canTargetPoseRotateY() {
		return false;
	}
}
