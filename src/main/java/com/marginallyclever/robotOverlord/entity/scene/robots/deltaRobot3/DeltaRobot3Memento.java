package com.marginallyclever.robotOverlord.entity.scene.robots.deltaRobot3;

import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.memento.Memento;


/**
 * Captures the physical state of a robot at a moment in time.
 * @author Dan Royer
 *
 */
@Deprecated
public class DeltaRobot3Memento implements Memento {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Relative to base
	public Vector3d fingerPosition = new Vector3d(0,0,0);
};
