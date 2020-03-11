package com.marginallyclever.robotOverlord.entity.robot.misc;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.engine.dhRobot.solvers.DHIKSolver_SCARA;
import com.marginallyclever.robotOverlord.entity.material.Material;
import com.marginallyclever.robotOverlord.entity.robot.Robot;
import com.marginallyclever.robotOverlord.entity.robot.RobotKeyframe;

/**
 * FANUC cylindrical coordinate robot GMF M-100
 * @author Dan Royer
 *
 */
public class Robot_SCARA_NM extends Robot {
	public transient boolean isFirstTime;
	protected DHRobot live;
	
	public Robot_SCARA_NM() {
		super();
		setName("SCARA NM");

		live = new DHRobot();
		live.setIKSolver(new DHIKSolver_SCARA());
		setupLinks(live);
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobot robot) {
		robot.setNumLinks(5);

		// roll
		robot.links.get(0).setD(13.784);
		robot.links.get(0).setR(15);
		robot.links.get(0).flags = LinkAdjust.THETA;
		robot.links.get(0).setRangeMin(-40);
		robot.links.get(0).setRangeMax(240);
		
		// roll
		robot.links.get(1).setR(13.0);
		robot.links.get(1).flags = LinkAdjust.THETA;		
		robot.links.get(1).setRangeMin(-120);
		robot.links.get(1).setRangeMax(120);
		// slide
		robot.links.get(2).setD(-8);
		robot.links.get(2).flags = LinkAdjust.D;
		live.links.get(2).setRangeMax(-10.92600+7.574);
		live.links.get(2).setRangeMin(-10.92600-0.5);//-18.5+7.574;
		// roll
		robot.links.get(3).flags = LinkAdjust.THETA;
		robot.links.get(3).setRangeMin(-180);
		robot.links.get(3).setRangeMax(180);

		robot.links.get(4).flags = LinkAdjust.NONE;
		robot.links.get(4).setRangeMin(0);
		robot.links.get(4).setRangeMax(0);
		
		robot.refreshPose();
	}

	public void setupModels(DHRobot robot) {
		try {
			robot.links.get(0).setModelFilename("/SCARA_NM/Scara_base.stl");
			robot.links.get(1).setModelFilename("/SCARA_NM/Scara_arm1.stl");
			robot.links.get(2).setModelFilename("/SCARA_NM/Scara_arm2.stl");
			robot.links.get(4).setModelFilename("/SCARA_NM/Scara_screw.stl");
			
			robot.links.get(0).getModel().adjustOrigin(new Vector3d(-8,0,0));
			robot.links.get(1).getModel().adjustOrigin(new Vector3d(-15,8,-13.784));
			robot.links.get(1).getModel().adjustRotation(new Vector3d(0,0,-90));

			robot.links.get(2).getModel().adjustOrigin(new Vector3d(-13,8,-13.784));
			robot.links.get(2).getModel().adjustRotation(new Vector3d(0,0,-90));

			robot.links.get(4).getModel().adjustOrigin(new Vector3d(-8,0,-13.784));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels(live);
		}
		
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.pose);
			
			// Draw models
			float r=0.5f;
			float g=0.5f;
			float b=0.5f;
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
