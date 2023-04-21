package com.marginallyclever.robotoverlord.swinginterface.view;

import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.StringParameterEdit;

import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Panel to alter a {@link ReferenceParameter}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ViewElementReference extends ViewElement implements ActionListener {
	private final JTextField field = new FocusTextField(20);
	private final ReferenceParameter parameter;

	public ViewElementReference(final ReferenceParameter parameter) {
		super();
		this.parameter = parameter;

		field.setEditable(false);
		field.setText(parameter.get());
		field.setMargin(new Insets(1,0,1,0));
		
		JLabel label=new JLabel(parameter.getName(),JLabel.LEADING);
		label.setLabelFor(field);

		JButton choose = new JButton("...");
		choose.addActionListener(this);
		choose.setMargin(new Insets(0, 5, 0, 5));
		choose.addFocusListener(this);
		
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx=0;
		gbc.gridy=0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets.right=5;
		this.add(label,gbc);
		gbc.weightx=1;
		gbc.insets.left=0;
		gbc.insets.right=0;
		this.add(field,gbc);
		gbc.weightx=0;
		this.add(choose,gbc);
		
		parameter.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				field.setText(parameter.get());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		boolean success = chooser.runDialog();
		if(success) {
			String newFilename = chooser.getSelectedEntity.getUUID();
			AbstractUndoableEdit event = new StringParameterEdit(parameter, newFilename);
			UndoSystem.addEvent(this,event);
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
