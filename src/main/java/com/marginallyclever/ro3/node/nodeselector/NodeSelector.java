package com.marginallyclever.ro3.node.nodeselector;

import com.marginallyclever.ro3.node.Node;

import javax.swing.*;

/**
 * <p>{@link NodeSelector} is a component that allows the user to select a {@link Node}.  Internally it stores the unique ID of the selected node.</p>
 * <p>nodeselector looks like a button.  It displays the name of the selected Node.  If no node is selected,
 * it displays "...".  When the user clicks on the button, a popup menu appears with a
 * {@link JTree} of all available nodes.  When the user selects a node, the button's text is
 * updated to show the name of the selected node.</p>
 */
public class NodeSelector<T extends Node> extends JButton {
    private T subject;

    public NodeSelector(Class<T> type) {
        super();
        addActionListener((e)-> runSelectionDialog(type));
        setButtonLabel();
    }

    public NodeSelector(Class<T> type, T subject) {
        super();
        this.subject = subject;
        addActionListener((e)-> runSelectionDialog(type));
        setButtonLabel();
    }

    private void runSelectionDialog(Class<T> type) {
        NodeSelectionPanel<T> panel = new NodeSelectionPanel<>(type);
        panel.setSubject(subject);
        int result = JOptionPane.showConfirmDialog(NodeSelector.this, panel, "Select Node", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            NodeSelector.this.subject = panel.getSelectedNode();
            setButtonLabel();

            firePropertyChange("subject",null,subject);
        }
    }

    private void setButtonLabel() {
        setText(subject==null ? "..." : subject.getName());
        setToolTipText(subject==null ? null : subject.getAbsolutePath());
    }

    public void setSubject(T subject) {
        this.subject = subject;
        setButtonLabel();
    }

    public T getSubject() {
        return subject;
    }
}
