package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
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

    public ComponentPasteAction() {
        super(Translator.get("ComponentPasteAction.name"));
        putValue(Action.SMALL_ICON,new UnicodeIcon("📎"));
        putValue(Action.SHORT_DESCRIPTION, Translator.get("ComponentPasteAction.shortDescription"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoSystem.addEvent(this,new ComponentPasteEdit((String)this.getValue(Action.NAME),Clipboard.getCopiedComponents(),Clipboard.getSelectedEntities()));
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!Clipboard.getCopiedEntities().getChildren().isEmpty() && !Clipboard.getSelectedEntities().isEmpty());
    }
}
