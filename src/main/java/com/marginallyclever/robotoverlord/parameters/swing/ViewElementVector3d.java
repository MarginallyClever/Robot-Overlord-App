package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.robotoverlord.parameters.Vector3DParameter;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.Vector3dParameterEdit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Panel to alter a Vector3d parameter (three float values).
 * @author Dan Royer
 *
 */
public class ViewElementVector3d extends ViewElement implements DocumentListener, PropertyChangeListener {
	private final JTextField [] fields = new JTextField[3];
	private final Vector3DParameter parameter;
	private final ReentrantLock lock = new ReentrantLock();
	
	public ViewElementVector3d(Vector3DParameter parameter) {
		super();
		this.parameter = parameter;

		parameter.addPropertyChangeListener(this);
		
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));
		
		fields[0] = addField(parameter.get().x,p2,"X");
		fields[1] = addField(parameter.get().y,p2,"Y");
		fields[2] = addField(parameter.get().z,p2,"Z");

		this.setLayout(new BorderLayout());
		this.add(new JLabel(parameter.getName(),JLabel.LEADING),BorderLayout.LINE_START);
		this.add(p2,BorderLayout.LINE_END);
	}
	
	private JTextField addField(double value,JPanel parent,String labelName) {
		JTextField field = new FocusTextField(6);
		field.addActionListener(new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				conditionalChange();
			}
		});
		field.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {}

			@Override
			public void focusLost(FocusEvent e) {
				conditionalChange();
			}
		});
		field.setText(StringHelper.formatDouble(value));
		field.setHorizontalAlignment(JTextField.RIGHT);
		field.getDocument().addDocumentListener(this);

		JLabel label = new JLabel(labelName,JLabel.LEADING);
		label.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		
		parent.add(label);
		parent.add(field);

		return field;
	}
	
	private double getField(int i,double oldValue) {
		double number;
		try {
			number = Double.parseDouble(fields[i].getText());
			fields[i].setForeground(UIManager.getColor("Textfield.foreground"));
		} catch(NumberFormatException e) {
			number = oldValue;
			fields[i].setForeground(Color.RED);
		}
		return number;
	}
	
	protected void conditionalChange() {
		if(lock.isLocked()) return;
		lock.lock();
			
		Vector3d oldValue = parameter.get();
		Vector3d newValue = new Vector3d(
			getField(0,oldValue.x),
			getField(1,oldValue.y),
			getField(2,oldValue.z)
		);

		Vector3d diff = new Vector3d();
		diff.sub(newValue,oldValue);
		
		if(diff.lengthSquared()>1e-6) {
			UndoSystem.addEvent(new Vector3dParameterEdit(parameter, newValue));
		}
		
		lock.unlock();
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {	
		Vector3d oldValue = parameter.get();
		getField(0,oldValue.x);
		getField(1,oldValue.y);
		getField(2,oldValue.z);
	}
	
	@Override
	public void insertUpdate(DocumentEvent arg0) {
		Vector3d oldValue = parameter.get();
		getField(0,oldValue.x);
		getField(1,oldValue.y);
		getField(2,oldValue.z);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		Vector3d oldValue = parameter.get();
		getField(0,oldValue.x);
		getField(1,oldValue.y);
		getField(2,oldValue.z);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object o = evt.getSource();
		
		if(lock.isLocked()) return;
		lock.lock();
		Vector3d input = ((Vector3DParameter)o).get();
		fields[0].setText(StringHelper.formatDouble(input.x));
		fields[1].setText(StringHelper.formatDouble(input.y));
		fields[2].setText(StringHelper.formatDouble(input.z));
		lock.unlock();		
	}

	@Override
	public void setReadOnly(boolean arg0) {
		for(int i=0;i<fields.length;++i) {
			fields[i].setEnabled(!arg0);
		}
	}
}
