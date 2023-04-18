package com.marginallyclever.communications.transport;

import com.marginallyclever.communications.session.SessionLayer;

import javax.swing.*;
import java.io.Serial;

public abstract class TransportLayerPanel extends JPanel {
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 8909954928628121764L;

	abstract public SessionLayer openConnection();
}
