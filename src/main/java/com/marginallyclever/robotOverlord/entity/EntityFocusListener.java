package com.marginallyclever.robotOverlord.entity;

/**
 * This Entity is notified when it is selected or unselected by the Robot Overlord app.
 * @author aggra
 *
 */
public interface EntityFocusListener {
	public void gainedFocus();
	public void lostFocus();
}
