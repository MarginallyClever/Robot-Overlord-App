package com.marginallyclever.robotOverlord.swingInterface.undoableEdits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class DHToolEdit extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DHRobotModel robot;
	private int newTool;
	private int previousTool;
	
	public DHToolEdit(DHRobotModel robot,int newTool) {
		super();
		
		this.robot = robot;
		this.newTool = newTool;
		this.previousTool = robot.getToolIndex();
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Set tool ")+newTool;//robot.getTool(newTool).getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		robot.setToolIndex(newTool);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		robot.setToolIndex(previousTool);
	}
}
