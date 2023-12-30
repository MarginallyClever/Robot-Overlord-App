package com.marginallyclever.communications.session;

/**
 * Use this to listen for activity on a NetworkSession.
 * @author Dan Royer
 *
 */
public interface SessionLayerListener {
	/**
	 * see {@code NetworkSessionEvent} for flags
	 * @param evt
	 */
    void networkSessionEvent(SessionLayerEvent evt);
}
