package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Makes a deep copy of the selected {@link Entity}.
 */
public class EntityCopyAction extends AbstractAction implements EditorAction {
    private final EntityManager entityManager;

    public EntityCopyAction(EntityManager entityManager) {
        super(Translator.get("EntityCopyAction.name"));
        this.entityManager = entityManager;
        putValue(SMALL_ICON,new UnicodeIcon("📋"));
        putValue(SHORT_DESCRIPTION, Translator.get("EntityCopyAction.shortDescription"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        List<Entity> list = Clipboard.getSelectedEntities();
        Entity container = new Entity();
        for(Entity entity : list) {
            Entity e = entity.deepCopy();
            entityManager.addEntityToParent(e,container);
        }
        Clipboard.setCopiedEntities(container);
    }

    private Entity makeDeepCopy(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!Clipboard.getSelectedEntities().isEmpty());
    }
}
