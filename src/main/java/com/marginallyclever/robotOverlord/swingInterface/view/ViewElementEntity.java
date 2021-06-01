package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionChangeString;
import com.marginallyclever.robotOverlord.swingInterface.entityTreePanel.EntityTreePanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.StringEntity;

public class ViewElementEntity extends ViewElement implements ActionListener {
	private JTextField field;
	private StringEntity e;
	
	public ViewElementEntity(RobotOverlord ro,StringEntity e) {
		super(ro);
		this.e=e;
			
		field = new JTextField(15);
		field.setEditable(false);
		field.setText(e.get());
		field.setMargin(new Insets(1,0,1,0));
		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		label.setLabelFor(field);

		JButton choose = new JButton("...");
		choose.addActionListener(this);
		choose.setMargin(new Insets(0, 5, 0, 5));
		choose.addFocusListener(this);
		
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx=0;
		gbc.gridy=0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.insets.right=5;
		panel.add(label,gbc);
		gbc.weightx=1;
		gbc.insets.left=0;
		gbc.insets.right=0;
		panel.add(field,gbc);
		gbc.weightx=0;
		panel.add(choose,gbc);
		
		e.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				field.setText(e.get());
			}
		});
	}

	// Panel action, update entity
	@Override
	public void actionPerformed(ActionEvent arg0) {
		EntityTreePanel treePanel = new EntityTreePanel(ro,false);
		String path = e.get();
		ArrayList<Entity> selected = new ArrayList<>();
		selected.add(ro.findByPath(path));
		treePanel.setSelection(selected);
		
		int returnVal = JOptionPane.showConfirmDialog(null, treePanel,"Choose one",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
		if(returnVal == JOptionPane.OK_OPTION) {
			ArrayList<Entity> subject = treePanel.getSelected();
			String s = (subject == null) ? "" : subject.get(0).getFullPath();
			AbstractUndoableEdit event = new ActionChangeString(e, s);
			if(ro!=null) ro.undoableEditHappened(new UndoableEditEvent(this,event) );
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
