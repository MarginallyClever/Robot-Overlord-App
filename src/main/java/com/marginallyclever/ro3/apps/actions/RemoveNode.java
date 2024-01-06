package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.UndoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

/**
 * {@link RemoveNode} is an action that removes the selected node(s) from the scene.
 */
public class RemoveNode extends AbstractAction {
    private final Logger logger = LoggerFactory.getLogger(RemoveNode.class);

    public RemoveNode() {
        super();
        putValue(Action.NAME,"Remove");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-delete-16.png"))));
        putValue(SHORT_DESCRIPTION,"Remove the selected node(s).");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        logger.info("Removing node(s).");
        var selection = new ArrayList<>(Registry.selection.getList());
        Registry.selection.removeAll();
        UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.RemoveNode(selection));
    }
}
