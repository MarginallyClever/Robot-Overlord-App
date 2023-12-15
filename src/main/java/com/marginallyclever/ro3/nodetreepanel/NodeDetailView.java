package com.marginallyclever.ro3.nodetreepanel;

import com.marginallyclever.ro3.DockingPanel;
import com.marginallyclever.ro3.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NodeDetailView extends DockingPanel implements SelectionChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(NodeDetailView.class);
    public NodeDetailView() {
        super("Node Detail Viewer");
    }

    public NodeDetailView(String tabName) {
        super(tabName);
    }

    @Override
    public void selectionChanged(List<Node> selectedNodes) {
        JPanel parent = new JPanel();
        parent.setLayout(new BoxLayout(parent,BoxLayout.PAGE_AXIS));

        List<JComponent> list = new ArrayList<>();

        for(Node node : selectedNodes) {
            try {
                node.getComponents(list);
            } catch(Exception e) {
                logger.error("Error getting components for node {}",node,e);
            }
        }

        for(JComponent c : list) {
            parent.add(c);
        }

        this.removeAll();
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(parent);
        this.add(scroll, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}
