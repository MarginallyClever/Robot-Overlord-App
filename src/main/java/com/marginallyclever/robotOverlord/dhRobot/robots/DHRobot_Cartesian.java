package com.marginallyclever.robotOverlord.dhRobot.robots;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.dhRobot.DHIKSolver;
import com.marginallyclever.robotOverlord.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.dhRobot.solvers.DHIKSolver_Cartesian;
import com.marginallyclever.robotOverlord.material.Material;
import com.marginallyclever.robotOverlord.model.ModelFactory;

/**
 * Cartesian 3 axis CNC robot like 3d printer or milling machine.
 * Effectively three prismatic joints.  Use this as an example for other cartesian machines.
 * @author Dan Royer
 *
 */
public class DHRobot_Cartesian extends DHRobot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean isFirstTime;
	public Material material;
	
	public DHRobot_Cartesian() {
		super();
		setDisplayName("Cartesian");
		isFirstTime=true;
	}
	
	@Override
	protected void setupLinks() {
		setNumLinks(4);
		// roll
		links.get(0).d=0;
		links.get(0).alpha=90;
		links.get(0).flags = DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA | DHLink.READ_ONLY_THETA;
		links.get(0).rangeMin=0;
		links.get(0).rangeMax=25;
		// tilt
		links.get(1).alpha=90;
		links.get(1).flags = DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA | DHLink.READ_ONLY_THETA;
		links.get(1).rangeMin=0;
		links.get(1).rangeMax=21;
		// tilt
		links.get(2).alpha=90;
		links.get(2).flags = DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA | DHLink.READ_ONLY_THETA;
		links.get(2).rangeMin=0+8.422;
		links.get(2).rangeMax=21+8.422;
		
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		
		this.refreshPose();
	}
	
	public void setupModels() {
		material = new Material();
		float r=0.5f;
		float g=0.5f;
		float b=0.5f;
		material.setDiffuseColor(r,g,b,1);
		/*
		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Sixi2/anchor.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Sixi2/shoulder.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Sixi2/bicep.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Sixi2/forearm.stl",0.1f);
			links.get(5).model = ModelFactory.createModelFromFilename("/Sixi2/tuningFork.stl",0.1f);
			links.get(6).model = ModelFactory.createModelFromFilename("/Sixi2/picassoBox.stl",0.1f);
			links.get(7).model = ModelFactory.createModelFromFilename("/Sixi2/hand.stl",0.1f);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels();
		}
		
		try {
			links.get(0).model = ModelFactory.createModelFromFilename("/Prusa i3 MK3/Prusa0.stl",0.1f);
			links.get(1).model = ModelFactory.createModelFromFilename("/Prusa i3 MK3/Prusa1.stl",0.1f);
			links.get(2).model = ModelFactory.createModelFromFilename("/Prusa i3 MK3/Prusa2.stl",0.1f);
			links.get(3).model = ModelFactory.createModelFromFilename("/Prusa i3 MK3/Prusa3.stl",0.1f);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		links.get(0).theta=90;
		links.get(0).alpha=90;
		links.get(0).model.adjustRotation(new Vector3d(90,0,0));
		links.get(0).model.adjustOrigin(new Vector3d(0,27.9,0));
		links.get(0).rangeMin=0+8.422;
		links.get(0).rangeMax=21+8.422;
		links.get(1).theta=-90;
		links.get(1).alpha=90;
		links.get(2).theta=90;
		links.get(2).alpha=90;
		links.get(1).model.adjustOrigin(new Vector3d(11.2758,-8.422,0));
		links.get(1).model.adjustRotation(new Vector3d(0,-90,0));
		links.get(2).model.adjustOrigin(new Vector3d(32.2679,-9.2891,-27.9));
		links.get(2).model.adjustRotation(new Vector3d(0,0,90));
		links.get(3).model.adjustRotation(new Vector3d(-90,0,0));
		links.get(3).model.adjustOrigin(new Vector3d(0,-31.9,32.2679));
				
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
	
	@Override
	public DHIKSolver getSolverIK() {
		return new DHIKSolver_Cartesian();
	}
	
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

	}
}
