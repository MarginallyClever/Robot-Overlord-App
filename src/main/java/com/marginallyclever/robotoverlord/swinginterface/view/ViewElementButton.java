package com.marginallyclever.robotoverlord.swinginterface.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * an empty element in the view
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElementButton extends ViewElement {
	private static final long serialVersionUID = -1717097303844646955L;
	protected JButton field;

	// who is listening to me?
	protected ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
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
		field.addFocusListener(this);
		
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
