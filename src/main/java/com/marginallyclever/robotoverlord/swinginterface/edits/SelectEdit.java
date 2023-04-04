package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.Serial;
import java.util.List;

/**
 * An undoable action to change the currently selected entity.
 * This is the equivalent to moving the caret in a text document.
 * @author Dan Royer
 *
 */
public class SelectEdit extends AbstractUndoableEdit {
	@Serial
	private static final long serialVersionUID = 1L;

	private final RobotOverlord ro;
	private final Entity next;
	private final List<Entity> prev;

	public SelectEdit(RobotOverlord ro, List<Entity> prev, Entity next) {
		super();

		this.ro = ro;
		this.next = next;
		this.prev = prev;

		doIt();
	}

	@Override
	public String getPresentationName() {
		String name = (next == null) ? Translator.get("nothing") : next.getName();
		return Translator.get("Select ") + name;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}

	private void doIt() {
		Clipboard.setSelectedEntity(next);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Clipboard.setSelectedEntities(prev);
	}
}
