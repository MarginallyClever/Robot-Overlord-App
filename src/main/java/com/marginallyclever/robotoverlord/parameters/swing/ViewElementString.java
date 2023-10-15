package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.StringParameterEdit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Panel to alter a string parameter.  There is currently no way to limit the length of strings.
 * @author Dan Royer
 *
 */
public class ViewElementString extends ViewElement implements DocumentListener, PropertyChangeListener {
	private final JTextField field = new FocusTextField(20);
	private final StringParameter parameter;
	private final ReentrantLock lock = new ReentrantLock();
	
	public ViewElementString(StringParameter parameter) {
		super();
		this.parameter = parameter;

		parameter.addPropertyChangeListener(this);

		field.setText(parameter.get());
		field.getDocument().addDocumentListener(this);
		
		JLabel label=new JLabel(parameter.getName(),JLabel.LEADING);
		label.setLabelFor(field);

		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}

	/**
	 * panel changed, poke entity
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		if(lock.isLocked()) return;
		lock.lock();

		String newValue = field.getText();
		if( !newValue.equals(parameter.get()) ) {
			UndoSystem.addEvent(new StringParameterEdit(parameter, newValue));
		}
		lock.unlock();
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
	public void propertyChange(PropertyChangeEvent evt) {
		
		if(lock.isLocked()) return;
		lock.lock();
		field.setText((String)evt.getNewValue());
		lock.unlock();
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);		
	}
}
