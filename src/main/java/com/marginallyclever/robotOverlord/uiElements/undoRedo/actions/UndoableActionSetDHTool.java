package com.marginallyclever.robotOverlord.uiElements.undoRedo.actions;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.dhTool.DHTool;
import com.marginallyclever.robotOverlord.uiElements.translator.Translator;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class UndoableActionSetDHTool extends AbstractUndoableEdit {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DHRobotEntity robot;
	private DHTool newTool;
	private DHTool previousTool;
	
	public UndoableActionSetDHTool(DHRobotEntity robot,DHTool newTool) {
		super();
		
		this.robot = robot;
		this.newTool = newTool;
		this.previousTool = robot.getCurrentTool();
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Set tool ")+newTool.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}
	
	protected void doIt() {
		robot.setTool(newTool);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		robot.setTool(previousTool);
	}
}
