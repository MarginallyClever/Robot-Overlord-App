package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.componentmanagerpanel.EntityChooser;
import com.marginallyclever.robotoverlord.swing.edits.StringParameterEdit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Panel to alter a {@link ReferenceParameter}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class ViewElementReference extends ViewElement implements ActionListener {
	private final JTextField field = new FocusTextField(20);
	private final ReferenceParameter parameter;
	private final EntityManager entityManager;

	public ViewElementReference(final ReferenceParameter parameter, EntityManager entityManager) {
		super();
		this.parameter = parameter;
		this.entityManager = entityManager;

		field.setEditable(false);
		field.setMargin(new Insets(1,0,1,0));
		
		JLabel label=new JLabel(parameter.getName(),JLabel.LEADING);
		label.setLabelFor(field);

		JButton choose = new JButton("...");
		choose.addActionListener(this);
		choose.setMargin(new Insets(0, 5, 0, 5));
		
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
		String uniqueID = parameter.get();
		if(uniqueID==null || uniqueID.isEmpty()) {
			field.setText("<none>");
			return;
		}

		Entity entity = entityManager.findEntityByUniqueID(uniqueID);
		if(entity==null) {
			field.setText("<missing>");
			return;
		}

		field.setText(entity.getFullPath());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Component source = (Component) e.getSource();
		JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);
		List<Entity> chosen = EntityChooser.runDialog(parentFrame, entityManager.getRoot(),true);
		String newFilename = chosen.isEmpty() ? null : chosen.get(0).getUniqueID();
		UndoSystem.addEvent(new StringParameterEdit(parameter, newFilename));
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
