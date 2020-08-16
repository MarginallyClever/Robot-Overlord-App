package com.marginallyclever.robotOverlord.entity.scene.recording;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElement;

public class RecordingViewButtons extends ViewElement implements ActionListener {
	public JButton toStart;
	public JButton keyPrev;
	public JButton playStop;
	public JButton keyNext;
	public JButton toEnd;
	
	
	public RecordingViewButtons(RobotOverlord ro,String label) {
		super(ro);

		toStart  = addButton("|◄");
		keyPrev  = addButton("◄◄");
		playStop = addButton("■");
		keyNext  = addButton("►►");
		toEnd    = addButton("►|");
		
		panel.setLayout(new FlowLayout());
	}
	
	protected JButton addButton(String text) {
		JButton field = new JButton(text);
		field.addActionListener(this);
		field.addFocusListener(this);
		panel.add(field);
		return field;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		setChanged();
		notifyObservers();
	}
}
