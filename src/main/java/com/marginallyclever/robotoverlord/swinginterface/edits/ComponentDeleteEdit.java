package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentManagerPanel;
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
	private final ComponentManagerPanel componentManagerPanel;

	public ComponentDeleteEdit(ComponentManagerPanel componentManagerPanel, Entity entity, Component component) {
		super();

		this.componentManagerPanel = componentManagerPanel;
		this.entity = entity;
		this.component = component;
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("ComponentDeleteEdit.name",entity.getName());
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();	
	}
	
	protected void doIt() {
		entity.removeComponent(component);
		componentManagerPanel.refreshContentsFromClipboard();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.addComponent(component);
		componentManagerPanel.refreshContentsFromClipboard();
	}
}
