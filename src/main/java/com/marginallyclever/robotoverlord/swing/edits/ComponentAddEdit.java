package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.swing.componentmanagerpanel.ComponentManagerPanel;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * An undoable action to add a {@link Component} to an {@link Entity}.
 * @author Dan Royer
 *
 */
public class ComponentAddEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;

	private final Entity entity;
	private final Component component;
	private final List<Component> existingDependencies = new ArrayList<>();
	private final ComponentManagerPanel componentManagerPanel;

	public ComponentAddEdit(ComponentManagerPanel componentManagerPanel, Entity entity, Component component) {
		super();

		this.componentManagerPanel = componentManagerPanel;
		this.entity = entity;
		this.component = component;
		// record existing dependencies so we can remove new ones later.
		existingDependencies.addAll(entity.getComponents());
		
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
		// remove dependencies created by this component
		List<Component> difference = new ArrayList<>(entity.getComponents());
		difference.removeAll(existingDependencies);
		for(Component c : difference) entity.removeComponent(c);
		// remove the component
		entity.removeComponent(component);
		componentManagerPanel.refreshContentsFromClipboard();
	}
}
