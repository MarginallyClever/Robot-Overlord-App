package com.marginallyclever.evilOverlord;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;


/**
 * an object in the world that can have a gui interface
 * @author danroyer
 *
 */
public class ObjectInWorld
{
	List<ObjectInWorld> children;
	
	public ObjectInWorld() {
		children = new ArrayList<ObjectInWorld>();
	}
	
	
	public JPanel getControlPanel() {
		return null;
	}
}
