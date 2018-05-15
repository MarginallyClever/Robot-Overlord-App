package com.marginallyclever.robotOverlord.physicalObject;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.EntityControlPanel;

public abstract class PhysicalObject extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1804941485489224976L;

	private Vector3f position;
	private Vector3f xAxis,yAxis,zAxis;
	private transient PhysicalObjectControlPanel physicalObjectControlPanel;
	
	public PhysicalObject() {
		super();
		
		position = new Vector3f();
		xAxis = new Vector3f();
		yAxis = new Vector3f();
		zAxis = new Vector3f();
	}
	
	
	/**
	 * Get the {@link EntityControlPanel} for this class' superclass, then the physicalObjectControlPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of physicalObjectControlPanels 
	 */
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = new ArrayList<JPanel>();

		physicalObjectControlPanel = new PhysicalObjectControlPanel(gui,this);
		list.add(physicalObjectControlPanel);

		return list;
	}

	// set up the future motion state of the physical object
	public void prepareMove(float dt) {}
	
	// apply the future motion state - make the future into the present
	public void finalizeMove() {}

	public Vector3f getPosition() {		return position;	}
	public Vector3f getXAxis() {		return xAxis;	}
	public Vector3f getYAxis() {		return yAxis;	}
	public Vector3f getZAxis() {		return zAxis;	}
	public void setPosition(Vector3f pos) {		position.set(pos);  if(physicalObjectControlPanel!=null) physicalObjectControlPanel.updateFields();	}
	public void setXAxis(Vector3f pos) {		xAxis.set(pos);  if(physicalObjectControlPanel!=null) physicalObjectControlPanel.updateFields();	}
	public void setYAxis(Vector3f pos) {		yAxis.set(pos);  if(physicalObjectControlPanel!=null) physicalObjectControlPanel.updateFields();	}
	public void setZAxis(Vector3f pos) {		zAxis.set(pos);  if(physicalObjectControlPanel!=null) physicalObjectControlPanel.updateFields();	}
}
