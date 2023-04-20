package com.marginallyclever.robotoverlord.swinginterface.edits;

import com.marginallyclever.robotoverlord.Entity;
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

	private final List<Entity> next;
	private final List<Entity> prev;

	public SelectEdit(List<Entity> prev, List<Entity> next) {
		super();

		this.next = next;
		this.prev = prev;

		doIt();
	}

	@Override
	public String getPresentationName() {
		String name;
		if(next.size()==1) {
			name = next.get(0).getName();
		} else if(next.size()>1) {
			name = Translator.get("multiple");
		} else {
			name = Translator.get("nothing");
		}
		return Translator.get("Select ") + name;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt();
	}

	private void doIt() {
		Clipboard.setSelectedEntities(next);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Clipboard.setSelectedEntities(prev);
	}
}
