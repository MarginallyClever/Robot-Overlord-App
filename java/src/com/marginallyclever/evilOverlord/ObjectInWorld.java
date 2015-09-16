package com.marginallyclever.evilOverlord;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.vecmath.Vector3f;


/**
 * an object in the world that can have a gui interface
 * @author danroyer
 *
 */
public class ObjectInWorld
{
	public List<ObjectInWorld> children;
	public Vector3f position;
	
	
	public ObjectInWorld() {
		children = new ArrayList<ObjectInWorld>();
		position = new Vector3f();
	}
	
	
	public JPanel getControlPanel() {
		return null;
	}
}
