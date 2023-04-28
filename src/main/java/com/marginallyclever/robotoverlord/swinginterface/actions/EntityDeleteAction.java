package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.swinginterface.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityDeleteEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Dan Royer
 */
public class EntityDeleteAction extends AbstractAction implements EditorAction {
	private final Scene scene;

	public EntityDeleteAction(Scene scene) {
		super(Translator.get("EntityDeleteAction.name"));
		this.scene = scene;
		putValue(SMALL_ICON,new UnicodeIcon("-"));
		putValue(SHORT_DESCRIPTION, Translator.get("EntityDeleteAction.shortDescription"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0) );
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		UndoSystem.addEvent(this,new EntityDeleteEdit((String)this.getValue(Action.NAME), scene, Clipboard.getSelectedEntities()));
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(!Clipboard.getSelectedEntities().isEmpty());
	}
}
