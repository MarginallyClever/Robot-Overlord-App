package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Cut the selected entities to the clipboard.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class EntityCutAction extends AbstractAction implements EditorAction {
    private final EntityDeleteAction entityDeleteAction;
    private final EntityCopyAction entityCopyAction;

    public EntityCutAction(EntityDeleteAction entityDeleteAction, EntityCopyAction entityCopyAction) {
        super(Translator.get("EntityCutAction.name"));
        this.entityDeleteAction = entityDeleteAction;
        this.entityCopyAction = entityCopyAction;
        putValue(SMALL_ICON,new UnicodeIcon("✂"));
        putValue(SHORT_DESCRIPTION, Translator.get("EntityCutAction.shortDescription"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        entityCopyAction.actionPerformed(e);
        entityDeleteAction.actionPerformed(e);
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!Clipboard.getSelectedEntities().isEmpty());
    }
}
