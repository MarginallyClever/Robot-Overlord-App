package com.marginallyclever.robotOverlord;

import java.util.ArrayList;

/**
 * Classes that derive this class can send string messages to listeners.
 * @author Dan Royer
 *
 */
public abstract class MessageSender {
	private ArrayList<MessageListener> listeners;

	protected MessageSender() {
		listeners = new ArrayList<MessageListener>();
	}

	protected void finalize() throws Throwable {
		listeners.clear();
		super.finalize();
	}

	public void addListener(MessageListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MessageListener listener) {
		listeners.remove(listener);
	}

	public void sendNewMessage(String message) {
		for( MessageListener l : listeners ) {
			l.messageEvent(message, this);
		}
	}
}
