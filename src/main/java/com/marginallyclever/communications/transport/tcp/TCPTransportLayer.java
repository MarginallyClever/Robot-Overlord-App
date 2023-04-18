package com.marginallyclever.communications.transport.tcp;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.transport.TransportLayer;
import com.marginallyclever.communications.transport.TransportLayerPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lists available TCP connections and opens a connection of that type to a robot
 *
 * @author Dan
 * @since v7.1.0.0
 */
public class TCPTransportLayer implements TransportLayer {
	private static final Logger logger = LoggerFactory.getLogger(TCPTransportLayer.class);

	public TCPTransportLayer() {}

	/**
	 * @return <code>serialConnection</code> if connection successful.
	 *         <code>null</code> on failure.
	 */
	@Override
	public SessionLayer openConnection(String connectionName) {
		/*
		 * // check it logger.info("Validating "+connectionName); InetAddressValidator
		 * validator = new InetAddressValidator();
		 * if(!validator.isValid(connectionName)) {
		 * Log.error("Not a valid IP Address."); return null; }
		 */
		String[] parts = connectionName.split("@");

		logger.info("Connecting to " + parts[parts.length - 1]);
		// if(connectionName.equals(recentPort)) return null;
		TCPSession connection = new TCPSession(this);

		try {
			connection.openConnection(connectionName);
			logger.info("Connect OK");
		} catch (Exception e) {
			logger.info("Connect FAILED");
			e.printStackTrace();
			return null;
		}

		return connection;
	}

	@Override
	public TransportLayerPanel getTransportLayerPanel() {
		return new TCPTransportLayerPanel(this);
	}
}
