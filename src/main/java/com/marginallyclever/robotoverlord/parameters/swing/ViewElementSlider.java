package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.IntParameterEdit;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel to alter a color parameter (four float values).
 * @author Dan Royer
 */
public class ViewElementSlider extends ViewElement implements ChangeListener, PropertyChangeListener {
	private final JSlider field = new JSlider();
	private final JLabel value;
	private final IntParameter parameter;
	
	public ViewElementSlider(IntParameter parameter, int top, int bottom) {
		super();
		this.parameter = parameter;

		parameter.addPropertyChangeListener(this);

		field.setMaximum(top);
		field.setMinimum(bottom);
		field.setMinorTickSpacing(1);
		field.setValue(parameter.get());
		field.addChangeListener(this);

		JLabel label = new JLabel(parameter.getName(),JLabel.LEADING);
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

	public void setMaximum(int max) {
		field.setMaximum(max);
	}

	public void setMinimum(int min) {
		field.setMinimum(min);
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
		int oldValue = parameter.get();
		int newValue = field.getValue();
		
		if(newValue!=oldValue) {
			UndoSystem.addEvent(new IntParameterEdit(parameter,newValue));
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
