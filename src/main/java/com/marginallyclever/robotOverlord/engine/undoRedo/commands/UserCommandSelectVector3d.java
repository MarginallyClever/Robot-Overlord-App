package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.SpringUtilities;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionSelectVector3d;

/**
 * Panel to alter a Vector3d parameter (three float values).
 * @author Dan Royer
 *
 */
public class UserCommandSelectVector3d extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final float EPSILON = 0.001f;
	private static final long serialVersionUID = 1L;
	private JTextField fieldX,fieldY,fieldZ;
	private RobotOverlord ro;
	private DecimalFormat df;
	private Vector3d value;
	private String label;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	private boolean allowSetText;
	
	public UserCommandSelectVector3d(RobotOverlord ro,String labelName,Vector3d defaultValue) {
		super();
		this.ro = ro;
		
		value = new Vector3d(defaultValue);
		this.label = labelName;
		
		allowSetText=true;
		df = new DecimalFormat("0.00");
		df.setGroupingUsed(false);

		JPanel values = new JPanel();
		values.setLayout(new SpringLayout());
		fieldX = addField("X",defaultValue.x,values);
		fieldY = addField("Y",defaultValue.y,values);
		fieldZ = addField("Z",defaultValue.z,values);
		SpringUtilities.makeCompactGrid(values, 1, 6, 0,0,0,0);

		JLabel label=new JLabel(labelName,JLabel.LEFT);
		label.setBorder(new EmptyBorder(0,0,0,5));

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(5,0,5,0));
		this.add(label,BorderLayout.LINE_START);
		this.add(values,BorderLayout.LINE_END);
	}
	
	private JTextField addField(String labelName,double defaultValue,JPanel values) {
		JLabel label = new JLabel(labelName, JLabel.TRAILING);
		JTextField f = new JTextField(4);
		f.setText(df.format(defaultValue));
		f.setHorizontalAlignment(JTextField.RIGHT);
		f.getDocument().addDocumentListener(this);
		label.setLabelFor(f);
		values.add(label);
		values.add(f);
		label.setBorder(new EmptyBorder(0,5,0,1));
		return f;
	}
	
	public void setDecimalFormat(DecimalFormat df) {
		this.df = df;
	}
	
	public Vector3d getValue() {
		return value;
	}
	
	public void setValue(Vector3d v) {
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
	
	private void setField(JTextField field,double value) {
		String x;
		if(df != null) x = df.format(value);
		else x = Double.toString(value);
		if(allowSetText) {
			allowSetText=false;
			field.setText(x);
			allowSetText=true;
		}		
	}
	
	private double getField(String value,double original) {
		try {
			return Double.parseDouble(value);
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
		
		Vector3d newValue = new Vector3d();
		newValue.x = getField(fieldX.getText(),value.x);
		newValue.y = getField(fieldY.getText(),value.y);
		newValue.z = getField(fieldZ.getText(),value.z);

		if(!newValue.epsilonEquals(value, EPSILON)) {
			allowSetText=false;
			ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectVector3d(this, label, newValue) ) );
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
