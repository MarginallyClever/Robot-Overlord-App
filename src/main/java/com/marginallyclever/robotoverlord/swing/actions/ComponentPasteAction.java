package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.swing.UnicodeIcon;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.ComponentPasteEdit;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Makes a deep copy of the selected {@link Entity}.
 */
public class ComponentPasteAction extends AbstractAction implements EditorAction {

    public ComponentPasteAction() {
        super(Translator.get("ComponentPasteAction.name"));
        putValue(SMALL_ICON,new UnicodeIcon("📎"));
        putValue(SHORT_DESCRIPTION, Translator.get("ComponentPasteAction.shortDescription"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoSystem.addEvent(new ComponentPasteEdit((String)this.getValue(Action.NAME),Clipboard.getCopiedComponents(),Clipboard.getSelectedEntities()));
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!Clipboard.getCopiedEntities().getChildren().isEmpty() && !Clipboard.getSelectedEntities().isEmpty());
    }
}
