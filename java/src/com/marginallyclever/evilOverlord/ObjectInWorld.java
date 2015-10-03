package com.marginallyclever.evilOverlord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;


/**
 * an object in the world that can have a gui interface
 * @author danroyer
 *
 */
public class ObjectInWorld {
	protected List<ObjectInWorld> children;
	protected Vector3f position;
	protected String displayName;
	protected int pickName;
	
	// unique ids for all objects in the world.  zero is reserved to indicate no object.
	static protected int pickNameCounter=1;
	
	//private JPanel oiwPanel;

	public ObjectInWorld() {
		pickName = pickNameCounter++;
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
	
	public JPanel buildPanel() {
		JPanel sum = new JPanel();
		sum.setLayout(new BoxLayout(sum, BoxLayout.PAGE_AXIS));
		
		ArrayList<JPanel> list = getControlPanels();
		Iterator<JPanel> pi = list.iterator();
		while(pi.hasNext()) {
			JPanel p = pi.next();
			sum.add(p);
		}
		return sum;
	}
	
	
	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public int getPickName() {
		return pickName;
	}
}
