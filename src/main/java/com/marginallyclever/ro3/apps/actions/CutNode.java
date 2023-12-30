package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class CutNode extends AbstractAction {
    private final Logger logger = LoggerFactory.getLogger(CutNode.class);

    public CutNode() {
        super();
        putValue(Action.NAME,"Cut");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-cut-16.png"))));
        putValue(SHORT_DESCRIPTION,"Cut the selected node(s).");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        logger.info("Cutting node(s).");
        // copy the selected nodes using the CopyNode action.
        new CopyNode().actionPerformed(e);
        // remove the selected noes using the RemoveNode action.
        new RemoveNode().actionPerformed(e);
    }
}
