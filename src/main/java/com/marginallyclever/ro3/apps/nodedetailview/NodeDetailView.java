package com.marginallyclever.ro3.apps.nodedetailview;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.listwithevents.ItemAddedListener;
import com.marginallyclever.ro3.listwithevents.ItemRemovedListener;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link NodeDetailView} is a panel that displays the details of a class that implements {@link Node}.
 */
public class NodeDetailView extends JPanel
        implements ItemAddedListener<Node>, ItemRemovedListener<Node> {
    private static final Logger logger = LoggerFactory.getLogger(NodeDetailView.class);
    private final JScrollPane scroll = new JScrollPane();

    public NodeDetailView() {
        super(new BorderLayout());
        this.add(scroll, BorderLayout.CENTER);
        selectionChanged(List.of());
    }

    public static JPanel createPanelFor(List<Node> nodeList) {
        JPanel parent = new JPanel(new BorderLayout());
        // handle no selection.
        if(nodeList.isEmpty()) {
            JLabel label = new JLabel("No nodes selected.");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            parent.add(label, BorderLayout.CENTER);
            return parent;
        }

        // collect the components from every node
        List<JPanel> list = new ArrayList<>();
        for (Node node : nodeList) {
            try {
                node.getComponents(list);
            } catch (Exception e) {
                logger.error("Error getting components for node {}", node, e);
            }
        }
        // collate the components
        Box vertical = Box.createVerticalBox();
        for (JPanel c : list) {
            CollapsiblePanel panel = new CollapsiblePanel(c.getName());
            panel.setContentPane(c);
            vertical.add(panel);
        }
        // attach them to the parent
        parent.add(vertical, BorderLayout.NORTH);
        return parent;
    }

    /**
     * Called when the selection changes.
     * See <a href="https://stackoverflow.com/questions/62864625/why-boxlayout-is-taking-extra-space">layout fix</a>
     * @param selectedNodes the list of nodes that are currently selected.
     */
    public void selectionChanged(List<Node> selectedNodes) {
        scroll.setViewportView(createPanelFor(selectedNodes));
        this.revalidate();
        this.repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.selection.addItemAddedListener(this);
        Registry.selection.addItemRemovedListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.selection.removeItemAddedListener(this);
        Registry.selection.removeItemRemovedListener(this);
    }

    @Override
    public void itemAdded(Object source,Node item) {
        selectionChanged(Registry.selection.getList());
    }

    @Override
    public void itemRemoved(Object source,Node item) {
        selectionChanged(Registry.selection.getList());
    }
}
