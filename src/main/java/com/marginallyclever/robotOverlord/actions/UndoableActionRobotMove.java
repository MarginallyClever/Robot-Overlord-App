package com.marginallyclever.robotOverlord.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.Translator;
import com.marginallyclever.robotOverlord.robot.Robot;

/**
 * An undoable command to make a robot move some part relative to itself. For example, a robot arm might move the wrist.
 * <p>
 * This should NOT adjust the position of the robot relative to the world.
 *  
 * @author Dan Royer
 *
 */
public class UndoableActionRobotMove extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// enumerate axies for movement commands
	// linear
	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;
	public static final int AXIS_Z = 2;
	// rotation
	public static final int AXIS_U = 3;
	public static final int AXIS_V = 4;
	public static final int AXIS_W = 5;
	// fk
	public static final int AXIS_A = 6;
	public static final int AXIS_B = 7;
	public static final int AXIS_C = 8;
	
	private Robot robot;
	private int axis;
	private int direction;
	
	/**
	 * 
	 * @param robot which machine
	 * @param axis index of axis
	 * @param direction 1 or -1
	 */
	public UndoableActionRobotMove(Robot robot,int axis,int direction) {
		this.robot = robot;
		this.axis = axis;
		this.direction = direction;
		moveNow(direction);
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
		String name = Translator.get("Move ");
		switch(axis) {
		case UndoableActionRobotMove.AXIS_X: name+=" X";  break;
		case UndoableActionRobotMove.AXIS_Y: name+=" Y";  break;
		case UndoableActionRobotMove.AXIS_Z: name+=" Z";  break;
		case UndoableActionRobotMove.AXIS_A: name+=" A";  break;
		case UndoableActionRobotMove.AXIS_B: name+=" B";  break;
		case UndoableActionRobotMove.AXIS_C: name+=" C";  break;
		case UndoableActionRobotMove.AXIS_U: name+=" U";  break;
		case UndoableActionRobotMove.AXIS_V: name+=" V";  break;
		case UndoableActionRobotMove.AXIS_W: name+=" W";  break;
		}
		if(direction>0) name += "+";
		name += Float.toString(direction);
		return name;
	}

	@Override
	public String getRedoPresentationName() {
		return Translator.get("Redo ") + getPresentationName();
	}

	@Override
	public String getUndoPresentationName() {
		return Translator.get("Undo ") + getPresentationName();
	}

	@Override
	public void redo() throws CannotRedoException {
		moveNow(direction);
	}

	@Override
	public void undo() throws CannotUndoException {
		moveNow(-direction);
	}
	
	private void moveNow(int n) {
		robot.move(axis,n);
	}
}
