package com.marginallyclever.robotOverlord.robots.robotArm;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.PoseEntity;

import java.io.Serial;

public class RobotEndEffectorTarget extends PoseEntity {
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = -1115218582722700063L;
	private RobotArmFK arm;
	
	public RobotEndEffectorTarget() {
		this(RobotEndEffectorTarget.class.getSimpleName());
	}
	
	public RobotEndEffectorTarget(String name) {
		super(name);
	}

	public void setArm(RobotArmFK arg0) {
		arm = arg0;
	}
	
	@Override
	public void moveTowards(Matrix4d newWorldPose) {
		if(arm!=null && arm instanceof RobotArmIK) {
			RobotArmIK ik = (RobotArmIK)arm; 
			Matrix4d tcp = new Matrix4d();
			tcp.mul(newWorldPose, ik.getToolCenterPointOffset());
			ik.moveEndEffectorTowards(tcp);
		} else {
			super.moveTowards(newWorldPose);
		}
	}
}
