package com.marginallyclever.ro3.apps.nodeselector;

import com.marginallyclever.ro3.apps.dialogs.NodeSelectionDialog;
import com.marginallyclever.ro3.node.Node;

import javax.swing.*;

/**
 * <p>{@link NodeSelector} is a component that allows the user to select a {@link Node}.  Internally it stores a
 * reference to the selected node.</p>
 * <p>{@link NodeSelector} looks like a button.  It displays the name of the selected Node.  If no node is selected,
 * it displays "...".  When the user clicks on the button, a popup menu appears with a
 * {@link JTree} of all available nodes.</p>
 * <p>When the user selects a node, the button's text is updated to show the name of the selected node.  Also,
 * a {@link java.beans.PropertyChangeEvent} is fired.  The propertyName will be "subject" and the values will be of
 * type T.</p>
 */
public class NodeSelector<T extends Node> extends JButton {
    private T subject;

    public NodeSelector(Class<T> type) {
        this(type,null);
    }

    public NodeSelector(Class<T> type, T subject) {
        super();
        this.subject = subject;
        setName("selector");
        addActionListener((e)-> runSelectionDialog(type));
        setButtonLabel();
    }

    private void runSelectionDialog(Class<T> type) {
        NodeSelectionDialog<T> panel = new NodeSelectionDialog<>(type);
        panel.setSubject(subject);
        int result = JOptionPane.showConfirmDialog(NodeSelector.this, panel, "Select "+type.getSimpleName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            T oldValue = subject;
            NodeSelector.this.subject = panel.getSelectedNode();
            setButtonLabel();

            firePropertyChange("subject",oldValue,subject);
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
