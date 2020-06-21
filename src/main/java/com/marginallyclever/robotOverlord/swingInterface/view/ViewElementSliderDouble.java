package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionChangeDouble;

/**
 * Panel to alter a color parameter (four float values).
 * @author Dan Royer
 */
public class ViewElementSliderDouble extends ViewElement implements ChangeListener, Observer {
	private JSlider field;
	private JLabel value;
	private DoubleEntity e;
	boolean inUpdate=false;
	
	public ViewElementSliderDouble(RobotOverlord ro,DoubleEntity e,int top,int bottom) {
		super(ro);
		this.e=e;

		e.addObserver(this);
		
		field = new JSlider();
		field.setMaximum(top);
		field.setMinimum(bottom);
		field.setMinorTickSpacing(1);
		field.setValue((int)Math.floor(e.get()));
		field.addChangeListener(this);
		field.addFocusListener(this);

		JLabel label = new JLabel(e.getName(),JLabel.LEADING);
		value = new JLabel(Integer.toString(field.getValue()),JLabel.RIGHT);
		Dimension dim = new Dimension(30,1);
		value.setMinimumSize(dim);
		value.setPreferredSize(dim);
		value.setMaximumSize(dim);
		
		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.CENTER);
		panel.add(value,BorderLayout.LINE_END);
	}

	/**
	 * entity changed, poke panel
	 */
	@Override
	public void update(Observable o, Object arg) {
		inUpdate=true;
		field.setValue((int)Math.floor((Double)arg));
		value.setText(Integer.toString(field.getValue()));
		inUpdate=false;
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		if(inUpdate) return;
		
		int oldValue = (int)Math.floor(e.get());
		int newValue = field.getValue();
		
		if(newValue!=oldValue) {
			AbstractUndoableEdit event = new ActionChangeDouble(e,(double)newValue);
			if(ro!=null) ro.undoableEditHappened(new UndoableEditEvent(this,event) );
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
