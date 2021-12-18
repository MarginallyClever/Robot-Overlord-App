package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.UndoSystem;
import com.marginallyclever.robotOverlord.swingInterface.undoableEdits.IntEdit;
import com.marginallyclever.robotOverlord.uiExposedTypes.IntEntity;

/**
 * Panel to alter a color parameter (four float values).
 * @author Dan Royer
 */
public class ViewElementSlider extends ViewElement implements ChangeListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9116526652018866486L;
	private JSlider field;
	private JLabel value;
	private IntEntity e;
	
	public ViewElementSlider(RobotOverlord ro,IntEntity e,int top,int bottom) {
		super(ro);
		this.e=e;

		e.addPropertyChangeListener(this);
		
		field = new JSlider();
		field.setMaximum(top);
		field.setMinimum(bottom);
		field.setMinorTickSpacing(1);
		field.setValue(e.get());
		field.addChangeListener(this);
		field.addFocusListener(this);

		JLabel label = new JLabel(e.getName(),JLabel.LEADING);
		value = new JLabel(Integer.toString(field.getValue()),JLabel.RIGHT);
		Dimension dim = new Dimension(30,1);
		value.setMinimumSize(dim);
		value.setPreferredSize(dim);
		value.setMaximumSize(dim);
		
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.CENTER);
		this.add(value,BorderLayout.LINE_END);
	}

	/**
	 * entity changed, poke panel
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		field.setValue((Integer)evt.getNewValue());
		value.setText(Integer.toString(field.getValue()));
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		int oldValue = e.get();
		int newValue = field.getValue();
		
		if(newValue!=oldValue) {
			AbstractUndoableEdit event = new IntEdit(e,newValue);
			UndoSystem.addEvent(this,event);
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
