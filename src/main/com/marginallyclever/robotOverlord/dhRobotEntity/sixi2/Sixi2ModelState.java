package com.marginallyclever.robotOverlord.dhRobotEntity.sixi2;

import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.robotOverlord.dhRobotEntity.PoseFK;

@Deprecated
public class Sixi2ModelState implements Memento, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8158091152154597786L;
	
	public PoseFK poseFK;
	public int currentTool;
	public Memento toolMemento;
	
	public Sixi2ModelState() {}
}
