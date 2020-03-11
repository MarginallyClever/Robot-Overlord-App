package com.marginallyclever.robotOverlord.engine.dhRobot;

import java.util.ArrayList;

import javax.swing.JPanel;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * DHTool is a model that has a DHLink equivalence.
 * In this way it can perform transforms and have sub-links.
 * @author Dan Royer
 *
 */
public class DHTool extends DHLink {
	// any child of this tool is either a sub-component of this tool or some world object being held by a gripper.
	protected DHToolPanel toolPanel;
	
	public DHTool() {
		super();
		rangeMin=0;
		rangeMax=0;
		flags = LinkAdjust.NONE;
		setName("No Tool");
	}
	
	public void set(DHTool b) {
		super.set(b);
		setName(b.getName());
	}
	

	@Override
	public ArrayList<JPanel> getContextPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanels(gui);
		if(list==null) list = new ArrayList<JPanel>();
		
		toolPanel = new DHToolPanel(gui,this);
		list.add(toolPanel);
		
		return list;
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);
			PrimitiveSolids.drawSphere(gl2, 1);
		gl2.glPopMatrix();
	}
	
	/**
	 * use the keyState to control the tool.
	 * @return true if the robot's pose has been affected.
	 */
	public boolean directDrive() {
		return false;		
	}

	public String getCommand() {
		return "";
	}
	
	public void sendCommand(String str) {}
	
	public void interpolate(double dt) {}
}
