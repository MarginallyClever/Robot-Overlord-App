package com.marginallyclever.robotOverlord.swingInterface.view;

import javax.swing.JPanel;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * an empty element in the view
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElement extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RobotOverlord ro;
	
	public ViewElement(RobotOverlord ro) {
		this.ro=ro;
	}
	
	public void setReadOnly(boolean arg0) {
		// an empty element is already read only.
	}
}
