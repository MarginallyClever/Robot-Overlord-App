package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2ChuckGripper extends Sixi2LinearGripper {

	public Sixi2ChuckGripper() {
		super();
		setName("Sixi2 Chuck Gripper");
		setModelFilename("/Sixi2/chuckGripper/base.stl");
		leftFinger.setModelFilename("/Sixi2/chuckGripper/a.stl");
		rightFinger.setModelFilename("/Sixi2/chuckGripper/b.stl");		
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
	}
}
