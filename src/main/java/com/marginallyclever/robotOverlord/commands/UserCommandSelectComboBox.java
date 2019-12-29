package com.marginallyclever.robotOverlord.commands;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.actions.UndoableActionSelectComboBox;

public class UserCommandSelectComboBox extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox<String> list;
	private RobotOverlord ro;
	private int value;
	private String labelName;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	private boolean allowSetText;
	
	public UserCommandSelectComboBox(RobotOverlord ro,String labelName,String [] listOptions,int defaultValue) {
		super();
		this.ro = ro;
		
		allowSetText=true;
		value=defaultValue;
		this.labelName = labelName;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.NONE;
		con1.anchor=GridBagConstraints.WEST;
		
		
		list = new JComboBox<String>(listOptions);
		list.setSelectedIndex(defaultValue);
		list.addActionListener(this);

		JLabel label=new JLabel(labelName,JLabel.LEFT);
		label.setLabelFor(list);

		this.add(label,con1);
		con1.gridy++;

		this.add(list,con1);
		con1.gridy++;
	}
	
	public String getValue() {
		return list.getItemAt(getIndex());
	}
	
	public int getIndex() {
		return list.getSelectedIndex();
	}
	
	public void setIndex(int v) {
		if(allowSetText) {
			allowSetText=false;
			list.setSelectedIndex(v);
			allowSetText=true;
			this.updateUI();
		}
		
		ChangeEvent arg0 = new ChangeEvent(this);
		Iterator<ChangeListener> i = changeListeners.iterator();
		while(i.hasNext()) {
			i.next().stateChanged(arg0);
		}
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		int newIndex = list.getSelectedIndex();
		if(newIndex != value) {
			allowSetText=false;
			ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectComboBox(this, labelName, newIndex) ) );
			allowSetText=true;
		}
		
	}

}
