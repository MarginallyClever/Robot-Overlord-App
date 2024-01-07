package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.UndoSystem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Copy the selected node(s) to the clipboard.
 */
public class CopyNode extends AbstractAction {
    public CopyNode() {
        super();
        putValue(Action.NAME,"Copy");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-copy-16.png"))));
        putValue(SHORT_DESCRIPTION,"Copy the selected node(s).");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        var selection = new ArrayList<>(Registry.selection.getList());
        if(selection.isEmpty()) return;
        UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.CopyNode(selection));
    }
}
