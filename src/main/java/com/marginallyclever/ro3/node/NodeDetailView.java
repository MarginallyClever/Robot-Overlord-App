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
    public NodeDetailView() {
        super();
    }

    /**
     * Called when the selection changes.
     * See <a href="https://stackoverflow.com/questions/62864625/why-boxlayout-is-taking-extra-space">layout fix</a>
     * @param selectedNodes the list of nodes that are currently selected.
     */
    @Override
    public void selectionChanged(List<Node> selectedNodes) {
        JPanel parent = new JPanel();

        parent.setLayout(new BorderLayout());
        Box vertical = Box.createVerticalBox();
        parent.add(vertical,BorderLayout.PAGE_START);

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

        this.removeAll();
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(parent);
        this.add(scroll, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }
}
