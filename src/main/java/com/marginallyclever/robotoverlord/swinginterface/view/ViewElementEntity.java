package com.marginallyclever.robotoverlord.swinginterface.view;

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
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanel;
import com.marginallyclever.robotoverlord.swinginterface.edits.StringEdit;
import com.marginallyclever.robotoverlord.uiexposedtypes.StringEntity;

public class ViewElementEntity extends ViewElement implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6416638035565316039L;
	private JTextField field;
	private StringEntity e;
	
	public ViewElementEntity(RobotOverlord ro,final StringEntity e) {
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
		
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx=0;
		gbc.gridy=0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.insets.right=5;
		this.add(label,gbc);
		gbc.weightx=1;
		gbc.insets.left=0;
		gbc.insets.right=0;
		this.add(field,gbc);
		gbc.weightx=0;
		this.add(choose,gbc);
		
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
		EntityTreePanel treePanel = new EntityTreePanel(false);
		treePanel.update(ro.getScene());
		String path = e.get();
		ArrayList<Entity> selected = new ArrayList<Entity>();
		selected.add(ro.findByPath(path));
		treePanel.setSelection(selected);
		
		int returnVal = JOptionPane.showConfirmDialog(null, treePanel,"Choose one",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
		if(returnVal == JOptionPane.OK_OPTION) {
			ArrayList<Entity> subject = treePanel.getSelected();
			String s = (subject == null) ? "" : subject.get(0).getFullPath();
			AbstractUndoableEdit event = new StringEdit(e, s);
			UndoSystem.addEvent(this,event);
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
