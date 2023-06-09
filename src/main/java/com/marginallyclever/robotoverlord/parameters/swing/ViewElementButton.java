package com.marginallyclever.robotoverlord.parameters.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * an empty element in the componentpanel
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElementButton extends ViewElement {
	protected JButton field;

	// who is listening to me?
	protected final ArrayList<ActionListener> actionListeners = new ArrayList<>();
	
	public void addActionEventListener(ActionListener p) {
		actionListeners.add(p);
	}
	
	public void removeActionEventListener(ActionListener p) {
		actionListeners.remove(p);
	}
	
	private void fireActionEvent(ActionEvent e) {
		for(ActionListener a : actionListeners) {
			a.actionPerformed(e);
		}
	}
	
	
	public ViewElementButton(String label) {
		super();
		
		field = new JButton(label);
		field.addActionListener((e)->{
			fireActionEvent(new ActionEvent(this,e.getID(),e.getActionCommand()));
		});
		
		this.setLayout(new BorderLayout());
		this.add(field,BorderLayout.CENTER);
	}
	
	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
	
	public void setText(String text) {
		field.setText(text);
	}
	
	public String getText() {
		return field.getText();
	}
}
