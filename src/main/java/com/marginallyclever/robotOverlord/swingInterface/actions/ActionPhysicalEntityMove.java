package com.marginallyclever.robotOverlord.swingInterface.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.entity.robotEntity.RobotEntity;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * An undoable command to make a robot move some part relative to itself. For example, a robot arm might move the wrist.
 * <p>
 * This should NOT adjust the position of the robot relative to the world.
 *  
 * @author Dan Royer
 *
 */
@Deprecated
@SuppressWarnings("unused")
public class ActionPhysicalEntityMove extends AbstractUndoableEdit {
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
	
	private RobotEntity robot;
	private int axis;
	private int direction;
	
	/**
	 * 
	 * @param robot which machine
	 * @param axis index of axis
	 * @param direction 1 or -1
	 */
	public ActionPhysicalEntityMove(RobotEntity robot,int axis,int direction) {
		super();
		
		this.robot = robot;
		this.axis = axis;
		this.direction = direction;
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		String name = Translator.get("Move ");
		switch(axis) {
		case ActionPhysicalEntityMove.AXIS_X: name+=" X";  break;
		case ActionPhysicalEntityMove.AXIS_Y: name+=" Y";  break;
		case ActionPhysicalEntityMove.AXIS_Z: name+=" Z";  break;
		case ActionPhysicalEntityMove.AXIS_A: name+=" A";  break;
		case ActionPhysicalEntityMove.AXIS_B: name+=" B";  break;
		case ActionPhysicalEntityMove.AXIS_C: name+=" C";  break;
		case ActionPhysicalEntityMove.AXIS_U: name+=" U";  break;
		case ActionPhysicalEntityMove.AXIS_V: name+=" V";  break;
		case ActionPhysicalEntityMove.AXIS_W: name+=" W";  break;
		}
		if(direction>0) name += "+";
		name += Float.toString(direction);
		return name;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		//robot.move(axis,direction);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		//robot.move(axis,-direction);
	}
}
