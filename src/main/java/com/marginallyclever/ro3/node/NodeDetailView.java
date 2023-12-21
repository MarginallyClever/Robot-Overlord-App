package com.marginallyclever.ro3.node;

import com.marginallyclever.ro3.DockingPanel;
import com.marginallyclever.ro3.node.nodetreeview.SelectionChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeDetailView} is a panel that displays the details of a class that implements {@link Node}.
 */
public class NodeDetailView extends JPanel implements SelectionChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(NodeDetailView.class);
    private final JScrollPane scroll = new JScrollPane();

    public NodeDetailView() {
        super(new BorderLayout());
        this.add(scroll, BorderLayout.CENTER);
        selectionChanged(List.of());
    }

    public static JPanel createPanelFor(List<Node> nodeList) {
        JPanel parent = new JPanel(new BorderLayout());
        if(nodeList.isEmpty()) {
            JLabel label = new JLabel("No nodes selected.");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            parent.add(label, BorderLayout.CENTER);
            return parent;
        }

        Box vertical = Box.createVerticalBox();
        List<JComponent> list = new ArrayList<>();
        for (Node node : nodeList) {
            try {
                node.getComponents(list);
            } catch (Exception e) {
                logger.error("Error getting components for node {}", node, e);
            }
        }
        for (JComponent c : list) {
            vertical.add(c);
        }
        parent.add(vertical, BorderLayout.NORTH);
        return parent;
    }

    /**
     * Called when the selection changes.
     * See <a href="https://stackoverflow.com/questions/62864625/why-boxlayout-is-taking-extra-space">layout fix</a>
     * @param selectedNodes the list of nodes that are currently selected.
     */
    @Override
    public void selectionChanged(List<Node> selectedNodes) {
        scroll.setViewportView(createPanelFor(selectedNodes));
        this.revalidate();
        this.repaint();
    }
}
