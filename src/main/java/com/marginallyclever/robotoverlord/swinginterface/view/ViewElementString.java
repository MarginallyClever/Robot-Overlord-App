package com.marginallyclever.robotoverlord.swinginterface.view;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.undoableedits.StringEdit;
import com.marginallyclever.robotoverlord.uiexposedtypes.StringEntity;

/**
 * Panel to alter a string parameter.  There is currently no way to limit the length of strings.
 * @author Dan Royer
 *
 */
public class ViewElementString extends ViewElement implements DocumentListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3508707912948161897L;
	private JTextField field;
	private StringEntity e;
	private ReentrantLock lock = new ReentrantLock();
	
	public ViewElementString(RobotOverlord ro,StringEntity e) {
		super(ro);
		this.e=e;

		e.addPropertyChangeListener(this);
		
		field = new FocusTextField(20);
		field.setText(e.get());
		field.getDocument().addDocumentListener(this);
		field.addFocusListener(this);
		
		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
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
		if( !newValue.equals(e.get()) ) {
			AbstractUndoableEdit event = new StringEdit(e, newValue);
			UndoSystem.addEvent(this,event);
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
