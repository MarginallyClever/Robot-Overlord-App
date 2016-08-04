package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;

public class ActionSelectNumber extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField fieldX;
	private RobotOverlord ro;
	private DecimalFormat df;
	private float value;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	private boolean allowSetText;
	
	public ActionSelectNumber(RobotOverlord ro,String labelName,float defaultValue) {
		super();
		this.ro = ro;
		
		allowSetText=true;
		df = new DecimalFormat("0.00");
		df.setGroupingUsed(false);
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.NONE;
		con1.anchor=GridBagConstraints.WEST;
		
		JLabel label=new JLabel(labelName,JLabel.LEFT);
	
		fieldX = new JTextField(15);
		fieldX.getDocument().addDocumentListener(this);
		fieldX.setText(df.format(defaultValue));
		label.setLabelFor(fieldX);
		
		con1.weightx=0.250;
		con1.gridx=0;
		this.add(label,con1);

		con1.weightx=0.750;
		con1.gridx=1;
		this.add(fieldX,con1);
	}
	
	public void setDecimalFormat(DecimalFormat df) {
		this.df = df;
	}
	
	public float getValue() {
		return value;
	}
	
	public void setValue(float v) {
		if(value==v) return;
		value = v;
		
		String x;
		if(df != null) x = df.format(v);
		else x = Float.toString(v);
		if(allowSetText) {
			fieldX.setText(x);
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
	public void changedUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		float newNumber;
		try {
			newNumber = Float.parseFloat(fieldX.getText());
		} catch(NumberFormatException e) {
			return;
		}
		allowSetText=false;
		ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new CommandSelectNumber(this, newNumber) ) );
		allowSetText=true;
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		changedUpdate(arg0);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		changedUpdate(arg0);
	}
}
