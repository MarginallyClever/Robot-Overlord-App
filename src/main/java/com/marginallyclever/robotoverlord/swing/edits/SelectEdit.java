package com.marginallyclever.robotoverlord.swing.edits;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.List;

/**
 * An undoable action to change the currently selected entity.
 * This is the equivalent to moving the caret in a text document.
 * @author Dan Royer
 *
 */
public class SelectEdit extends AbstractUndoableEdit {
	private final List<Entity> next;
	private final List<Entity> prev;

	public SelectEdit(List<Entity> prev, List<Entity> next) {
		super();

		this.next = next;
		this.prev = prev;

		doIt(next);
	}

	@Override
	public String getPresentationName() {
		String name;
		if(next.size()==1) {
			name = next.get(0).getName();
		} else if(next.size()>1) {
			name = Translator.get("Select.many",String.valueOf(next.size()));
		} else {
			name = Translator.get("Select.nothing");
		}
		return Translator.get("Select.one",name);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		doIt(next);
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		doIt(prev);
	}

	private void doIt(List<Entity> items) {
		Clipboard.setSelectedEntities(items);
	}
}
