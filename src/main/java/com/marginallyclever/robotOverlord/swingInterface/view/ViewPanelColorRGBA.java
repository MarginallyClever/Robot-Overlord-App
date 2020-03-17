package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.swingInterface.FocusTextField;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionChangeColorRGBA;

/**
 * Panel to alter a Vector3f parameter (three float values).
 * @author Dan Royer
 *
 */
public class ViewPanelColorRGBA extends JPanel implements DocumentListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextField [] fields = new JTextField[4];
	private RobotOverlord ro;
	private ColorEntity e;
	
	public ViewPanelColorRGBA(RobotOverlord ro,ColorEntity e) {
		super();
		this.ro = ro;
		this.e = e;
		
		JLabel label=new JLabel(e.getName(),JLabel.LEADING);
		
		JPanel values = new JPanel();
		values.setLayout(new FlowLayout(FlowLayout.TRAILING,0,0));
		
		float [] oldValues = e.getFloatArray();
		for(int i=0;i<oldValues.length;++i) {
			fields[i] = addField(oldValues[i],values);
		}

		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(values,BorderLayout.LINE_END);
	}
	
	private JTextField addField(float value,JPanel values) {
		JTextField f = new FocusTextField(4);
		f.setText(StringHelper.formatFloat(value));
		f.setHorizontalAlignment(SwingConstants.RIGHT);
		Dimension preferredSize = f.getPreferredSize();
		preferredSize.width=20;
		f.setPreferredSize(preferredSize);
		f.setMaximumSize(preferredSize);
		f.getDocument().addDocumentListener(this);
		
		values.add(f);
		
		return f;
	}
	
	private float getField(int i,float oldValue) {
		try {
			return Float.parseFloat(fields[i].getText());
		} catch(NumberFormatException e) {
			//fields[i].setText(StringHelper.formatFloat(oldValue));
			return oldValue;
		}
	}
	
	/**
	 * panel changed, poke entity
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {
		float [] newValues = new float[fields.length];
		float [] oldValues = e.getFloatArray();
		
		float sum=0;
		for(int i=0;i<fields.length;++i) {
			newValues[i] = getField(i,oldValues[i]);
			sum += Math.abs( newValues[i] - oldValues[i] );
		}

		if(sum>1e-3) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new ActionChangeColorRGBA(e,newValues) ) );
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
	
	/**
	 * entity changed, poke panel
	 */
	@Override
	public void update(Observable o, Object arg) {/*
		float [] newValues = e.getFloatArray();
		
		for(int i=0;i<newValues.length;++i) {
			fields[i].getDocument().removeDocumentListener(this);
		}
		
		for(int i=0;i<newValues.length;++i) {
			fields[i].setText(StringHelper.formatFloat(newValues[i]));
		}
		
		for(int i=0;i<newValues.length;++i) {
			fields[i].getDocument().addDocumentListener(this);
		}*/
	}
}
