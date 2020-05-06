package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
//import com.marginallyclever.robotOverlord.swingInterface.CollapsiblePanel;
import com.marginallyclever.robotOverlord.swingInterface.FocusTextField;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionChangeVector3d;

/**
 * Panel to alter a Vector3d parameter (three float values).
 * @author Dan Royer
 *
 */
public class ViewElementVector3d extends ViewElement implements DocumentListener, Observer {
	private JTextField [] fields = new JTextField[3];
	private Vector3dEntity e;
	private ReentrantLock lock = new ReentrantLock();
	
	public ViewElementVector3d(RobotOverlord ro,Vector3dEntity e) {
		super(ro);
		this.e=e;

		e.addObserver(this);
		
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEADING,0,0));
		
		fields[0] = addField(e.get().x,p2,"X");
		fields[1] = addField(e.get().y,p2,"Y");
		fields[2] = addField(e.get().z,p2,"Z");

		panel.setLayout(new BorderLayout());
		panel.add(new JLabel(e.getName(),JLabel.LEADING),BorderLayout.LINE_START);
		panel.add(p2,BorderLayout.LINE_END);
	}
	
	private JTextField addField(double value,JPanel parent,String labelName) {
		JTextField field = new FocusTextField(6);
		field.setText(StringHelper.formatDouble(value));
		field.setHorizontalAlignment(JTextField.RIGHT);
		field.getDocument().addDocumentListener(this);
		field.addFocusListener(this);

		JLabel label = new JLabel(labelName,JLabel.LEADING);
		label.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		
		parent.add(label);
		parent.add(field);

		return field;
	}
	
	private double getField(int i,double oldValue) {
		try {
			return Double.parseDouble(fields[i].getText());
		} catch(NumberFormatException e) {
			return oldValue;
		}
	}
	
	@Override
	public void changedUpdate(DocumentEvent arg0) {	
		if(lock.isLocked()) return;
		lock.lock();
			
		Vector3d oldValue = e.get(); 
		Vector3d newValue = new Vector3d(
			getField(0,oldValue.x),
			getField(1,oldValue.y),
			getField(2,oldValue.z)
		);

		Vector3d diff = new Vector3d();
		diff.sub(newValue,oldValue);
		
		if(diff.lengthSquared()>1e-6) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new ActionChangeVector3d(e, newValue) ) );
		}
		
		lock.unlock();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		changedUpdate(arg0);
	}

	@Override
	public void update(Observable o, Object arg) {
		if(lock.isLocked()) return;
		lock.lock();
		Vector3d input = (Vector3d)arg;
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
