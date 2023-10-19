package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.EntityRenameEdit;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
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
	private final EntityManager entityManager;

	public EntityRenameAction(EntityManager entityManager) {
		super(Translator.get("EntityRenameAction.name"));
		this.entityManager = entityManager;
		putValue(SHORT_DESCRIPTION, Translator.get("EntityRenameAction.shortDescription"));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Component source = (Component) event.getSource();
		JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

		List<Entity> entityList = Clipboard.getSelectedEntities();
		if (entityList.size() != 1) {
			logger.error("Rename more than one entity at the same time?!");
			return;
		}
		Entity e = entityList.get(0);

		String newName = (String)JOptionPane.showInputDialog(
				parentFrame,
				"New name:",
				"Rename Entity",
				JOptionPane.PLAIN_MESSAGE,null,null,e.getName());
		if( newName!=null && !newName.equals(e.getName()) ) {
			UndoSystem.addEvent(new EntityRenameEdit(e,newName,entityManager));
		}
	}

	@Override
	public void updateEnableStatus() {
		setEnabled(Clipboard.getSelectedEntities().size()==1);
	}
}
