package com.marginallyclever.evilOverlord.communications;


/**
 * Created on 4/12/15.
 *
 * @author Peter Colapietro
 * @since v7
 */
public abstract interface AbstractConnection {
	public void close();
	
	public void open(String connectionName) throws Exception;

	public void reconnect() throws Exception;

	public boolean isConnectionOpen();

	public String getRecentConnection();

	public void sendMessage(String msg) throws Exception;
	
    public void addListener(AbstractConnectionListener listener);
    
    public void removeListener(AbstractConnectionListener listener);
}
