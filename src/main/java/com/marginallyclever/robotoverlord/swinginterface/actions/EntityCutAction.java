package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.swinginterface.EditorAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class EntityCutAction extends AbstractAction implements EditorAction {
    private final EntityDeleteAction removeAction;
    private final EntityCopyAction copyAction;

    public EntityCutAction(String name, EntityDeleteAction removeAction, EntityCopyAction copyAction) {
        super(name);
        this.removeAction = removeAction;
        this.copyAction = copyAction;
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
