package com.marginallyclever.robotOverlord.robots.spidee;

import java.io.Serializable;

import javax.vecmath.Vector3d;

public class SpideeLocation implements Serializable {
	  /**
	 * 
	 */
	private static final long serialVersionUID = -1812312249185841160L;
	
	Vector3d up = new Vector3d();
	Vector3d left = new Vector3d();
	Vector3d forward = new Vector3d();
	Vector3d pos = new Vector3d();
	Vector3d relative = new Vector3d();
}
