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
	
	//private JPanel oiwPanel;
	
	
	public ObjectInWorld() {
		children = new ArrayList<ObjectInWorld>();
		position = new Vector3f();
	}
	
	
	public ArrayList<JPanel> getControlPanels() {
		ArrayList<JPanel> list = new ArrayList<JPanel>();
		/*
		oiwPanel = new JPanel(new GridLayout(3,2));
		
		JLabel x=new JLabel("X");
		JTextField px = new JTextField(Float.toString(position.x));
		x.setLabelFor(px);
		oiwPanel.add(x);
		oiwPanel.add(px);
		
		JLabel y=new JLabel("Y");
		JTextField py = new JTextField(Float.toString(position.y));
		y.setLabelFor(py);
		oiwPanel.add(y);
		oiwPanel.add(py);
		
		JLabel z=new JLabel("Z");
		JTextField pz = new JTextField(Float.toString(position.z));
		z.setLabelFor(pz);
		oiwPanel.add(z);
		oiwPanel.add(pz);
		
		list.add(oiwPanel);
		*/
		return list;
	}
}
