package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

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

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionSelectNumber;

/**
 * Panel to alter a number parameter.  There is no way at present to limit the input options (range, step size, etc)
 * @author Dan Royer
 *
 */
public class UserCommandSelectNumber extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private RobotOverlord ro;
	private DecimalFormat df;
	private float value;
	private String label;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	private boolean allowSetText;
	
	public UserCommandSelectNumber(RobotOverlord ro,String labelName,float defaultValue) {
		super();
		this.ro = ro;
		
		//this.setBorder(BorderFactory.createLineBorder(new Color(255,0,0)));
		
		allowSetText=true;
		df = new DecimalFormat("0.00");
		df.setGroupingUsed(false);
		
		this.label = labelName;
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		
		JLabel label=new JLabel(labelName,JLabel.LEFT);
	
		textField = new JTextField(8);
		textField.getDocument().addDocumentListener(this);
		label.setLabelFor(textField);
		setValue(defaultValue);

		con1.gridx=0;
		con1.gridy=0;
		con1.weighty=1;
		con1.fill=GridBagConstraints.NONE;
		con1.anchor=GridBagConstraints.WEST;
		con1.weightx=0.250;
		con1.gridx=0;
		this.add(label,con1);

		con1.anchor=GridBagConstraints.EAST;
		con1.weightx=0.750;
		con1.gridx=1;
		this.add(textField,con1);
	}
	
	public void setDecimalFormat(DecimalFormat df) {
		this.df = df;
	}
	
	public float getValue() {
		return value;
	}
	
	public void setValue(float v) {
		setValue(v,true);
	}
	
	public void setValue(float v,boolean sendChange) {
		if(value==v) return;
		value = v;
		
		String x;
		if(df != null) x = df.format(v);
		else x = Float.toString(v);
		if(allowSetText) {
			allowSetText=false;
			textField.setText(x);
			allowSetText=true;
			this.updateUI();
		}
		
		if(sendChange) {
			ChangeEvent arg0 = new ChangeEvent(this);
			Iterator<ChangeListener> i = changeListeners.iterator();
			while(i.hasNext()) {
				i.next().stateChanged(arg0);
			}
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
		if(allowSetText==false) return;
		
		float newNumber;
		try {
			newNumber = Float.parseFloat(textField.getText());
		} catch(NumberFormatException e) {
			return;
		}
		if(newNumber != value) {
			allowSetText=false;
			ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectNumber(this, label, newNumber) ) );
			allowSetText=true;
		}
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);
	}
	
	public boolean isReadOnly() {
		return this.textField.isEditable();
	}
	
	public void setReadOnly(boolean arg0) {
		this.textField.setEditable(!arg0);
	}
}
