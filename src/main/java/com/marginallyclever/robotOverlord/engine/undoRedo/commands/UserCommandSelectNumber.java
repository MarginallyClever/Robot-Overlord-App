package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionSelectNumber;
import com.marginallyclever.robotOverlord.uiElements.FocusTextField;

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

		allowSetText=true;
		df = new DecimalFormat("0.00");
		df.setGroupingUsed(false);
		
		this.label = labelName;
		
		textField = new FocusTextField(8);
		textField.getDocument().addDocumentListener(this);
		textField.setHorizontalAlignment(SwingConstants.RIGHT);
		setValue(defaultValue);

		JLabel label=new JLabel(labelName,JLabel.LEFT);
		label.setLabelFor(textField);
		label.setBorder(new EmptyBorder(0,0,0,5));
		
		this.setBorder(new EmptyBorder(5,0,5,0));
		//this.setBorder(new LineBorder(Color.RED));
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(textField,BorderLayout.LINE_END);
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
			for( ChangeListener i : changeListeners ) {
				i.stateChanged(arg0);
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
			ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectNumber(this, label, newNumber) ) );
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
