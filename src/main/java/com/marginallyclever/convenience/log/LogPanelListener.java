package com.marginallyclever.convenience.log;

/**
 * Interface for listening to LogPanel events.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public interface LogPanelListener {
	void commandFromLogPanel(String msg);
}
