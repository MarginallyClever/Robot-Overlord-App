package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class EntityCutAction extends AbstractAction implements EditorAction {
    private final EntityDeleteAction removeAction;
    private final EntityCopyAction copyAction;

    public EntityCutAction(EntityDeleteAction removeAction, EntityCopyAction copyAction) {
        super(Translator.get("EntityCutAction.name"));
        this.removeAction = removeAction;
        this.copyAction = copyAction;
        putValue(Action.SMALL_ICON,new UnicodeIcon("✂"));
        putValue(Action.SHORT_DESCRIPTION, Translator.get("EntityCutAction.shortDescription"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        copyAction.actionPerformed(e);
        removeAction.actionPerformed(e);
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(removeAction.isEnabled());
    }
}
