package com.marginallyclever.robotoverlord.swinginterface.view;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.BooleanEdit;
import com.marginallyclever.robotoverlord.uiexposedtypes.BooleanEntity;

/**
 * Panel to alter a boolean parameter.  There is currently no way to limit the length of strings.
 * @author Dan Royer
 *
 */
public class ViewElementBoolean extends ViewElement implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9081079237414488699L;
	private JCheckBox field;
	
	public ViewElementBoolean(final RobotOverlord ro,final BooleanEntity e) {
		super(ro);
		
		e.addPropertyChangeListener(this);
		
		field = new JCheckBox();
		field.setSelected(e.get());
		field.setBorder(new EmptyBorder(0,0,0,0));
		field.addFocusListener(this);
		field.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				boolean newValue = field.isSelected();
				if(e.get()!=newValue) {
					AbstractUndoableEdit event = new BooleanEdit(e, newValue);
					UndoSystem.addEvent(this, event);
				}
			}
		});
		
		JLabel label=new JLabel(e.getName(),SwingConstants.LEFT);
		label.setLabelFor(field);
		
		this.setLayout(new BorderLayout());
		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object o = evt.getSource();
		if(o instanceof BooleanEntity) {
			field.setSelected(((BooleanEntity)o).get());
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		field.setEnabled(!arg0);
	}
}
