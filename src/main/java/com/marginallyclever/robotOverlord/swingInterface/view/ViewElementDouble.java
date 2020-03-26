package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.swingInterface.FocusTextField;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionChangeDouble;

/**
 * Panel to alter a number parameter.  There is no way at present to limit the input options (range, step size, etc)
 * @author Dan Royer
 *
 */
public class ViewElementDouble extends ViewElement implements DocumentListener, Observer {
	private JTextField field;
	private DoubleEntity e;
	private ReentrantLock lock = new ReentrantLock();
	
	public ViewElementDouble(RobotOverlord ro,DoubleEntity e) {
		super(ro);
		this.e=e;
		
		e.addObserver(this);
		
		field = new FocusTextField(8);
		field.getDocument().addDocumentListener(this);
		field.setHorizontalAlignment(SwingConstants.RIGHT);
		field.setText(StringHelper.formatDouble(e.get()));

		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		label.setLabelFor(field);
		
		//this.setBorder(new LineBorder(Color.RED));
		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
	}
	
	/**
	 * panel changed, poke entity
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		if(lock.isLocked()) return;
		lock.lock();
		
		double newNumber;
		
		try {
			newNumber = Double.valueOf(field.getText());
			field.setForeground(UIManager.getColor("Textfield.foreground"));
		} catch(NumberFormatException e1) {
			field.setForeground(Color.RED);
			newNumber = e.get();
		}
		
		if(newNumber != e.get()) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new ActionChangeDouble(e, newNumber) ) );
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
	
	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}

	@Override
	public void update(Observable o, Object arg) {
		if(lock.isLocked()) return;
		lock.lock();
		field.setText(StringHelper.formatDouble((Double)arg));
		lock.unlock();		
	}
}
