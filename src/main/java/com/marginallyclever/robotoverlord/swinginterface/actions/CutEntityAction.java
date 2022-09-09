package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.swinginterface.EditorAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CutEntityAction extends AbstractAction implements EditorAction {
    private final DeleteEntityAction removeAction;
    private final CopyEntityAction copyAction;

    public CutEntityAction(String name, DeleteEntityAction removeAction, CopyEntityAction copyAction) {
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
