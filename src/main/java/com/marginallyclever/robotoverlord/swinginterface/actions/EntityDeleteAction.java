package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
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
	private final RobotOverlord ro;
	
	public EntityDeleteAction(RobotOverlord ro) {
		super(Translator.get("EntityDeleteAction.name"));
		this.ro = ro;
		putValue(Action.SMALL_ICON,new UnicodeIcon("🗑"));
		putValue(Action.SHORT_DESCRIPTION, Translator.get("EntityDeleteAction.shortDescription"));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0) );
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		UndoSystem.addEvent(this,new EntityDeleteEdit((String)this.getValue(Action.NAME),ro.getSelectedEntities()));
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(!ro.getSelectedEntities().isEmpty());
	}
}
