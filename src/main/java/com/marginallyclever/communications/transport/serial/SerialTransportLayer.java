package com.marginallyclever.communications.transport.serial;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.transport.TransportLayer;
import com.marginallyclever.communications.transport.TransportLayerPanel;
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
			//logger.info("OS X");
		} else if (OS.indexOf("win") >= 0) {
			portsDetected = SerialPortList.getPortNames("COM");
			//logger.info("Windows");
		} else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
			portsDetected = SerialPortList.getPortNames("/dev/");
			//logger.info("Linux/Unix");
		} else {
			Log.error("OS NAME=" + System.getProperty("os.name"));
		}*/
		return portsDetected;
	}

	/**
	 * @return <code>serialConnection</code> if connection successful.  <code>null</code> on failure.
	 */
	@Override
	public SessionLayer openConnection(String connectionName) {
		//if(connectionName.equals(recentPort)) return null;

		SerialSession serialSession = new SerialSession(this);

		try {
			serialSession.openConnection(connectionName);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return serialSession;
	}

	/**
	 * @return a panel with the gui options for this transport layer
	 */
	@Override
	public TransportLayerPanel getTransportLayerPanel() {
		return new SerialTransportLayerPanel(this);
	}
}
