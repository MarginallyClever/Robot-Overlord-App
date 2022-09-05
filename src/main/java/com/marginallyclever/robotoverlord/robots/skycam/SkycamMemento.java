package com.marginallyclever.robotoverlord.robots.skycam;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.memento.Memento;

public class SkycamMemento implements Memento {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// relative position of end effector
	public Matrix4d relative;
	// size of skycam
	public Vector3d size;
	
	public SkycamMemento() {
		relative = new Matrix4d();
		size = new Vector3d();
	}
}
