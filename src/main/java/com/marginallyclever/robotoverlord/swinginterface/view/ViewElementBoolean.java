package com.marginallyclever.robotoverlord.swinginterface.view;

import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.BooleanEdit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.undo.AbstractUndoableEdit;
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
	private final JCheckBox field = new JCheckBox();
	
	public ViewElementBoolean(BooleanParameter e) {
		super();
		
		e.addPropertyChangeListener(this);

		field.setSelected(e.get());
		field.setBorder(new EmptyBorder(0,0,0,0));
		field.addFocusListener(this);
		field.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				boolean newValue = field.isSelected();
				if(e.get()!=newValue) {
					AbstractUndoableEdit event = new BooleanEdit(e, newValue);
					UndoSystem.addEvent(this, event);
				}
			}
		});
		
		JLabel label=new JLabel(e.getName(),SwingConstants.LEFT);
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
}
