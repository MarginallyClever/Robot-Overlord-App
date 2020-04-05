package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import com.marginallyclever.robotOverlord.swingInterface.CollapsiblePanel;
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
	
	public ViewElementVector3d(RobotOverlord ro,Vector3dEntity e) {
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
		
		fields[0] = addField(e.get().x,p2,"X",gbc);
		fields[1] = addField(e.get().y,p2,"Y",gbc);
		fields[2] = addField(e.get().z,p2,"Z",gbc);
		
		panel.setLayout(new BorderLayout());
		panel.add(p,BorderLayout.CENTER);
	}
	
	private JTextField addField(double value,JPanel parent,String labelName,GridBagConstraints gbc) {
		JTextField field = new FocusTextField(8);
		field.setText(StringHelper.formatDouble(value));
		field.setHorizontalAlignment(JTextField.RIGHT);
		field.getDocument().addDocumentListener(this);

		JLabel label = new JLabel(labelName,JLabel.LEADING);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
		
		parent.add(panel,gbc);

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

	@Override
	public void setReadOnly(boolean arg0) {
		for(int i=0;i<fields.length;++i) {
			fields[i].setEnabled(!arg0);
		}
	}
}
