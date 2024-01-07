package com.marginallyclever.communications.application;

/**
 * {@link ConversationEvent} is a simple record to hold a conversation event.
 * @param whoSpoke the name of the person who spoke
 * @param whatWasSaid what was said
 */
public record ConversationEvent(String whoSpoke, String whatWasSaid) {
	@Override
	public String toString() {
		return whoSpoke +": "+whatWasSaid;
	}
}