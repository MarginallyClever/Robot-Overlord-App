package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Display an Add Entity dialog box.  If an entity is selected and "ok" is pressed, add that Entity to the world. 
 * @author Dan Royer
 *
 */
public class EntityAddChildAction extends AbstractAction implements EditorAction {
	private static final Logger logger = LoggerFactory.getLogger(EntityAddChildAction.class);
	private final EntityManager entityManager;

	/**
	 *
	 * @param entityManager the EntityManager to add the new Entity to.
	 */
	public EntityAddChildAction(EntityManager entityManager) {
		super(Translator.get("EntityAddChildAction.name"));
		this.entityManager = entityManager;
		putValue(SMALL_ICON,new UnicodeIcon("+"));
		putValue(SHORT_DESCRIPTION, Translator.get("EntityAddChildAction.shortDescription"));
	}
	
    /**
     * select from a list of all object types.  An instance of that type is then added to the world.
     */
	@Override
	public void actionPerformed(ActionEvent event) {
		List<Entity> list = Clipboard.getSelectedEntities();
		for(Entity parent : list) {
			UndoSystem.addEvent(new EntityAddEdit(entityManager,parent,new Entity()));
		}
    }

	@Override
	public void updateEnableStatus() {
		setEnabled(Clipboard.getSelectedEntities().size()==1);
	}
}
