package com.marginallyclever.communications.session;

import java.util.EventObject;

public class SessionLayerEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1727188492088759549L;
	// connection has closed
	public static final int CONNECTION_CLOSED=1;
	// a transport error.  error message is (String)data.
	public static final int TRANSPORT_ERROR=2;
	// connection can accept more data.
	public static final int SEND_BUFFER_EMPTY=3;
	// data has arrived.  Data is (String)data.
	public static final int DATA_AVAILABLE=4;

	public int flag;
	public Object data;
	
	public SessionLayerEvent(Object source, int flag, Object data) {
		super(source);
		this.flag = flag;
		this.data = data;
	}
}
