package com.marginallyclever.robotOverlord.uiElements.undoRedo.commands;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.uiElements.FocusTextField;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.actions.UndoableActionSelectDouble;

/**
 * Panel to alter a number parameter.  There is no way at present to limit the input options (range, step size, etc)
 * @author Dan Royer
 *
 */
public class UserCommandSelectDouble extends JPanel implements DocumentListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private RobotOverlord ro;
	private DoubleEntity e;
	
	public UserCommandSelectDouble(RobotOverlord ro,DoubleEntity e) {
		super();
		this.ro = ro;
		this.e = e;

		textField = new FocusTextField(8);
		textField.getDocument().addDocumentListener(this);
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		textField.setText(StringHelper.formatDouble(e.get()));

		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		label.setLabelFor(textField);
		
		//this.setBorder(new LineBorder(Color.RED));
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(textField,BorderLayout.LINE_END);
	}
	
	/**
	 * panel changed, poke entity
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		double newNumber;
		
		try {
			newNumber = Double.valueOf(textField.getText());
			textField.setForeground(UIManager.getColor("Textfield.foreground"));
		} catch(NumberFormatException e1) {
			textField.setForeground(Color.RED);
			newNumber = e.get();
		}
		
		if(newNumber != e.get()) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectDouble(e, newNumber) ) );
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
	
	public boolean isReadOnly() {
		return this.textField.isEditable();
	}
	
	public void setReadOnly(boolean arg0) {
		this.textField.setEditable(!arg0);
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
