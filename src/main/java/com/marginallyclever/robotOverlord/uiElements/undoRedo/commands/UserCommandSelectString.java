package com.marginallyclever.robotOverlord.uiElements.undoRedo.commands;

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.uiElements.FocusTextField;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.actions.UndoableActionSelectString;

/**
 * Panel to alter a string parameter.  There is currently no way to limit the length of strings.
 * @author Dan Royer
 *
 */
public class UserCommandSelectString extends JPanel implements DocumentListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private RobotOverlord ro;
	private StringEntity e;
	
	public UserCommandSelectString(RobotOverlord ro,StringEntity e) {
		super();
		this.ro = ro;
		this.e = e;
		
		textField = new FocusTextField(20);
		textField.setText(e.get());
		textField.getDocument().addDocumentListener(this);
		
		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		label.setLabelFor(textField);

		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(textField,BorderLayout.LINE_END);
	}

	/**
	 * panel changed, poke entity
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		String newValue = textField.getText();
		if(!newValue.equals(e.get())) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectString(e, newValue) ) );
		}
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);
	}

	/**
	 * entity changed, poke panel
	 */
	@Override
	public void update(Observable o, Object arg) {
		textField.setText((String)arg);
	}
}
