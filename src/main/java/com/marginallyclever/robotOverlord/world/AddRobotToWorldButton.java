package com.marginallyclever.robotOverlord.world;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.marginallyclever.robotOverlord.RobotWithConnection;

public class AddRobotToWorldButton extends JMenuItem implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5311068101587322758L;
	
	transient protected RobotWithConnection robot;
	transient protected World world;
	
	public AddRobotToWorldButton(World world, RobotWithConnection robot,String buttonText) {
		super(buttonText);
		this.world=world;
		this.robot=robot;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		try {
			RobotWithConnection r = robot.getClass().newInstance();
			world.addRobot(r);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
