package com.marginallyclever.communications;

/**
 * Use this to listen for activity on a NetworkConnection.
 * @author Dan Royer (dan@marginallyclever.com)
 *
 */
public interface NetworkTransportListener {
	/**
	 * the transport layer has experienced a serious error.  
	 * @param arg0 connection involved
	 * @param errorMessage the type of error.
	 */
	public void transportError(NetworkConnection arg0,String errorMessage);
	
	/**
	 * The outbound data buffer for a NetworkConnection is empty.
	 * @param arg0 connection involved
	 */
	public void sendBufferEmpty(NetworkConnection arg0);

	/**
	 * Inbound data has arrived from a NetworkConnection. 
	 * @param arg0 connection involved
	 * @param data the data which has arrived
	 */
	public void dataAvailable(NetworkConnection arg0,String data);
}
