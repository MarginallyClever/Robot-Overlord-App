package com.marginallyclever.communications;

/**
 * Use this to listen for activity on a NetworkConnection.
 * @author Dan Royer (dan@marginallyclever.com)
 *
 */
public interface NetworkConnectionListener {
	/**
	 * Transmission of message number 'lineNumber' has failed on a NetworkConnection.
	 * @param arg0  connection involved
	 * @param lineNumber the number of the command line sent that failed to receive.  all messages after this point will also fail until this line is resent.
	 */
	public void lineError(NetworkConnection arg0,int lineNumber);
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
