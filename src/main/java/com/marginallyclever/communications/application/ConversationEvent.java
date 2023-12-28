package com.marginallyclever.communications.application;

/**
 *
 */
public record ConversationEvent(String whoSpoke, String whatWasSaid) {
	@Override
	public String toString() {
		return whoSpoke +": "+whatWasSaid;
	}
}