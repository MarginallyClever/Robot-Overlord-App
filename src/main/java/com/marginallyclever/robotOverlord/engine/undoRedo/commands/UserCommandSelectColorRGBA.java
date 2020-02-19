package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionSelectColorRGBA;

/**
 * Panel to alter a Vector3f parameter (three float values).
 * @author Dan Royer
 *
 */
public class UserCommandSelectColorRGBA extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final float EPSILON = 0.001f;
	private static final long serialVersionUID = 1L;
	private JTextField fieldR,fieldG,fieldB,fieldA;
	private RobotOverlord ro;
	private DecimalFormat df;
	private float[] value;
	private String label;
	private LinkedList<ChangeListener> changeListeners = new LinkedList<ChangeListener>();
	private boolean allowSetText;
	
	public UserCommandSelectColorRGBA(RobotOverlord ro,String labelName,float[] defaultValue) {
		super();
		this.ro = ro;
		
		value = defaultValue;
		this.label = labelName;
		
		allowSetText=true;
		df = new DecimalFormat("0.00");
		df.setGroupingUsed(false);
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.NONE;
		con1.anchor=GridBagConstraints.WEST;

		JLabel label=new JLabel(labelName,JLabel.LEFT);
	
		con1.gridx=0;
		con1.ipadx=5;
		this.add(label,con1);
		con1.gridx++;
		con1.ipadx=0;
		con1.weightx=0;
		fieldR = addField("R",defaultValue[0],con1);
		fieldG = addField("G",defaultValue[1],con1);
		fieldB = addField("B",defaultValue[2],con1);
		fieldA = addField("A",defaultValue[3],con1);
		con1.gridy++;
	}
	
	private JTextField addField(String labelName,float defaultValue,GridBagConstraints con1) {
		JLabel label = new JLabel(labelName, JLabel.LEADING);
		JTextField f = new JTextField(3);
		f.setText(df.format(defaultValue));
		Dimension preferredSize = f.getPreferredSize();
		preferredSize.width=20;
		f.setPreferredSize(preferredSize);
		f.setMaximumSize(preferredSize);
		f.getDocument().addDocumentListener(this);
		label.setLabelFor(f);
		con1.insets = new Insets(0, 1, 0, 2);
		this.add(label,con1);
		con1.gridx++;
		this.add(f,con1);
		con1.gridx++;
		con1.insets = new Insets(0, 0, 0, 0);
		return f;
	}
	
	public void setDecimalFormat(DecimalFormat df) {
		this.df = df;
	}
	
	public float[] getValue() {
		return value;
	}

	public boolean isSignificantDifference(float [] v) {
		assert(v.length==value.length);

		int i;
		float sum=0;
		for(i=0;i<v.length;++i) {
			sum+=(value[i]-v[i]);
		}
		return Math.abs(sum)>=EPSILON;  // returns true if different enough.
	}
	
	public void setValue(float [] v) {
		if(!isSignificantDifference(v)) return;
		
		for(int i=0;i<v.length;++i) {
			value[i]=v[i];
		}
		setField(fieldR,v[0]);
		setField(fieldG,v[1]);
		setField(fieldB,v[2]);
		setField(fieldA,v[3]);
		if(allowSetText) {
			this.updateUI();
		}
		
		ChangeEvent arg0 = new ChangeEvent(this);
		Iterator<ChangeListener> ic = changeListeners.iterator();
		while(ic.hasNext()) {
			ic.next().stateChanged(arg0);
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
		
		float [] newValue = new float[4];
		newValue[0] = getField(fieldR.getText(),value[0]);
		newValue[1] = getField(fieldG.getText(),value[1]);
		newValue[2] = getField(fieldB.getText(),value[2]);
		newValue[3] = getField(fieldA.getText(),value[3]);

		if(isSignificantDifference(newValue)) {
			allowSetText=false;
			ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectColorRGBA(this, label, newValue) ) );
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
