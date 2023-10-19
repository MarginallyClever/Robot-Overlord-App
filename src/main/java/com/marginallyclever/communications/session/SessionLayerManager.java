package com.marginallyclever.communications.session;

import com.marginallyclever.communications.transport.TransportLayer;
import com.marginallyclever.communications.transport.TransportLayerPanel;
import com.marginallyclever.communications.transport.serial.SerialTransportLayer;
import com.marginallyclever.communications.transport.tcp.TCPTransportLayer;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.*;

/**
 * Handles requests between the UI and the various transport layers 
 * @author Dan Royer
 *
 */
public class SessionLayerManager {
	static private final TransportLayer serial = new SerialTransportLayer();
	static private final TransportLayer tcp = new TCPTransportLayer();
	static private int selectedLayer=0;
	
	/**
	 * create a GUI to give the user transport layer options.
	 * @param parent the root gui component
	 * @return a new connection or null.
	 */
	static public SessionLayer requestNewSession(Component parent) {
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(Translator.get("Local"), serial.getTransportLayerPanel());
		tabs.addTab(Translator.get("Remote"), tcp.getTransportLayerPanel());
		tabs.setSelectedIndex(selectedLayer);
		
		JPanel top = new JPanel(new BorderLayout());
		top.add(tabs,BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(parent, top, Translator.get("MenuConnect"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			Component c = tabs.getSelectedComponent();
			selectedLayer = tabs.getSelectedIndex();
			if(c instanceof TransportLayerPanel) {
				return ((TransportLayerPanel)c).openConnection();
			}
		}
		// cancelled connect
		return null;
	}
}
