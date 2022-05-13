package com.marginallyclever.robotOverlord.robots.deltaRobot3;

import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.memento.Memento;

import java.io.Serial;


/**
 * Captures the physical state of a robot at a moment in time.
 * @author Dan Royer
 *
 */
public class DeltaRobot3Memento implements Memento {
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 1L;
	
	// Relative to base
	public Vector3d fingerPosition = new Vector3d(0,0,0);
};
