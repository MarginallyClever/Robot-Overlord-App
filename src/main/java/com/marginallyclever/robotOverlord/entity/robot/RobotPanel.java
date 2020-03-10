package com.marginallyclever.robotOverlord.entity.robot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
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
		this.robot = robot;

		this.setName("Robot");
		this.setLayout(new GridBagLayout());

		GridBagConstraints con1 = PanelHelper.getDefaultGridBagConstraints();

		buttonConnect = createButton(Translator.get("ButtonConnect"));
		this.add(buttonConnect, con1);
		
		PanelHelper.ExpandLastChild(this, con1);
	}

	private JButton createButton(String name) {
		JButton b = new JButton(name);

		// Font font = new Font("Segoe UI Symbol",
		// Font.PLAIN,b.getFont().getSize());
		// .b.setFont(font);

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
