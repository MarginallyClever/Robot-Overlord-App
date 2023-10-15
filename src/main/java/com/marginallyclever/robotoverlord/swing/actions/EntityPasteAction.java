package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.EntityPasteEdit;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Makes a deep copy of the selected {@link Entity}.
 */
public class EntityPasteAction extends AbstractAction implements EditorAction {
    private final EntityManager entityManager;

    public EntityPasteAction(EntityManager entityManager) {
        super(Translator.get("EntityPasteAction.name"));
        this.entityManager = entityManager;
        putValue(SMALL_ICON,new UnicodeIcon("📎"));
        putValue(SHORT_DESCRIPTION, Translator.get("EntityPasteAction.shortDescription"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoSystem.addEvent(new EntityPasteEdit((String)this.getValue(Action.NAME),
                        entityManager,
                        Clipboard.getCopiedEntities(),
                        Clipboard.getSelectedEntities())
        );
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!Clipboard.getCopiedEntities().getChildren().isEmpty() && !Clipboard.getSelectedEntities().isEmpty());
    }
}
