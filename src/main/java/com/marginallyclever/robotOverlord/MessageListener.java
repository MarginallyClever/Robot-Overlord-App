package com.marginallyclever.robotOverlord;

public interface MessageListener {
	/**
	 * 
	 * @param message
	 * @param sender
	 */
	abstract public void messageEvent(String message,Object sender);
}
