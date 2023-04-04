package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.ComponentPasteEdit;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityPasteEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Makes a deep copy of the selected {@link Entity}.
 */
public class ComponentPasteAction extends AbstractAction implements EditorAction {
    private final RobotOverlord ro;

    public ComponentPasteAction(RobotOverlord ro) {
        super(Translator.get("ComponentPasteAction.name"));
        this.ro=ro;
        putValue(Action.SMALL_ICON,new UnicodeIcon("📎"));
        putValue(Action.SHORT_DESCRIPTION, Translator.get("ComponentPasteAction.shortDescription"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoSystem.addEvent(this,new ComponentPasteEdit((String)this.getValue(Action.NAME),ro,ro.getCopiedComponents(),ro.getSelectedEntities()));
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!ro.getCopiedEntities().getChildren().isEmpty() && !ro.getSelectedEntities().isEmpty());
    }
}
