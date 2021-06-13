package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.undoableEdits.ComboBoxEdit;
import com.marginallyclever.robotOverlord.uiExposedTypes.IntEntity;

public class ViewElementComboBox extends ViewElement implements ActionListener, PropertyChangeListener {
	private JComboBox<String> field;
	private IntEntity e;
	
	public ViewElementComboBox(RobotOverlord ro,IntEntity e,String [] listOptions) {
		super(ro);
		this.e=e;
		
		e.addPropertyChangeListener(this);
		
		field = new JComboBox<String>(listOptions);
		field.setSelectedIndex(e.get());
		field.addActionListener(this);
		field.addFocusListener(this);

		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		label.setLabelFor(field);

		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(0,0,0,1));
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
	}
	
	public String getValue() {
		return field.getItemAt(e.get());
	}

	/**
	 * I have changed.  poke the entity
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int newIndex = field.getSelectedIndex();
		if(newIndex != e.get()) {
			AbstractUndoableEdit event = new ComboBoxEdit(e, e.getName(), newIndex);
			if(ro!=null) ro.undoableEditHappened(new UndoableEditEvent(this,event) );
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
