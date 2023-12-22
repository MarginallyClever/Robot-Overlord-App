package com.marginallyclever.ro3.apps.nodetreeview;

import com.marginallyclever.ro3.node.Node;

import java.util.EventListener;
import java.util.List;

public interface SelectionChangeListener extends EventListener {
    void selectionChanged(List<Node> selectedNodes);
}
