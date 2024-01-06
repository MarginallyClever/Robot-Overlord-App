package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.UndoSystem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * {@link CutNode} is an action that cuts the selected node(s) from the scene.
 */
public class CutNode extends AbstractAction {

    public CutNode() {
        super();
        putValue(Action.NAME,"Cut");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-cut-16.png"))));
        putValue(SHORT_DESCRIPTION,"Cut the selected node(s).");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.CutNode(Registry.selection.getList()));
    }
}
