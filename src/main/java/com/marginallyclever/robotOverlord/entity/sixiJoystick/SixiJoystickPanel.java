package com.marginallyclever.robotOverlord.entity.sixiJoystick;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.translator.Translator;

/**
 * Control Panel for a DHRobot
 * @author Dan Royer
 *
 */
public class SixiJoystickPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected SixiJoystick joystick;
	protected RobotOverlord ro;

	protected transient JButton buttonConnect;
	
	
	public SixiJoystickPanel(RobotOverlord gui,SixiJoystick robot) {
		this.joystick = robot;
		this.ro = gui;
		
		buildPanel();
	}
	
	protected void buildPanel() {
		this.removeAll();

		this.setName("Sixi Joystick");		
		
		this.setBorder(new EmptyBorder(0,0,0,0));
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.HORIZONTAL;

		buttonConnect = new JButton(Translator.get("ButtonConnect"));
		this.add(buttonConnect, con1);
		con1.gridy++;
		buttonConnect.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == buttonConnect) {
			if (joystick.getConnection() != null) {
				buttonConnect.setText(Translator.get("ButtonConnect"));
				joystick.closeConnection();
			} else {
				joystick.openConnection();
				if (joystick.getConnection() != null) {
					buttonConnect.setText(Translator.get("ButtonDisconnect"));
				}
			}
			return;
		}
	}
}