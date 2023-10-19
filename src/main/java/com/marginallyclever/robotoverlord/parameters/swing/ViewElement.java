package com.marginallyclever.robotoverlord.parameters.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * An element in the View
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElement extends JPanel {
	public ViewElement() {
		super();

		setOpaque(true);
	}
	
	public void setReadOnly(boolean arg0) {
		// an empty element is already read only.
	}
}
