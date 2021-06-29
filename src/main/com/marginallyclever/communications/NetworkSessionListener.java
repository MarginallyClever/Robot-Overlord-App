package com.marginallyclever.communications;

/**
 * Use this to listen for activity on a NetworkSession.
 * @author Dan Royer
 *
 */
public interface NetworkSessionListener {
	/**
	 * the NetworkSession has experienced a serious error.  
	 * @param arg0 connection involved
	 * @param errorMessage the type of error.
	 */
	public void transportError(NetworkSession arg0,String errorMessage);
	
	/**
	 * The outbound data buffer for a NetworkSession is empty.
	 * @param arg0 connection involved
	 */
	public void sendBufferEmpty(NetworkSession arg0);

	/**
	 * Inbound data has arrived from a NetworkSession. 
	 * @param arg0 connection involved
	 * @param data the data which has arrived
	 */
	public void dataAvailable(NetworkSession arg0,String data);
}
