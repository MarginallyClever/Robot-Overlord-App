package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionSelectString;
import com.marginallyclever.robotOverlord.uiElements.FocusTextField;

/**
 * Panel to alter a string parameter.  There is currently no way to limit the length of strings.
 * @author Dan Royer
 *
 */
public class UserCommandSelectString extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private RobotOverlord ro;
	private String value;
	private String labelName;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	private boolean allowSetText;
	
	public UserCommandSelectString(RobotOverlord ro,String labelName,String defaultValue) {
		super();
		this.ro = ro;
		
		allowSetText=true;
		value=defaultValue;
		this.labelName = labelName;

		textField = new FocusTextField(20);
		textField.setText(defaultValue);
		textField.getDocument().addDocumentListener(this);
		
		JLabel label=new JLabel(labelName,JLabel.LEFT);
		label.setLabelFor(textField);
		label.setBorder(new EmptyBorder(0,0,0,5));

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(5,0,5,0));
		this.add(label,BorderLayout.LINE_START);
		this.add(textField,BorderLayout.LINE_END);
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String v) {
		if(value.equals(v)) return;
		value = v;
		
		if(allowSetText) {
			allowSetText=false;
			textField.setText(v);
			allowSetText=true;
			this.updateUI();
		}
		
		ChangeEvent arg0 = new ChangeEvent(this);
		Iterator<ChangeListener> i = changeListeners.iterator();
		while(i.hasNext()) {
			i.next().stateChanged(arg0);
		}
	}
	
	public void addChangeListener(ChangeListener arg0) {
		changeListeners.add(arg0);
	}
	
	public void removeChangeListner(ChangeListener arg0) {
		changeListeners.remove(arg0);
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		if(allowSetText==false) return;
		
		String newValue = textField.getText();
		if(!newValue.equals(value)) {
			allowSetText=false;
			ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectString(this, labelName, newValue) ) );
			allowSetText=true;
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
}
