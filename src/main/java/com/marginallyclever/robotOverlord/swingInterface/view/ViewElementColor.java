package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.swingInterface.CollapsiblePanel;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionChangeColorRGBA;

/**
 * Panel to alter a color parameter (four float values).
 * @author Dan Royer
 */
public class ViewElementColor extends ViewElement implements ChangeListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JSlider [] fields = new JSlider[4];
	private ColorEntity e;
	
	public ViewElementColor(RobotOverlord ro,ColorEntity e) {
		super(ro);
		this.e=e;

		CollapsiblePanel p = new CollapsiblePanel(e.getName());
		JPanel p2 = p.getContentPane();
		p2.setLayout(new GridBagLayout());
		
	    GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx=1;
		gbc.gridx=0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.bottom=5;
		gbc.insets.left=5;
		gbc.insets.right=5;
		
		
		float [] oldValues = e.getFloatArray();
		fields[0] = addField(oldValues[0],p2,"R",gbc);
		fields[1] = addField(oldValues[1],p2,"G",gbc);
		fields[2] = addField(oldValues[2],p2,"B",gbc);
		fields[3] = addField(oldValues[3],p2,"A",gbc);

		this.setLayout(new BorderLayout());
		this.add(p,BorderLayout.CENTER);
	}
	
	private JSlider addField(float value,JPanel parent,String labelName,GridBagConstraints gbc) {
		JSlider field = new JSlider();
		field.setMaximum(255);
		field.setMinimum(0);
		field.setMinorTickSpacing(1);
		field.setValue((int)(value*255));
		field.addChangeListener(this);

		JLabel label = new JLabel(labelName,JLabel.LEADING);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
		
		parent.add(panel,gbc);
		
		return field;
	}
	
	private float getField(int i,float oldValue) {
		return (float)fields[i].getValue()/255.0f;
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

	@Override
	public void stateChanged(ChangeEvent arg0) {
		float [] newValues = new float[fields.length];
		float [] oldValues = ((ColorEntity)e).getFloatArray();
		
		float sum=0;
		for(int i=0;i<fields.length;++i) {
			newValues[i] = getField(i,oldValues[i]);
			sum += Math.abs( newValues[i] - oldValues[i] );
		}

		if(sum>1e-3) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new ActionChangeColorRGBA((ColorEntity)e,newValues) ) );
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		for(int i=0;i<fields.length;++i) {
			fields[i].setEnabled(!arg0);
		}
	}
}
