package com.marginallyclever.robotOverlord.commands;

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
import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.actions.UndoableActionSelectVector3f;

/**
 * Panel to alter a Vector3f parameter (three float values).
 * @author Dan Royer
 *
 */
public class UserCommandSelectVector3f extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final float EPSILON = 0.001f;
	private static final long serialVersionUID = 1L;
	private JTextField fieldX,fieldY,fieldZ;
	private RobotOverlord ro;
	private DecimalFormat df;
	private Vector3f value;
	private String label;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	private boolean allowSetText;
	
	public UserCommandSelectVector3f(RobotOverlord ro,String labelName,Vector3f defaultValue) {
		super();
		this.ro = ro;
		
		value = new Vector3f(defaultValue);
		this.label = labelName;
		
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
		this.add(label,con1);
		con1.gridy++;
	
		fieldX = addField("X",defaultValue.x,con1);
		fieldY = addField("Y",defaultValue.y,con1);
		fieldZ = addField("Z",defaultValue.z,con1);
	}
	
	private JTextField addField(String labelName,float defaultValue,GridBagConstraints con1) {
		JLabel label = new JLabel(labelName);
		JTextField f = new JTextField(15);
		f.setText(df.format(defaultValue));
		f.getDocument().addDocumentListener(this);
		label.setLabelFor(f);
		con1.weightx=0.250;
		con1.gridx=0;
		this.add(label,con1);
		con1.weightx=0.750;
		con1.gridx=1;
		this.add(f,con1);
		con1.gridy++;
		return f;
	}
	
	public void setDecimalFormat(DecimalFormat df) {
		this.df = df;
	}
	
	public Vector3f getValue() {
		return value;
	}
	
	public void setValue(Vector3f v) {
		if(value.epsilonEquals(v, EPSILON)) return;
		
		value.set(v);		
		setField(fieldX,v.x);
		setField(fieldY,v.y);
		setField(fieldZ,v.z);
		if(allowSetText) {
			this.updateUI();
		}
		
		ChangeEvent arg0 = new ChangeEvent(this);
		Iterator<ChangeListener> i = changeListeners.iterator();
		while(i.hasNext()) {
			i.next().stateChanged(arg0);
		}
	}
	
	private void setField(JTextField field,float value) {
		String x;
		if(df != null) x = df.format(value);
		else x = Float.toString(value);
		if(allowSetText) {
			allowSetText=false;
			field.setText(x);
			allowSetText=true;
		}		
	}
	
	private float getField(String value,float original) {
		try {
			return Float.parseFloat(value);
		} catch(NumberFormatException e) {
			return original;
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
		
		Vector3f newValue = new Vector3f();
		newValue.x = getField(fieldX.getText(),value.x);
		newValue.y = getField(fieldY.getText(),value.y);
		newValue.z = getField(fieldZ.getText(),value.z);

		if(!newValue.epsilonEquals(value, EPSILON)) {
			allowSetText=false;
			ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectVector3f(this, label, newValue) ) );
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
}
