package com.marginallyclever.communications;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.marginallyclever.communications.serial.SerialTransportLayer;
import com.marginallyclever.communications.tcp.TCPTransportLayer;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Handles requests between the UI and the various transport layers 
 * @author Dan Royer
 *
 */
public class NetworkConnectionManager {
	static private SerialTransportLayer serial = new SerialTransportLayer();
	static private TCPTransportLayer tcp = new TCPTransportLayer();
	
	/**
	 * create a GUI to give the user transport layer options.
	 * @param parent the root gui component
	 * @return a new connection or null.
	 */
	static public NetworkConnection requestNewConnection(Component parent) {
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(Translator.get("Local"), serial.getTransportLayerPanel());
		tabs.addTab(Translator.get("Remote"), tcp.getTransportLayerPanel());

		JPanel top = new JPanel(new BorderLayout());
		top.add(tabs,BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(parent, top, Translator.get("MenuConnect"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			Component c = tabs.getSelectedComponent();
			if(c instanceof TransportLayerPanel) {
				return ((TransportLayerPanel)c).openConnection();
			}
		}
		// cancelled connect
		return null;
	}
}
