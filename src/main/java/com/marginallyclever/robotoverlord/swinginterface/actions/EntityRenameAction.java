package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityRenameEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 *  
 * @author Dan Royer
 *
 */
public class EntityRenameAction extends AbstractAction implements EditorAction {
	private static final Logger logger = LoggerFactory.getLogger(EntityRenameAction.class);
	private final RobotOverlord robotOverlord;
	
	public EntityRenameAction(RobotOverlord robotOverlord) {
		super(Translator.get("EntityRenameAction.name"));
		putValue(Action.SHORT_DESCRIPTION, Translator.get("EntityRenameAction.shortDescription"));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
		this.robotOverlord = robotOverlord;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> entityList = Clipboard.getSelectedEntities();
		if (entityList.size() != 1) {
			logger.error("Rename more than one entity at the same time?!");
			return;
		}
		Entity e = entityList.get(0);
		renameEntity(robotOverlord, e);
	}

	public void renameEntity(RobotOverlord ro, Entity e) {
		String newName = (String)JOptionPane.showInputDialog(
				ro.getMainFrame(),
				"New name:",
				"Rename Entity",
				JOptionPane.PLAIN_MESSAGE,null,null,e.getName());
		if( newName!=null && !newName.equals(e.getName()) ) {
			UndoSystem.addEvent(this,new EntityRenameEdit(ro,e,newName));
		}
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(Clipboard.getSelectedEntities().size()==1);
	}
}
