package com.marginallyclever.robotOverlord.dhRobot;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;


/**
 * D-H robot modified for Andreas Hoelldorfer's MANTIS
 * @author Dan Royer
 * @see https://hackaday.io/project/3800-3d-printable-robot-arm
 *
 */
public class DHRobot_Mantis extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DHRobot_Mantis() {
		super();
		setDisplayName("Mantis");
	}
	
	@Override
	public void setupLinks() {
		setNumLinks(8);

		// roll
		//this.links.get(0).d=13.44;
		this.links.get(0).d=24.5+2.7;
		this.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		this.links.get(0).rangeMin=-120;
		this.links.get(0).rangeMax=120;

		// tilt
		this.links.get(1).d=0;
		this.links.get(1).alpha=0;
		this.links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		this.links.get(1).rangeMin=-72;

		// tilt
		this.links.get(2).d=13.9744 + 8.547;
		this.links.get(2).alpha=0;
		this.links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		this.links.get(2).rangeMin=-83.369;
		this.links.get(2).rangeMax=86;
		// interim point
		this.links.get(3).d=0.001;  // TODO explain why this couldn't just be zero.  Solver hates it for some reason.
		this.links.get(3).alpha=90;
		this.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		this.links.get(4).d=8.547;
		this.links.get(4).theta=0;
		this.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		this.links.get(4).rangeMin=-90;
		this.links.get(4).rangeMax=90;

		// tilt
		this.links.get(5).d=14.6855f;
		this.links.get(5).alpha=0;
		this.links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		this.links.get(5).rangeMin=-90;
		this.links.get(5).rangeMax=90;
		// roll
		this.links.get(6).d=5.0f;
		this.links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		this.links.get(6).rangeMin=-90;
		this.links.get(6).rangeMax=90;
		
		this.links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		try {
			this.links.get(0).model = ModelFactory.createModelFromFilename("/AH/rotBaseCase.stl",0.1f);
			this.links.get(1).model = ModelFactory.createModelFromFilename("/AH/Shoulder_r1.stl",0.1f);
			this.links.get(2).model = ModelFactory.createModelFromFilename("/AH/Elbow.stl",0.1f);
			this.links.get(3).model = ModelFactory.createModelFromFilename("/AH/Forearm.stl",0.1f);
			this.links.get(5).model = ModelFactory.createModelFromFilename("/AH/Wrist_r1.stl",0.1f);
			this.links.get(6).model = ModelFactory.createModelFromFilename("/AH/WristRot.stl",0.1f);
			
			this.links.get(0).model.adjustOrigin(new Vector3d(0,0,2.7));
			this.links.get(1).model.adjustRotation(new Vector3d(0,0,90));
			this.links.get(1).model.adjustOrigin(new Vector3d(0,0,0));
			this.links.get(2).model.adjustRotation(new Vector3d(90,90,90));
			this.links.get(2).model.adjustOrigin(new Vector3d(0,0.476,2.7+(13.9744 + 8.547)/2));
			this.links.get(3).model.adjustRotation(new Vector3d(180,90,90));
			this.links.get(3).model.adjustOrigin(new Vector3d(0,-5.7162,0));//0.3488,0.3917
			this.links.get(5).model.adjustOrigin(new Vector3d(0,0,0));
			this.links.get(6).model.adjustRotation(new Vector3d(-180,90,0));

		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.refreshPose();
	}
	
	
	@Override
	public void render(GL2 gl2) {
		
		Material material = new Material();
		
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			float r=0.5f;
			float g=0.5f;
			float b=0.5f;
			material.setDiffuseColor(r,g,b,1);
			material.render(gl2);
			
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
		return new DHIKSolver_RTTRTR();
	}

	public void sendNewStateToRobot(DHKeyframe keyframe) {}
}
