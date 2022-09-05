package com.marginallyclever.robotoverlord.swinginterface.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.uiexposedtypes.RemoteEntity;

/**
 * Panel to alter a network connection parameter.
 * @author Dan Royer
 *
 */
public class ViewElementRemote extends ViewElement implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1720210992376705253L;
	private JButton field;
	private RemoteEntity e;

	public ViewElementRemote(RobotOverlord ro,RemoteEntity e) {
		super(ro);
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
