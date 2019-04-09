package com.marginallyclever.robotOverlord;

/**
 * A class which implements this interface is prepared to listen and process JInput input events 
 * @author Dan Royer
 * @see https://github.com/jinput/jinput/wiki/Code-Example
 */
public interface InputListener {
	/**
	 * Override this method to receive input events from JInput.
	 */
	public void inputUpdate();
}
