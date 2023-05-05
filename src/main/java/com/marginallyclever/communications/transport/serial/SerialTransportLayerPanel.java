package com.marginallyclever.communications.transport.serial;

import com.marginallyclever.communications.session.SessionLayer;
import com.marginallyclever.communications.transport.TransportLayerPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A panel for configuring a serial transport layer.
 *
 * @author Dan Royer
 * @since 1.7.1
 */
public class SerialTransportLayerPanel extends TransportLayerPanel {
	private final SerialTransportLayer layer;
	private final JComboBox<String> connectionPort;
	private final JComboBox<String> connectionBaud;
	
	private static String lastConnectionPort="COM4";
	private static int lastConnectionBaud=250000;

	public static final int [] COMMON_BAUD_RATES = {300,1200,2400,4800,9600,19200,38400,57600,74880,115200,230400,250000,500000,1000000,2000000};
	
	public SerialTransportLayerPanel(SerialTransportLayer serialTransportLayer) {
		this.layer = serialTransportLayer;

		this.setBorder(new EmptyBorder(5,5,5,5));
		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel("Port",JLabel.LEADING));
		add(connectionPort = new JComboBox<String>());
		this.add(new JLabel("Baud",JLabel.LEADING));
		add(connectionBaud = new JComboBox<String>());

		// fill in the ports
	    String [] portsDetected = layer.listConnections();
		int i;
	    for(i=0;i<portsDetected.length;++i) {
	    	connectionPort.addItem(portsDetected[i]);
	    	// set the drop down to the last port used, if possible.
	    	if(portsDetected[i].contentEquals(lastConnectionPort)) {
	        	connectionPort.setSelectedIndex(i);
	    	}
	    }
	    // fill in the baud rates
	    for(i=0;i<COMMON_BAUD_RATES.length;++i) {
	    	connectionBaud.addItem(Integer.toString(COMMON_BAUD_RATES[i]));
	    	// set the drop down to the last port used, if possible.
	    	if(COMMON_BAUD_RATES[i] == lastConnectionBaud) {
	    		connectionBaud.setSelectedIndex(i);
	    	}
	    }
	    this.add(Box.createVerticalGlue());
	}

	@Override
	public SessionLayer openConnection() {
		lastConnectionPort = connectionPort.getItemAt(connectionPort.getSelectedIndex());
		lastConnectionBaud = Integer.parseInt(connectionBaud.getItemAt(connectionBaud.getSelectedIndex()));
		return layer.openConnection(lastConnectionPort + "@" + lastConnectionBaud);
	}
}
