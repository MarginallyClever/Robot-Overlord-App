package com.marginallyclever.communications;

import javax.swing.JPanel;

public abstract class TransportLayerPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8909954928628121764L;

	abstract public NetworkSession openConnection();
}
