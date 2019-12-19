package com.marginallyclever.robotOverlord.dhRobot.robots;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver_RTTRTR;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;


public class DHRobot_Thor extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean isFirstTime;
	public Material material;
	
	public DHRobot_Thor() {
		super(new DHIKSolver_RTTRTR());
		setDisplayName("Thor");
		isFirstTime=true;
	}
	
	@Override
	protected void setupLinks(DHRobot robot) {
		robot.setNumLinks(8);

		// roll
		robot.links.get(0).d=20.2;
		robot.links.get(0).theta=0;
		robot.links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(0).rangeMin=-120;
		robot.links.get(0).rangeMax=120;
		// tilt
		robot.links.get(1).d=0.0;
		robot.links.get(1).alpha=0;
		robot.links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(1).rangeMin=-72;
		// tilt
		robot.links.get(2).d=16.0;
		robot.links.get(2).alpha=0;
		robot.links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(2).rangeMin=-83.369;
		robot.links.get(2).rangeMax=86;
		// interim point
		robot.links.get(3).d=0.001;
		robot.links.get(3).alpha=90;
		robot.links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll
		robot.links.get(4).d=1.4;
		robot.links.get(4).theta=0;
		robot.links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(4).rangeMin=-90;
		robot.links.get(4).rangeMax=90;

		// tilt
		robot.links.get(5).d=18.1;
		robot.links.get(5).alpha=0;
		robot.links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		robot.links.get(5).rangeMin=-90;
		robot.links.get(5).rangeMax=90;
		// roll
		robot.links.get(6).d=5.35;
		robot.links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		robot.links.get(6).rangeMin=-90;
		robot.links.get(6).rangeMax=90;
		
		robot.links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		
		this.refreshPose();
	}
	
	public void setupModels() {
		material = new Material();
		float r=1;
		float g=0f/255f;
		float b=0f/255f;
		material.setDiffuseColor(r,g,b,1);
		
		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Thor/Thor0.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Thor/Thor1.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Thor/Thor2.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Thor/Thor3.stl",0.1f);
			links.get(5).model = ModelFactory.createModelFromFilename("/Thor/Thor4.stl",0.1f);
			links.get(6).model = ModelFactory.createModelFromFilename("/Thor/Thor5.stl",0.1f);
			links.get(7).model = ModelFactory.createModelFromFilename("/Thor/Thor6.stl",0.1f);

			links.get(1).model.adjustOrigin(new Vector3d(0,0,-15.35));
			links.get(1).model.adjustRotation(new Vector3d(0,0,90));
			links.get(2).model.adjustOrigin(new Vector3d(0,0,-6.5));
			links.get(2).model.adjustRotation(new Vector3d(0,0,90));
			links.get(3).model.adjustRotation(new Vector3d(90,0,90));
			links.get(3).model.adjustOrigin(new Vector3d(0,6,0));
			links.get(5).model.adjustOrigin(new Vector3d(0,0,0));
			links.get(5).model.adjustRotation(new Vector3d(0,0,90));
			links.get(6).model.adjustOrigin(new Vector3d(0,0,-4.75));
			links.get(6).model.adjustRotation(new Vector3d(0,0,90));
			links.get(7).model.adjustOrigin(new Vector3d(0,0,0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels();
		}

		
		material.render(gl2);
		
		gl2.glPushMatrix();
			Vector3d position = this.getPosition();
			gl2.glTranslated(position.x, position.y, position.z);
			
			// Draw models
			
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
	/*
	@Override
	public void sendNewStateToRobot(DHKeyframe keyframe) {
		// If the wiring on the robot is reversed, these parameters must also be reversed.
		// This is a software solution to a hardware problem.
		final double SCALE_0=-1;
		final double SCALE_1=-1;
		final double SCALE_2=-1;
		//final double SCALE_3=-1;
		//final double SCALE_4=1;
		//final double SCALE_5=1;

		sendLineToRobot("G0"
    		+" X"+StringHelper.formatDouble(keyframe.fkValues[0]*SCALE_0)
    		+" Y"+StringHelper.formatDouble(keyframe.fkValues[1]*SCALE_1)
    		+" Z"+StringHelper.formatDouble(keyframe.fkValues[2]*SCALE_2)
    		//+" U"+StringHelper.formatDouble(keyframe.fkValues[3]*SCALE_3)
    		//+" V"+StringHelper.formatDouble(keyframe.fkValues[4]*SCALE_4)
    		//+" W"+StringHelper.formatDouble(keyframe.fkValues[5]*SCALE_5)
			);

	}*/
}
