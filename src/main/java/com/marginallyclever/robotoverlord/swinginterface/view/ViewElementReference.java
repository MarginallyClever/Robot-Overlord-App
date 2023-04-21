package com.marginallyclever.robotoverlord.swinginterface.view;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.StringParameterEdit;

import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel to alter a {@link ReferenceParameter}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ViewElementReference extends ViewElement implements ActionListener {
	private final JTextField field = new FocusTextField(20);
	private final ReferenceParameter parameter;
	private final RobotOverlord robotOverlord;

	public ViewElementReference(final ReferenceParameter parameter, RobotOverlord robotOverlord) {
		super();
		this.parameter = parameter;
		this.robotOverlord = robotOverlord;

		field.setEditable(false);
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
		
		parameter.addPropertyChangeListener((e)->updateFieldText());
		updateFieldText();
	}

	private void updateFieldText() {
		String UUID = parameter.get();
		Entity entity = robotOverlord.getScene().findEntityByUniqueID(UUID);
		if(entity!=null) {
			field.setText(entity.getFullPath());
		} else {
			field.setText("");
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Scene scene = robotOverlord.getScene();
		List<Entity> chosen = EntityChooser.runDialog(robotOverlord.getMainFrame(),scene,true);
		if(!chosen.isEmpty()) {
			String newFilename = chosen.get(0).getUniqueID();
			AbstractUndoableEdit event = new StringParameterEdit(parameter, newFilename);
			UndoSystem.addEvent(this,event);
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
