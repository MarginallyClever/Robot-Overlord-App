package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionChangeBoolean;

/**
 * Panel to alter a boolean parameter.  There is currently no way to limit the length of strings.
 * @author Dan Royer
 *
 */
public class ViewElementBoolean extends ViewElement implements Observer {
	private JCheckBox field;
	
	public ViewElementBoolean(RobotOverlord ro,BooleanEntity e) {
		super(ro);
		
		e.addObserver(this);
		
		field = new JCheckBox();
		field.setSelected(e.get());
		field.setBorder(new EmptyBorder(0,0,0,0));
		field.addFocusListener(this);
		field.addItemListener(new ItemListener() {
			// the panel element has changed.  poke the entity.
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				boolean newValue = field.isSelected();
				if(e.get()!=newValue) {
					AbstractUndoableEdit event = new ActionChangeBoolean(e, newValue);
					if(ro!=null) ro.undoableEditHappened(new UndoableEditEvent(this, event ) );
				}
			}
		});
		
		JLabel label=new JLabel(e.getName(),SwingConstants.LEFT);
		label.setLabelFor(field);
		
		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
	}
	

	/**
	 * entity we are observing has changed.  poke the panel element.
	 */
	@Override
	public void update(Observable o, Object arg) {
		if(o instanceof BooleanEntity) {
			field.setSelected((boolean)arg);
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
