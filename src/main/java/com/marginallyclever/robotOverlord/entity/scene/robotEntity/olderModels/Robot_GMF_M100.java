package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.MaterialEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_Cylindrical;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;

/**
 * FANUC cylindrical coordinate robot GMF M-100
 * @author Dan Royer
 *
 */
public class Robot_GMF_M100 extends RobotEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8098352986694626519L;
	DHRobotEntity live;

	public Robot_GMF_M100() {
		super();
		setName("FANUC GMF M-100");

		live = new DHRobotEntity();
		live.setIKSolver(new DHIKSolver_Cylindrical());
		setupLinks(live);
	}
	
	protected void setupLinks(DHRobotEntity robot) {
		robot.setNumLinks(5);
		// roll
		robot.links.get(0).flags = LinkAdjust.THETA;
		robot.links.get(0).setRangeMin(-120);
		robot.links.get(0).setRangeMax(120);
		// slide
		robot.links.get(1).setAlpha(90);
		robot.links.get(1).flags = LinkAdjust.D;
		robot.links.get(1).setRangeMin(0);
		robot.links.get(1).setRangeMin(1300);
		// slide
		robot.links.get(2).setAlpha(90);
		robot.links.get(2).flags = LinkAdjust.D;
		robot.links.get(2).setRangeMin(0);
		robot.links.get(2).setRangeMax(1100);
		// roll
		robot.links.get(3).flags = LinkAdjust.THETA;
		robot.links.get(3).setRangeMin(-90);
		robot.links.get(3).setRangeMax(90);

		robot.links.get(4).flags = LinkAdjust.NONE;

		robot.refreshPose();
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
