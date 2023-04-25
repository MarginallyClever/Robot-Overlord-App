package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentManagerPanel;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.Serial;

/**
 * An undoable action to add a {@link com.marginallyclever.robotoverlord.Component} to an {@link Entity}.
 * @author Dan Royer
 *
 */
public class ComponentAddEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;

	private final Entity entity;
	private final Component component;
	private final ComponentManagerPanel componentManagerPanel;

	public ComponentAddEdit(ComponentManagerPanel componentManagerPanel, Entity entity, Component component) {
		super();

		this.componentManagerPanel = componentManagerPanel;
		this.entity = entity;
		this.component = component;
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Add ")+entity.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();	
	}
	
	protected void doIt() {
		entity.addComponent(component);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		entity.removeComponent(component);
		componentManagerPanel.refreshContentsFromClipboard();
	}
}
