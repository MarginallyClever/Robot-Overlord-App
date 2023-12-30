package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.apps.nodetreeview.NodeTreeView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

public class RemoveNode extends AbstractAction {
    public RemoveNode() {
        super();
        putValue(Action.NAME,"Remove");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-delete-16.png"))));
        putValue(SHORT_DESCRIPTION,"Remove the selected node(s).");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var selection = new ArrayList<>(Registry.selection.getList());
        Registry.selection.removeAll();
        UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.RemoveNode(selection));
    }
}
