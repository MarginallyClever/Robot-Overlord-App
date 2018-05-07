package com.marginallyclever.robotOverlord.robot;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.robotOverlord.CollapsiblePanel;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.robot.Robot;

public class RobotPanel extends JPanel implements ActionListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4719253861336378906L;
	// connect/rescan/disconnect dialog options
	protected transient JButton buttonConnect;
	protected transient CollapsiblePanel connectionPanel=null;
	
	private Robot robot=null;

	public RobotPanel(RobotOverlord gui,Robot robot) {
		this.robot = robot; 

		connectionPanel = new CollapsiblePanel("Robot with connection");
		JPanel contents =connectionPanel.getContentPane();
		
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTH;
		
        buttonConnect = createButton(Translator.get("ButtonConnect"));

		contents.add(buttonConnect,con1);
		con1.gridy++;
	}
	
	private JButton createButton(String name) {
		JButton b = new JButton(name);
		b.addActionListener(this);
		return b;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if(subject==buttonConnect) {
			if(robot.getConnection()!=null) {
				buttonConnect.setText(Translator.get("ButtonConnect"));
				robot.closeConnection();
			} else {
				robot.openConnection();
				if(robot.getConnection()!=null) {
					buttonConnect.setText(Translator.get("ButtonDisconnect"));
				}
			}
			return;
		}
	}
}
