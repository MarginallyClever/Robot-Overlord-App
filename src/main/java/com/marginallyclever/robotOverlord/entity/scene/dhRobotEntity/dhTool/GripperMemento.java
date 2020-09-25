package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool;

import com.marginallyclever.convenience.memento.Memento;

public class GripperMemento implements Memento {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public double gripperAngle;
	
	public GripperMemento(double angle) {
		gripperAngle=angle;
	}
}
