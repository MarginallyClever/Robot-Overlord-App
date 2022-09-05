package com.marginallyclever.robotoverlord.swinginterface.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.undoableedits.ComboBoxEdit;
import com.marginallyclever.robotoverlord.uiexposedtypes.IntEntity;

public class ViewElementComboBox extends ViewElement implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3675061794997239658L;
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

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(0,0,0,1));
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
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
			UndoSystem.addEvent(this,event);
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
