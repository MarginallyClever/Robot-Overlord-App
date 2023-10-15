package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.BooleanParameterEdit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel to alter a boolean parameter.  There is currently no way to limit the length of strings.
 * @author Dan Royer
 *
 */
public class ViewElementBoolean extends ViewElement implements PropertyChangeListener {
	private final JLabel label;
	private final JCheckBox field = new JCheckBox();
	
	public ViewElementBoolean(BooleanParameter parameter) {
		super();
		
		parameter.addPropertyChangeListener(this);

		field.setSelected(parameter.get());
		field.setBorder(new EmptyBorder(0,0,0,0));
		field.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				boolean newValue = field.isSelected();
				if(parameter.get()!=newValue) {
					UndoSystem.addEvent(new BooleanParameterEdit(parameter, newValue));
				}
			}
		});
		
		label=new JLabel(parameter.getName(),SwingConstants.LEFT);
		label.setLabelFor(field);
		
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object o = evt.getSource();
		if(o instanceof BooleanParameter) {
			field.setSelected(((BooleanParameter)o).get());
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}

	public void setLabel(String s) {
		label.setText(s);
	}
}
