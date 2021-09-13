package com.marginallyclever.communications;

/**
 * Use this to listen for activity on a NetworkSession.
 * @author Dan Royer
 *
 */
public abstract interface NetworkSessionListener {
	/**
	 * see {@code NetworkSessionEvent} for flags
	 * @param evt
	 */
	public void networkSessionEvent(NetworkSessionEvent evt);
}
