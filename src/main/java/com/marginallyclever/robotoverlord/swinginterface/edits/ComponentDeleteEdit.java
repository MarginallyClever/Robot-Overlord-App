package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * An undoable action to add a {@link Component} to an {@link Entity}.
 * @author Dan Royer
 *
 */
public class ComponentDeleteEdit extends AbstractUndoableEdit {
	private final Entity entity;
	private final Component component;
	private final RobotOverlord ro;

	public ComponentDeleteEdit(RobotOverlord ro, Entity entity, Component component) {
		super();

		this.ro = ro;
		this.entity = entity;
		this.component = component;
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Delete ")+entity.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();	
	}
	
	protected void doIt() {
		entity.removeComponent(component);
		ro.updateComponentPanel();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.addComponent(component);
		ro.updateComponentPanel();
	}
}
