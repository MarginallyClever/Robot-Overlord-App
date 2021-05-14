package com.marginallyclever.robotOverlord.entity.scene.robots;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_Cylindrical;

/**
 * FANUC cylindrical coordinate robot GMF M-100
 * @author Dan Royer
 *
 */
public class Robot_GMF_M100 extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3788475254620756094L;
	DHRobotModel live;

	public Robot_GMF_M100() {
		super();
		setName("FANUC GMF M-100");

		live = new DHRobotModel();
		live.setIKSolver(new DHIKSolver_Cylindrical());
		setupLinks(live);
	}
	
	protected void setupLinks(DHRobotModel robot) {
		robot.setNumLinks(5);
		// roll
		robot.getLink(0).flags = LinkAdjust.THETA;
		robot.getLink(0).setRangeMin(-120);
		robot.getLink(0).setRangeMax(120);
		// slide
		robot.getLink(1).setAlpha(90);
		robot.getLink(1).flags = LinkAdjust.D;
		robot.getLink(1).setRangeMin(0);
		robot.getLink(1).setRangeMin(1300);
		// slide
		robot.getLink(2).setAlpha(90);
		robot.getLink(2).flags = LinkAdjust.D;
		robot.getLink(2).setRangeMin(0);
		robot.getLink(2).setRangeMax(1100);
		// roll
		robot.getLink(3).flags = LinkAdjust.THETA;
		robot.getLink(3).setRangeMin(-90);
		robot.getLink(3).setRangeMax(90);

		robot.getLink(4).flags = LinkAdjust.NONE;

		robot.refreshDHMatrixes();
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, this.getPose());	
			// Draw models
			float r=1;
			float g=217f/255f;
			float b=33f/255f;
			MaterialEntity mat = new MaterialEntity();
			mat.setDiffuseColor(r,g,b,1);
			mat.render(gl2);
			live.render(gl2);
		gl2.glPopMatrix();
		
		super.render(gl2);
	}

	@Override
	public Memento createKeyframe() {
		// TODO Auto-generated method stub
		return null;
	}
}
