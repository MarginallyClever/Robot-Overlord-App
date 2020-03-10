package com.marginallyclever.robotOverlord.entity.sixiJoystick;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import com.marginallyclever.convenience.PanelHelper;
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
		
		this.setName("Sixi Joystick");		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

		buttonConnect = new JButton(Translator.get("ButtonConnect"));
		this.add(buttonConnect, con1);
		buttonConnect.addActionListener(this);
		
		PanelHelper.ExpandLastChild(this, con1);
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