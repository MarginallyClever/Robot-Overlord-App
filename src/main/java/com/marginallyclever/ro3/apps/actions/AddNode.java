package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.apps.FactoryPanel;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.UndoSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Add a new instance of a Node to every selected branches of the tree
 */
public class AddNode<T extends Node> extends AbstractAction {
    private static final FactoryPanel<Node> nfd = new FactoryPanel<>(Registry.nodeFactory);

    public AddNode() {
        super();
        putValue(Action.NAME,"Add");
        putValue(SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-add-16.png"))));
        putValue(SHORT_DESCRIPTION,"Add a new instance of a Node to every selected branches of the tree.");
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
        UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.AddNode<>(factory));
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
