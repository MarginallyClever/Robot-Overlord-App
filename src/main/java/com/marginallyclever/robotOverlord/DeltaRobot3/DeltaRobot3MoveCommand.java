package com.marginallyclever.robotOverlord.DeltaRobot3;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;


public class DeltaRobot3MoveCommand extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// enumerate axies for movement commands
	static final int AXIS_X = 0;
	static final int AXIS_Y = 1;
	static final int AXIS_Z = 2;
	static final int AXIS_A = 3;
	static final int AXIS_B = 4;
	static final int AXIS_C = 5;
	
	private DeltaRobot3 robot;
	private int axis;
	private float amount;
	
	
	public DeltaRobot3MoveCommand(DeltaRobot3 robot,int axis,float amount) {
		this.robot = robot;
		this.axis = axis;
		this.amount = amount;
		moveNow(amount);
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public String getPresentationName() {
		String name = "Move ";
		switch(axis) {
		case DeltaRobot3MoveCommand.AXIS_X: name+=" X";  break;
		case DeltaRobot3MoveCommand.AXIS_Y: name+=" Y";  break;
		case DeltaRobot3MoveCommand.AXIS_Z: name+=" Z";  break;
		case DeltaRobot3MoveCommand.AXIS_A: name+=" A";  break;
		case DeltaRobot3MoveCommand.AXIS_B: name+=" B";  break;
		case DeltaRobot3MoveCommand.AXIS_C: name+=" C";  break;
		}
		if(amount>0) name += "+";
		name += Float.toString(amount);
		return name;
	}

	@Override
	public String getRedoPresentationName() {
		return "Redo " + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return "Undo " + getPresentationName();
	}

	@Override
	public void redo() throws CannotRedoException {
		moveNow(amount);
	}

	@Override
	public void undo() throws CannotUndoException {
		moveNow(-amount);
	}
	
	private void moveNow(float n) {
		switch(axis) {
		case DeltaRobot3MoveCommand.AXIS_X: robot.moveX(n);  break;
		case DeltaRobot3MoveCommand.AXIS_Y: robot.moveY(n);  break;
		case DeltaRobot3MoveCommand.AXIS_Z: robot.moveZ(n);  break;
		case DeltaRobot3MoveCommand.AXIS_A: robot.moveA(n);  break;
		case DeltaRobot3MoveCommand.AXIS_B: robot.moveB(n);  break;
		case DeltaRobot3MoveCommand.AXIS_C: robot.moveC(n);  break;
		}
	}
}
