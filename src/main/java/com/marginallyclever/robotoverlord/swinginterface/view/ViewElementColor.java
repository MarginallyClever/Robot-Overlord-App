package com.marginallyclever.robotoverlord.swinginterface.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.CollapsiblePanel;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.undoableedits.ColorRGBAEdit;
import com.marginallyclever.robotoverlord.uiexposedtypes.ColorEntity;

/**
 * Panel to alter a color parameter (four float values).
 * @author Dan Royer
 */
public class ViewElementColor extends ViewElement implements ChangeListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 393949606034181281L;
	private JSlider [] fields = new JSlider[4];
	private ColorEntity e;
	
	public ViewElementColor(RobotOverlord ro,ColorEntity e) {
		super(ro);
		this.e=e;

		e.addPropertyChangeListener(this);
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
		
		
		double [] oldValues = e.getDoubleArray();
		fields[0] = addField(oldValues[0],p2,"R",gbc);
		fields[1] = addField(oldValues[1],p2,"G",gbc);
		fields[2] = addField(oldValues[2],p2,"B",gbc);
		fields[3] = addField(oldValues[3],p2,"A",gbc);

		this.setLayout(new BorderLayout());
		this.add(p,BorderLayout.CENTER);
	}
	
	private JSlider addField(double value,JPanel parent,String labelName,GridBagConstraints gbc) {
		JSlider field = new JSlider();
		field.setMaximum(255);
		field.setMinimum(0);
		field.setMinorTickSpacing(1);
		field.setValue((int)(value*255));
		field.addChangeListener(this);
		field.addFocusListener(this);

		JLabel label = new JLabel(labelName,JLabel.LEADING);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
		
		parent.add(panel,gbc);
		
		return field;
	}
	
	private double getField(int i,double oldValue) {
		return (double)fields[i].getValue()/255.0;
	}

	/**
	 * entity changed, poke panel
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {		
		double [] newValues = e.getDoubleArray();
		
		for(int i=0;i<newValues.length;++i) {
			fields[i].setValue((int)(newValues[i]*255.0));
		}
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		double [] newValues = new double[fields.length];
		double [] oldValues = ((ColorEntity)e).getDoubleArray();
		
		float sum=0;
		for(int i=0;i<fields.length;++i) {
			newValues[i] = getField(i,oldValues[i]);
			sum += Math.abs( newValues[i] - oldValues[i] );
		}

		if(sum>1e-3) {
			AbstractUndoableEdit event = new ColorRGBAEdit(e,newValues);
			UndoSystem.addEvent(this,event);
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		for(int i=0;i<fields.length;++i) {
			fields[i].setEnabled(!arg0);
		}
	}
}
