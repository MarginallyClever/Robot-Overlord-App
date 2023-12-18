package com.marginallyclever.ro3.actions;

import com.marginallyclever.ro3.FactoryPanel;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodetreeview.NodeTreeView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class AddNode extends AbstractAction {
    private static final FactoryPanel<Node> nfd = new FactoryPanel<>(Registry.nodeFactory);
    private final NodeTreeView treeView;

    public AddNode(NodeTreeView treeView) {
        super("Add Node");
        this.treeView = treeView;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Supplier<Node> factory = getFactoryFromUser((Component) e.getSource());
        if(factory==null) return;  // action cancelled
        treeView.addChildrenUsingFactory(factory);
    }

    private Supplier<Node> getFactoryFromUser(Component source) {
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);
        int result = JOptionPane.showConfirmDialog(parentFrame,nfd,"Create Node",JOptionPane.OK_CANCEL_OPTION);
        if(result != JOptionPane.OK_OPTION) return null;  // cancelled
        if(nfd.getResult() != JOptionPane.OK_OPTION) return null;  // cancelled

        String type = nfd.getSelectedNode();
        if(type.isEmpty()) return null;  // cancelled?

        Supplier<Node> factory = Registry.nodeFactory.getSupplierFor(type);
        if(factory==null) throw new RuntimeException("NodeTreePanel: no factory for "+type);

        return factory;
    }
}
