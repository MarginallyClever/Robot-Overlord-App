package com.marginallyclever.communications.serial;

import com.marginallyclever.communications.NetworkConnection;
import com.marginallyclever.communications.TransportLayer;
import com.marginallyclever.communications.TransportLayerPanel;
import jssc.SerialPortList;


/**
 * Lists available serial connections and opens a connection of that type
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class SerialTransportLayer implements TransportLayer {
	private String[] portsDetected;

	public SerialTransportLayer() {}

	/**
	 * find all available serial ports
	 *
	 * @return a list of port names
	 */
	public String[] listConnections() {
		portsDetected = SerialPortList.getPortNames();/*
		String OS = System.getProperty("os.name").toLowerCase();

		if (OS.indexOf("mac") >= 0) {
			portsDetected = SerialPortList.getPortNames("/dev/");
			//Log.message("OS X");
		} else if (OS.indexOf("win") >= 0) {
			portsDetected = SerialPortList.getPortNames("COM");
			//Log.message("Windows");
		} else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
			portsDetected = SerialPortList.getPortNames("/dev/");
			//Log.message("Linux/Unix");
		} else {
			Log.error("OS NAME=" + System.getProperty("os.name"));
		}*/
		return portsDetected;
	}

	/**
	 * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
	 */
	public NetworkConnection openConnection(String connectionName) {
		//if(connectionName.equals(recentPort)) return null;

		SerialConnection serialConnection = new SerialConnection(this);

		try {
			serialConnection.openConnection(connectionName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return serialConnection;
	}

	/**
	 * @return a panel with the gui options for this transport layer
	 */
	public TransportLayerPanel getTransportLayerPanel() {
		return new SerialTransportLayerPanel(this);
	}
}
