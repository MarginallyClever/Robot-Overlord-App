package com.marginallyclever.robotOverlord.entity.robot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marginallyclever.convenience.PanelHelper;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.entity.robot.Robot;

public class RobotPanel extends JPanel implements ActionListener, ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4719253861336378906L;
	// connect/rescan/disconnect dialog options
	protected transient JButton buttonConnect;

	private Robot robot = null;

	public RobotPanel(RobotOverlord gui, Robot robot) {
		super();
		
		this.robot = robot;

		this.setName("Robot");

		buttonConnect = new JButton();
		if(robot.getConnection()!=null && robot.getConnection().isOpen()) {
			buttonConnect.setText(Translator.get("ButtonDisconnect"));
		} else {
			buttonConnect.setText(Translator.get("ButtonConnect"));
		}
		buttonConnect.addActionListener(this);
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();
		this.setBorder(new EmptyBorder(5,5,5,5));
		this.add(buttonConnect, con1);
		PanelHelper.ExpandLastChild(this, con1);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if (subject == buttonConnect) {
			if (robot.getConnection() != null) {
				buttonConnect.setText(Translator.get("ButtonConnect"));
				robot.closeConnection();
			} else {
				robot.openConnection();
				if (robot.getConnection() != null) {
					buttonConnect.setText(Translator.get("ButtonDisconnect"));
				}
			}
			return;
		}
	}
}
