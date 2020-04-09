package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * an empty element in the view
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElementButton extends ViewElement implements ActionListener {
	protected JButton field;
	
	public ViewElementButton(RobotOverlord ro,String label) {
		super(ro);
		
		field = new JButton(label);
		field.addActionListener(this);
		field.addFocusListener(this);
		
		panel.setLayout(new BorderLayout());
		panel.add(field,BorderLayout.CENTER);
	}
	
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		setChanged();
		notifyObservers();
	}
}
