package com.marginallyclever.evilOverlord.Spidee;

import java.io.Serializable;

import javax.vecmath.Vector3f;

public class SpideeLocation implements Serializable {
	  /**
	 * 
	 */
	private static final long serialVersionUID = -1812312249185841160L;
	
	Vector3f up = new Vector3f();
	  Vector3f left = new Vector3f();
	  Vector3f forward = new Vector3f();
	  Vector3f pos = new Vector3f();
	  Vector3f relative = new Vector3f();
}
