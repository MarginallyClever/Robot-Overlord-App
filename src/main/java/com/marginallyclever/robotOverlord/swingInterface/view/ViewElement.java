package com.marginallyclever.robotOverlord.swingInterface.view;

import java.util.Observable;

import javax.swing.JPanel;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * an empty element in the view
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElement extends Observable {
	protected RobotOverlord ro;
	public JPanel panel = new JPanel();
	
	public ViewElement(RobotOverlord ro) {
		this.ro=ro;
	}
	
	public void setReadOnly(boolean arg0) {
		// an empty element is already read only.
	}
}
