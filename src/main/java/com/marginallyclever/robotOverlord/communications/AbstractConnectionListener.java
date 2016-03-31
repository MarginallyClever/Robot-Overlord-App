package com.marginallyclever.robotOverlord.communications;


public abstract interface AbstractConnectionListener {
	public void connectionReady(AbstractConnection arg0);
	public void dataAvailable(AbstractConnection arg0,String data);
}
