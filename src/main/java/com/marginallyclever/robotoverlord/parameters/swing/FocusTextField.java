package com.marginallyclever.robotoverlord.parameters.swing;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * text field that automatically selects all text when it gains focus
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class FocusTextField extends JTextField implements FocusListener {
	public FocusTextField(int i) {
		super(i);
		addFocusListener(this);
	}
	
    @Override
    public void focusGained(FocusEvent e) {
        FocusTextField.this.select(0, getText().length());
    }

    @Override
    public void focusLost(FocusEvent e) {
        FocusTextField.this.select(0, 0);
    }
}
