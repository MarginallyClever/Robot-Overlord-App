package com.marginallyclever.robotoverlord.swinginterface.view;

import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.IntEdit;
import com.marginallyclever.robotoverlord.uiexposedtypes.IntEntity;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.AbstractUndoableEdit;
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
	private final IntEntity e;
	private final ReentrantLock lock = new ReentrantLock();
	
	public ViewElementInt(IntEntity e) {
		super();
		this.e=e;
		
		e.addPropertyChangeListener(this);
		
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
		field.setText(e.get().toString());
		field.addFocusListener(this);

		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		label.setLabelFor(field);
		
		//this.setBorder(new LineBorder(Color.RED));
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	protected void conditionalChange() {
		int newNumber;
		
		try {
			newNumber = Integer.valueOf(field.getText());
			field.setForeground(UIManager.getColor("Textfield.foreground"));
		} catch(NumberFormatException e1) {
			field.setForeground(Color.RED);
			newNumber = e.get();
		}
		
		if(lock.isLocked()) return;
		lock.lock();

		if(newNumber != e.get()) {
			AbstractUndoableEdit event = new IntEdit(e, newNumber);
			UndoSystem.addEvent(this,event);
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
