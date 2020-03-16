package com.marginallyclever.robotOverlord.uiElements.undoRedo.commands;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;

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
import com.marginallyclever.robotOverlord.uiElements.FocusTextField;
import com.marginallyclever.robotOverlord.uiElements.undoRedo.actions.UndoableActionSelectVector3d;

/**
 * Panel to alter a Vector3d parameter (three float values).
 * @author Dan Royer
 *
 */
public class UserCommandSelectVector3d extends JPanel implements DocumentListener, Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTextField [] fields = new JTextField[3];
	private RobotOverlord ro;
	private Vector3dEntity e;
	
	public UserCommandSelectVector3d(RobotOverlord ro,Vector3dEntity e) {
		super();
		this.ro = ro;
		this.e = e; 
		
		JPanel values = new JPanel();
		values.setLayout(new FlowLayout(FlowLayout.TRAILING,0,0));
		fields[0] = addField(e.get().x,values);
		fields[1] = addField(e.get().y,values);
		fields[2] = addField(e.get().z,values);

		JLabel label=new JLabel(e.getName(),JLabel.LEADING);

		this.add(label,BorderLayout.LINE_START);
		this.add(values,BorderLayout.LINE_END);
	}
	
	private JTextField addField(double value,JPanel values) {
		JTextField f = new FocusTextField(4);
		f.setText(StringHelper.formatDouble(value));
		f.setHorizontalAlignment(JTextField.RIGHT);
		f.getDocument().addDocumentListener(this);
		values.add(f);
		return f;
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
		Vector3d oldValue = e.get(); 
		Vector3d newValue = new Vector3d(
			getField(0,oldValue.x),
			getField(1,oldValue.y),
			getField(2,oldValue.z)
		);

		Vector3d diff = new Vector3d();
		diff.sub(newValue,oldValue);
		
		if(diff.lengthSquared()>1e-6) {
			ro.undoableEditHappened(new UndoableEditEvent(this,new UndoableActionSelectVector3d(e, newValue) ) );
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

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
