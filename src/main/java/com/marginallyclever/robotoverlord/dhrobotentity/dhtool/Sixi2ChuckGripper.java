package com.marginallyclever.robotoverlord.dhrobotentity.dhtool;

import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Vector3d;

@Deprecated
public class Sixi2ChuckGripper extends Sixi2LinearGripper {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5463215121829599553L;

	public Sixi2ChuckGripper() {
		super();
		setName("Sixi2 Chuck Gripper");
		setShapeFilename("/Sixi2/chuckGripper/base.stl");
		shapeEntity.setShapeScale(0.1);
		shapeEntity.setShapeOrigin(0, 0, 3.4);
		leftFinger.setShapeFilename("/Sixi2/chuckGripper/a.stl");
		leftFinger.setShapeScale(0.1);
		leftFinger.setShapeOrigin(new Vector3d(0, 0, 3.4));
		rightFinger.setShapeFilename("/Sixi2/chuckGripper/b.stl");		
		rightFinger.setShapeScale(0.1);
		rightFinger.setShapeOrigin(new Vector3d(0, 0, 3.4));
		toolTipOffset.setD(13.5);
	}
	
	@Override
	public void getView(ViewPanel view) {
		super.getView(view);
	}
}
