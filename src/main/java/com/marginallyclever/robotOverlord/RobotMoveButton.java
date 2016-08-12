package com.marginallyclever.robotOverlord;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.event.UndoableEditEvent;

import com.marginallyclever.robotOverlord.commands.CommandRobotMove;

public class RobotMoveButton extends JButton implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private RobotWithConnection robot;
	private int axis;
	private int direction;
	private UndoHelper undoHelper;
	
	public RobotMoveButton(UndoHelper undoHelper,RobotWithConnection robot,int axis,int direction,String buttonText) {
		super(buttonText);
		this.robot = robot;
		this.axis = axis;
		this.direction = direction;
		this.undoHelper = undoHelper;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		undoHelper.undoableEditHappened(new UndoableEditEvent(this,new CommandRobotMove( robot, axis, direction ) ) );
	}
}
