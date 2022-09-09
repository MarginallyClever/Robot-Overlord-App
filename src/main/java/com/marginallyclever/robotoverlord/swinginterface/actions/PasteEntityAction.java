package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.PasteEntityEdit;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Makes a deep copy of the selected {@link Entity}.
 */
public class PasteEntityAction extends AbstractAction implements EditorAction {
    private final RobotOverlord ro;

    public PasteEntityAction(String name,RobotOverlord ro) {
        super(name);
        this.ro=ro;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoSystem.addEvent(this,new PasteEntityEdit((String)this.getValue(Action.NAME),ro,ro.getCopiedEntities(),ro.getSelectedEntities()));
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!ro.getCopiedEntities().getEntities().isEmpty() && !ro.getSelectedEntities().isEmpty());
    }
}
