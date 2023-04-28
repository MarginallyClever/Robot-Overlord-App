package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.Serial;

/**
 * An undoable action to add an {@link Entity} to the world.
 * @author Dan Royer
 *
 */
public class EntityAddEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;
	private final Scene scene;
	private final Entity child;
	private final Entity parent;
	
	public EntityAddEdit(Scene scene,Entity parent, Entity child) {
		super();
		this.scene = scene;
		this.child = child;
		this.parent = parent;
		
		doIt();
	}

	@Override
	public String getPresentationName() {
		return Translator.get("Add ")+ child.getName();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();	
	}
	
	protected void doIt() {
		scene.addEntityToParent(child,parent);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		scene.removeEntityFromParent(child,parent);
	}
}
