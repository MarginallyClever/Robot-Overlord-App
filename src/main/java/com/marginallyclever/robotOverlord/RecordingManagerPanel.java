package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@Deprecated
public class RecordingManagerPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JButton buttonRecord;
	public JButton buttonPlay;
	RobotOverlord ro;

	public RecordingManagerPanel(RobotOverlord gui) {
		super();
		ro = gui;
	}
	
	protected void buildPanel() {
		this.removeAll();
		this.setBorder(new EmptyBorder(0,0,0,0));
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		
		buttonRecord=new JButton("Record");
		buttonPlay  =new JButton("Play");
		
		//this.add(buttonRecord, con1);	con1.gridy++;
		//this.add(buttonPlay  , con1);	con1.gridy++;
		//buttonRecord.addActionListener(this);
		//buttonPlay.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		//Object source = e.getSource();
		//if(source == buttonRecord && !RecordingManager.isPlaying()) {
		//	RecordingManager.setRecording(!RecordingManager.isRecording());
		//}
		//if(source == buttonPlay && !RecordingManager.isRecording()) {
		//	RecordingManager.setPlaying(!RecordingManager.isPlaying());
		//}
	}

}
