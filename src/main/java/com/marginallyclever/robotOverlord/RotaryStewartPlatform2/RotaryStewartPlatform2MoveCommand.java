package com.marginallyclever.robotOverlord.RotaryStewartPlatform2;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class RotaryStewartPlatform2MoveCommand extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4515557041260517347L;
	private RotaryStewartPlatform2 robot;
	private int axis;
	private float amount;
	
	public RotaryStewartPlatform2MoveCommand(RotaryStewartPlatform2 robot,int axis,float amount) {
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
		case RotaryStewartPlatform2.AXIS_X: name+=" X";  break;
		case RotaryStewartPlatform2.AXIS_Y: name+=" Y";  break;
		case RotaryStewartPlatform2.AXIS_Z: name+=" Z";  break;
		case RotaryStewartPlatform2.AXIS_U: name+=" U";  break;
		case RotaryStewartPlatform2.AXIS_V: name+=" V";  break;
		case RotaryStewartPlatform2.AXIS_W: name+=" W";  break;
		}
		name+=Float.toString(amount);
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
		case RotaryStewartPlatform2.AXIS_X: robot.moveX(n);  break;
		case RotaryStewartPlatform2.AXIS_Y: robot.moveY(n);  break;
		case RotaryStewartPlatform2.AXIS_Z: robot.moveZ(n);  break;
		case RotaryStewartPlatform2.AXIS_U: robot.moveU(n);  break;
		case RotaryStewartPlatform2.AXIS_V: robot.moveV(n);  break;
		case RotaryStewartPlatform2.AXIS_W: robot.moveW(n);  break;
		}
	}
}
