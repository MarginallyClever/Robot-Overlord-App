package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityPasteEdit;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Makes a deep copy of the selected {@link Entity}.
 */
public class EntityPasteAction extends AbstractAction implements EditorAction {
    private final RobotOverlord ro;

    public EntityPasteAction(RobotOverlord ro) {
        super(Translator.get("EntityPasteAction.name"));
        this.ro=ro;
        putValue(Action.SMALL_ICON,new UnicodeIcon("📎"));
        putValue(Action.SHORT_DESCRIPTION, Translator.get("EntityPasteAction.shortDescription"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoSystem.addEvent(this,new EntityPasteEdit((String)this.getValue(Action.NAME),ro,ro.getCopiedEntities(),ro.getSelectedEntities()));
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!ro.getCopiedEntities().getChildren().isEmpty() && !ro.getSelectedEntities().isEmpty());
    }
}
