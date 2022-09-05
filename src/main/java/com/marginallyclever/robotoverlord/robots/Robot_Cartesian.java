package com.marginallyclever.robotoverlord.robots;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotoverlord.dhrobotentity.DHRobotModel;
import com.marginallyclever.robotoverlord.dhrobotentity.DHLink.LinkAdjust;
import com.marginallyclever.robotoverlord.uiexposedtypes.MaterialEntity;

/**
 * Cartesian 3 axis CNC robot like 3d printer or milling machine.
 * Effectively three prismatic joints.  Use this as an example for other cartesian machines.
 * @author Dan Royer
 *
 */
@Deprecated
public class Robot_Cartesian extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2162728806833383798L;
	public transient boolean isFirstTime;
	public MaterialEntity material;
	DHRobotModel live;
	public Robot_Cartesian() {
		super();
		setName("Cartesian");

		live = new DHRobotModel();
		//live.setIKSolver(new DHIKSolver_Cartesian());
		setupLinks(live);
		
		isFirstTime=true;
	}
	
	protected void setupLinks(DHRobotModel robot) {
		robot.setNumLinks(4);
		// roll
		robot.getLink(0).flags = LinkAdjust.D;
		robot.getLink(0).setRangeMin(0);
		robot.getLink(0).setRangeMax(25);
		robot.getLink(0).setTheta(90);
		robot.getLink(0).setAlpha(90);
		robot.getLink(0).setRangeMin(0+8.422);
		robot.getLink(0).setRangeMax(21+8.422);
		
		// tilt
		robot.getLink(1).setAlpha(90);
		robot.getLink(1).setTheta(-90);
		robot.getLink(1).flags = LinkAdjust.D;
		robot.getLink(1).setRangeMin(0);
		robot.getLink(1).setRangeMax(21);
		// tilt
		robot.getLink(2).setAlpha(90);
		robot.getLink(2).setTheta(90);
		robot.getLink(2).flags = LinkAdjust.D;
		robot.getLink(2).setRangeMin(0+8.422);
		robot.getLink(2).setRangeMax(21+8.422);
		
		robot.getLink(3).flags = LinkAdjust.NONE;

		robot.refreshDHMatrixes();
	}
	
	public void setupModels(DHRobotModel robot) {
		material = new MaterialEntity();
		float r=0.5f;
		float g=0.5f;
		float b=0.5f;
		material.setDiffuseColor(r,g,b,1);

		try {
			robot.getLink(0).setShapeFilename("/Prusa i3 MK3/Prusa0.stl");
			robot.getLink(1).setShapeFilename("/Prusa i3 MK3/Prusa1.stl");
			robot.getLink(2).setShapeFilename("/Prusa i3 MK3/Prusa2.stl");
			robot.getLink(3).setShapeFilename("/Prusa i3 MK3/Prusa3.stl");

			robot.getLink(0).setShapeScale(0.1);
			robot.getLink(1).setShapeScale(0.1);
			robot.getLink(2).setShapeScale(0.1);
			robot.getLink(3).setShapeScale(0.1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		robot.getLink(0).setShapeRotation(new Vector3d(90,0,0));
		robot.getLink(0).setShapeOrigin(new Vector3d(0,27.9,0));
		robot.getLink(1).setShapeOrigin(new Vector3d(11.2758,-8.422,0));
		robot.getLink(1).setShapeRotation(new Vector3d(0,-90,0));
		robot.getLink(2).setShapeOrigin(new Vector3d(32.2679,-9.2891,-27.9));
		robot.getLink(2).setShapeRotation(new Vector3d(0,0,90));
		robot.getLink(3).setShapeRotation(new Vector3d(-90,0,0));
		robot.getLink(3).setShapeOrigin(new Vector3d(0,-31.9,32.2679));	
	}
	
	@Override
	public void render(GL2 gl2) {
		if( isFirstTime ) {
			isFirstTime=false;
			setupModels(live);
		}

		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.getPose());
			material.render(gl2);
			live.render(gl2);
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
	}
*/

	@Override
	public Memento createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
