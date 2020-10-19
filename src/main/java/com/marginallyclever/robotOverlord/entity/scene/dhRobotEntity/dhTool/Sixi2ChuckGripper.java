package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool;

import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2ChuckGripper extends Sixi2LinearGripper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5463215121829599553L;

	public Sixi2ChuckGripper() {
		super();
		setName("Sixi2 Chuck Gripper");
		setModelFilename("/Sixi2/chuckGripper/base.stl");
		setModelScale(0.1);
		setModelOrigin(0, 0, 3.4);
		leftFinger.setModelFilename("/Sixi2/chuckGripper/a.stl");
		leftFinger.setModelScale(0.1);
		leftFinger.setModelOrigin(0, 0, 3.4);
		rightFinger.setModelFilename("/Sixi2/chuckGripper/b.stl");		
		rightFinger.setModelScale(0.1);
		rightFinger.setModelOrigin(0, 0, 3.4);
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
	}
}
