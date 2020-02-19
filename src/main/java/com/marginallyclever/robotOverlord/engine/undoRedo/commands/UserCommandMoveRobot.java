package com.marginallyclever.robotOverlord.engine.undoRedo.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.engine.undoRedo.actions.UndoableActionRobotMove;
import com.marginallyclever.robotOverlord.entity.robot.Robot;

public class UserCommandMoveRobot extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Robot robot;
	private int axis;
	private int direction;
	private RobotOverlord ro;
	
	public UserCommandMoveRobot(RobotOverlord ro,Robot robot,int axis,int direction,String buttonText) {
		super(buttonText);
		this.robot = robot;
		this.axis = axis;
		this.direction = direction;
		this.ro = ro;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		ro.getUndoHelper().undoableEditHappened(new UndoableEditEvent(this,new UndoableActionRobotMove( robot, axis, direction ) ) );
	}
}
