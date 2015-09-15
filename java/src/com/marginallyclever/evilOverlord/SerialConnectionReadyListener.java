package com.marginallyclever.evilOverlord;




public interface SerialConnectionReadyListener {
	public void serialConnectionReady(SerialConnection arg0);
	public void serialDataAvailable(SerialConnection arg0,String data);
}
