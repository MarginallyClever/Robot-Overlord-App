package com.marginallyclever.ro3.apps.nodeselector;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.dialogs.NodeSelectionDialog;
import com.marginallyclever.ro3.node.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

/**
 * <p>{@link NodeSelector} is a component that allows the user to select a {@link Node}.  Internally it stores a
 * reference to the selected node.</p>
 * <p>{@link NodeSelector} looks like a button.  It displays the name of the selected Node.  If no node is selected,
 * it displays "...".  When the user clicks on the button, a popup menu appears with a
 * {@link JTree} of all available nodes.</p>
 * <p>When the user selects a node, the button's text is updated to show the name of the selected node.  Also,
 * a {@link java.beans.PropertyChangeEvent} is fired.  The propertyName will be "subject" and the values will be of
 * type T.</p>
 * <p>{@link NodeSelector} also provides a <b>find</b> button.  The find action changes the global selection, which
 * updates any other systems listening to the selection.</p>
 */
public class NodeSelector<T extends Node> extends JPanel {
    private T subject;
    private final JButton chooseButton = new JButton();
    private final JButton selectButton = new JButton(new AbstractAction() {
        {
            putValue(Action.NAME,"");
            putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource(
                    "/com/marginallyclever/ro3/apps/shared/icons8-search-16.png"))));
            putValue(Action.SHORT_DESCRIPTION,"Select this node");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Registry.selection.set(subject);
        }
    });

    public NodeSelector(Class<T> type) {
        this(type,null);
    }

    public NodeSelector(Class<T> type, T subject) {
        super(new BorderLayout());
        setName("selector");

        this.subject = subject;

        chooseButton.addActionListener((e)-> runSelectionDialog(type));
        setButtonLabel();

        add(chooseButton,BorderLayout.CENTER);
        add(selectButton,BorderLayout.LINE_END);

        new DropTarget(this,new NodeSelectorDropTarget<>(this,type));
    }

    private void runSelectionDialog(Class<T> type) {
        NodeSelectionDialog<T> panel = new NodeSelectionDialog<>(type);
        panel.setSubject(subject);
        int result = JOptionPane.showConfirmDialog(NodeSelector.this, panel, "Select "+type.getSimpleName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            setSubject(panel.getSelectedNode());
        }
    }

    private void setButtonLabel() {
        chooseButton.setText(subject==null ? "..." : subject.getName());
        setToolTipText(subject==null ? null : subject.getAbsolutePath());
    }

    public void setSubject(T subject) {
        T oldValue = this.subject;
        this.subject = subject;
        setButtonLabel();
        firePropertyChange("subject",oldValue,subject);
    }

    public T getSubject() {
        return subject;
    }
}
