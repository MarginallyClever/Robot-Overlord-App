package com.marginallyclever.robotoverlord.swinginterface.view;

import com.marginallyclever.robotoverlord.parameters.RemoteEntity;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel to alter a network connection parameter.
 * @author Dan Royer
 *
 */
public class ViewElementRemote extends ViewElement implements ActionListener {
	private final JButton field;
	private final RemoteEntity e;

	public ViewElementRemote(RemoteEntity e) {
		super();
		this.e=e;
		
		field = new JButton(e.isConnectionOpen()?Translator.get("Close"):Translator.get("Connect"));
		field.addActionListener(this);
		field.addFocusListener(this);
		this.setLayout(new BorderLayout());
		this.add(field,BorderLayout.CENTER);
	}

	/**
	 * panel action, update entity
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(e.isConnectionOpen()) {
			e.closeConnection();
			field.setText(Translator.get("Reopen"));
		} else {
			e.openConnection();
			if(e.isConnectionOpen()) {
				field.setText(Translator.get("Close"));
			}
		}
	}


	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
