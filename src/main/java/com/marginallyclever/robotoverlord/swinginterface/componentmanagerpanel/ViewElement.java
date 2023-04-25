package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * An element in the View
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElement extends JComponent {
	public ViewElement() {
		super();
	}
	
	public void setReadOnly(boolean arg0) {
		// an empty element is already read only.
	}
}
