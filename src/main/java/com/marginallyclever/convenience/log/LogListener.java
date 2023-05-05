package com.marginallyclever.convenience.log;

/**
 * Interface for listening to log events.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public interface LogListener {
	// decorated HTML message has been logged
	void logEvent(String message);
}