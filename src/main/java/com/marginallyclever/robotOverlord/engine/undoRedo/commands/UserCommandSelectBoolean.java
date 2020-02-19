package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionSelectBoolean;

/**
 * Panel to alter a boolean parameter.  There is currently no way to limit the length of strings.
 * @author Dan Royer
 *
 */
public class UserCommandSelectBoolean extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JCheckBox checkboxField;
	private RobotOverlord ro;
	private boolean value;
	private String label;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	private boolean allowSetText;
	
	public UserCommandSelectBoolean(RobotOverlord ro,String labelName,boolean defaultValue) {
		super();
		this.ro = ro;
		
		allowSetText=true;
		value=defaultValue;
		this.label = labelName;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.NONE;
		con1.anchor=GridBagConstraints.WEST;
		
		JLabel label=new JLabel(labelName,JLabel.LEFT);
	
		checkboxField = new JCheckBox();
		checkboxField.setSelected(defaultValue);
		checkboxField.addActionListener(this);
		label.setLabelFor(checkboxField);
		
		this.add(label,con1);
		con1.gridy++;
		this.add(checkboxField,con1);
		con1.gridy++;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(boolean v) {
		if(value == v) return;
		value = v;
		
		if(allowSetText) {
			allowSetText=false;
			checkboxField.setSelected(v);
			allowSetText=true;
			this.updateUI();
		}
		
		ChangeEvent arg0 = new ChangeEvent(this);
		Iterator<ChangeListener> i = changeListeners.iterator();
		while(i.hasNext()) {
			i.next().stateChanged(arg0);
		}
	}
	
	public void addChangeListener(ChangeListener arg0) {
		changeListeners.add(arg0);
	}
	
	public void removeChangeListner(ChangeListener arg0) {
		changeListeners.remove(arg0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean newValue = checkboxField.isSelected();
		if(newValue == value) {
			ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectBoolean(this, label, newValue) ) );
		}
		
	}
}
