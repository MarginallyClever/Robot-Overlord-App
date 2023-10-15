package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.DoubleParameterEdit;

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
public class ViewElementSliderDouble extends ViewElement implements ChangeListener, PropertyChangeListener {
	private final JSlider field = new JSlider();
	private final JLabel value;
	private final DoubleParameter parameter;
	boolean inUpdate=false;
	
	public ViewElementSliderDouble(DoubleParameter parameter, int top, int bottom) {
		super();
		this.parameter = parameter;

		parameter.addPropertyChangeListener(this);

		field.setMaximum(top*10);
		field.setMinimum(bottom*10);
		field.setMinorTickSpacing(1);
		field.setValue((int)Math.floor(parameter.get()*10));
		field.addChangeListener(this);

		JLabel label = new JLabel(parameter.getName(),JLabel.LEADING);
		value = new JLabel(Double.toString(field.getValue()/10.0),JLabel.RIGHT);
		Dimension dim = new Dimension(35,1);
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
		inUpdate=true;
		if(field != null) {
			field.setValue((int)Math.floor((Double)evt.getNewValue()*10));
			value.setText(Double.toString(field.getValue()/10.0));
		}
		inUpdate=false;
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if(inUpdate) return;
		
		double oldValue = Math.floor(parameter.get());
		double newValue = field.getValue()/10.0;
		
		if(newValue!=oldValue) {
			UndoSystem.addEvent(new DoubleParameterEdit(parameter,newValue));
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
