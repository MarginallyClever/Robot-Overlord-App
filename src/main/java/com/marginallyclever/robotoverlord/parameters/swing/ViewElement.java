package com.marginallyclever.robotoverlord.parameters.swing;

import javax.swing.*;

/**
 * An element in the View
 *
 */
@Deprecated public class ViewElement extends JPanel {
	public ViewElement() {
		super();

		setOpaque(true);
	}
	
	public void setReadOnly(boolean arg0) {
		// an empty element is already read only.
	}
}
