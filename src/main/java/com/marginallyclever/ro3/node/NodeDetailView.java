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
    private final JPanel parent = new JPanel(new BorderLayout());

    public NodeDetailView() {
        super();
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(parent);
        this.add(scroll, BorderLayout.CENTER);
    }

    /**
     * Called when the selection changes.
     * See <a href="https://stackoverflow.com/questions/62864625/why-boxlayout-is-taking-extra-space">layout fix</a>
     * @param selectedNodes the list of nodes that are currently selected.
     */
    @Override
    public void selectionChanged(List<Node> selectedNodes) {
        Box vertical = Box.createVerticalBox();
        List<JComponent> list = new ArrayList<>();

        for(Node node : selectedNodes) {
            try {
                node.getComponents(list);
            } catch(Exception e) {
                logger.error("Error getting components for node {}",node,e);
            }
        }

        for(JComponent c : list) {
            vertical.add(c);
        }

        parent.removeAll();
        parent.add(vertical,BorderLayout.NORTH);
        this.revalidate();
        this.repaint();
    }
}
