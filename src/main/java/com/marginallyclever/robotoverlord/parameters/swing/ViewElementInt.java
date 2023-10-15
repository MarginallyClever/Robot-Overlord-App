package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.IntParameterEdit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Panel to alter a number parameter.  There is no way at present to limit the input options (range, step size, etc)
 * @author Dan Royer
 *
 */
public class ViewElementInt extends ViewElement implements DocumentListener, PropertyChangeListener {
	private final JTextField field;
	private final IntParameter parameter;
	private final ReentrantLock lock = new ReentrantLock();
	
	public ViewElementInt(IntParameter parameter) {
		super();
		this.parameter = parameter;
		
		parameter.addPropertyChangeListener(this);
		
		field = new FocusTextField(8);
		field.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				conditionalChange();
			}
		});
		field.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {}

			@Override
			public void focusLost(FocusEvent e) {
				conditionalChange();
			}
		});
		field.getDocument().addDocumentListener(this);
		field.setHorizontalAlignment(SwingConstants.RIGHT);
		field.setText(parameter.get().toString());

		JLabel label=new JLabel(parameter.getName(),JLabel.LEADING);
		label.setLabelFor(field);
		
		//this.setBorder(new LineBorder(Color.RED));
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	protected void conditionalChange() {
		int newNumber;
		
		try {
			newNumber = Integer.parseInt(field.getText());
			field.setForeground(UIManager.getColor("Textfield.foreground"));
		} catch(NumberFormatException e1) {
			field.setForeground(Color.RED);
			newNumber = parameter.get();
		}
		
		if(lock.isLocked()) return;
		lock.lock();

		if(newNumber != parameter.get()) {
			UndoSystem.addEvent(new IntParameterEdit(parameter, newNumber));
		}
		lock.unlock();
	}
	
	protected void validateField() {
		try {
			Integer.valueOf(field.getText());
			field.setForeground(UIManager.getColor("Textfield.foreground"));
		} catch(NumberFormatException e1) {
			field.setForeground(Color.RED);
		}
	}
	
	/**
	 * panel changed, poke entity
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		validateField();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		validateField();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		validateField();
	}
	
	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(lock.isLocked()) return;
		lock.lock();
		Integer i = (Integer)evt.getNewValue();
		field.setText(i.toString());
		lock.unlock();
	}
}
