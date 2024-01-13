package com.marginallyclever.convenience.log;

/**
 * Interface for listening to log events.
 *
 */
public interface LogListener {
	// decorated HTML message has been logged
	void logEvent(String message);
}