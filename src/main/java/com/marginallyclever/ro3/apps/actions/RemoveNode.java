package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.apps.nodetreeview.NodeTreeView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class RemoveNode extends AbstractAction {
    private final NodeTreeView nodeTreeView;
    public RemoveNode(NodeTreeView nodeTreeView) {
        super();
        putValue(Action.NAME,"Remove");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-delete-16.png"))));
        putValue(SHORT_DESCRIPTION,"Remove the selected node(s).");
        this.nodeTreeView = nodeTreeView;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        nodeTreeView.removeSelectedNodes();
    }
}
