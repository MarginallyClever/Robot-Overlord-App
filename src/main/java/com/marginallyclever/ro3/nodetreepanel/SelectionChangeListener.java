package com.marginallyclever.ro3.nodetreepanel;

import com.marginallyclever.ro3.nodes.Node;

import java.util.EventListener;
import java.util.List;

public interface SelectionChangeListener extends EventListener {
    void selectionChanged(List<Node> selectedNodes);
}
