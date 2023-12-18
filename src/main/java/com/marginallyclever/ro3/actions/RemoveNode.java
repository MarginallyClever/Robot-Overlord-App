package com.marginallyclever.ro3.actions;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodetreeview.NodeTreeBranch;
import com.marginallyclever.ro3.node.nodetreeview.NodeTreeView;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RemoveNode extends AbstractAction {
    private final NodeTreeView nodeTreeView;
    public RemoveNode(NodeTreeView nodeTreeView) {
        super("Remove Node");
        this.nodeTreeView = nodeTreeView;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        nodeTreeView.removeSelectedNodes();
    }
}
