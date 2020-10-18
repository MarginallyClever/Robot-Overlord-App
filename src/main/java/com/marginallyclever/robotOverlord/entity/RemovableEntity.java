package com.marginallyclever.robotOverlord.entity;

/**
 * This Entity is notified when it is about to be removed.
 * Entities that do not implement this interface cannot be removed through the GUI.
 * @author aggra
 *
 */
public interface RemovableEntity {
	/**
	 * Hey!  You're being removed!
	 */
	public void beingRemoved();
}
