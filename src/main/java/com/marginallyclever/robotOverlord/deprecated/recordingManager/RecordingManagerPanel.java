package com.marginallyclever.robotOverlord.deprecated.recordingManager;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;

@Deprecated
@SuppressWarnings("unused")
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
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		
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
