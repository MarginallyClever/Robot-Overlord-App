package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.ComboBoxEdit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A view element that displays a combo box.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ViewElementComboBox extends ViewElement implements ActionListener, PropertyChangeListener {
	private final JComboBox<String> field;
	private final IntParameter parameter;
	
	public ViewElementComboBox(IntParameter parameter, String [] listOptions) {
		super();
		this.parameter = parameter;
		
		parameter.addPropertyChangeListener(this);
		
		field = new JComboBox<>(listOptions);
		field.setSelectedIndex(parameter.get());
		field.addActionListener(this);

		JLabel label=new JLabel(parameter.getName(),JLabel.LEADING);
		label.setLabelFor(field);

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(0,0,0,1));
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	public String getValue() {
		return field.getItemAt(parameter.get());
	}

	/**
	 * I have changed.  poke the entity
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int newIndex = field.getSelectedIndex();
		if(newIndex != parameter.get()) {
			UndoSystem.addEvent(new ComboBoxEdit(parameter, parameter.getName(), newIndex));
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		field.setSelectedIndex((Integer)evt.getNewValue());
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
