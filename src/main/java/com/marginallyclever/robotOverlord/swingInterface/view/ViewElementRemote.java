package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * Panel to alter a network connection parameter.
 * @author Dan Royer
 *
 */
public class ViewElementRemote extends ViewElement implements ActionListener, Observer {
	private JButton field;
	private RemoteEntity e;

	public ViewElementRemote(RobotOverlord ro,RemoteEntity e) {
		super(ro);
		this.e=e;
		
		field = new JButton("Connect");
		field.addActionListener(this);
		panel.setLayout(new BorderLayout());
		panel.add(field,BorderLayout.CENTER);
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
			field.setText(Translator.get("Close"));
		}
	}

	/**
	 * entity has changed, update panel
	 */
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
