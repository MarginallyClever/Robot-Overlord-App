package com.marginallyclever.communications.transport;

import com.marginallyclever.communications.session.SessionLayer;

import javax.swing.*;
import java.io.Serial;

/**
 * A TransportLayerPanel is a GUI component that allows the user to configure a {@link TransportLayer}.
 *
 * @author Dan Royer
 * @since 1.7.1
 */
public abstract class TransportLayerPanel extends JComponent {
	abstract public SessionLayer openConnection();
}
