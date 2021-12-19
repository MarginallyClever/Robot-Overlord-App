package com.marginallyclever.robotOverlord.physics.ode;

import org.ode4j.ode.DBody;

import com.marginallyclever.robotOverlord.Entity;

public class ODEPhysicsEngine extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2824123731874009996L;

	public DBody createBody() {
		return new DBody();
	}

}
