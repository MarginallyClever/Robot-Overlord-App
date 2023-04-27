package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

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
	private final Scene scene;

	public ViewElementReference(final ReferenceParameter parameter, Scene scene) {
		super();
		this.parameter = parameter;
		this.scene = scene;

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

		Entity entity = scene.findEntityByUniqueID(uniqueID);
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
		List<Entity> chosen = EntityChooser.runDialog(parentFrame,scene,true);
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
