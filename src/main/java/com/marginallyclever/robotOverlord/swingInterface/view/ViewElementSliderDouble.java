package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.UndoSystem;
import com.marginallyclever.robotOverlord.swingInterface.undoableEdits.DoubleEdit;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;

/**
 * Panel to alter a color parameter (four float values).
 * @author Dan Royer
 */
public class ViewElementSliderDouble extends ViewElement implements ChangeListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5637079548326079275L;
	private JSlider field;
	private JLabel value;
	private DoubleEntity e;
	boolean inUpdate=false;
	
	public ViewElementSliderDouble(RobotOverlord ro,DoubleEntity e,int top,int bottom) {
		super(ro);
		this.e=e;

		e.addPropertyChangeListener(this);
		
		field = new JSlider();
		field.setMaximum(top*10);
		field.setMinimum(bottom*10);
		field.setMinorTickSpacing(1);
		field.setValue((int)Math.floor(e.get()*10));
		field.addChangeListener(this);
		field.addFocusListener(this);

		JLabel label = new JLabel(e.getName(),JLabel.LEADING);
		value = new JLabel(Double.toString(field.getValue()/10.0),JLabel.RIGHT);
		Dimension dim = new Dimension(35,1);
		value.setMinimumSize(dim);
		value.setPreferredSize(dim);
		value.setMaximumSize(dim);
		
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.CENTER);
		this.add(value,BorderLayout.LINE_END);
	}

	/**
	 * entity changed, poke panel
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		inUpdate=true;
		if(field != null) {
			field.setValue((int)Math.floor((Double)evt.getNewValue()*10));
			value.setText(Double.toString(field.getValue()/10.0));
		}
		inUpdate=false;
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if(inUpdate) return;
		
		double oldValue = Math.floor(e.get());
		double newValue = field.getValue()/10.0;
		
		if(newValue!=oldValue) {
			AbstractUndoableEdit event = new DoubleEdit(e,newValue);
			UndoSystem.addEvent(this,event);
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
