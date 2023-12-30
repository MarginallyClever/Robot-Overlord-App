package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class CopyNode extends AbstractAction {
    private final Logger logger = LoggerFactory.getLogger(CopyNode.class);

    public CopyNode() {
        super();
        putValue(Action.NAME,"Copy");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-copy-16.png"))));
        putValue(SHORT_DESCRIPTION,"Copy the selected node(s).");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        var selection = Registry.selection.getList();
        if(selection.isEmpty()) return;

        logger.info("Copying {} node(s).",selection.size());
        JSONArray list = new JSONArray();
        for(Node node : selection) {
            list.put(node.toJSON());
        }
        JSONObject jsonWrapper = new JSONObject();
        jsonWrapper.put("copied",list);
        // store the json for later.
        StringSelection stringSelection = new StringSelection(jsonWrapper.toString());
        Registry.clipboard.setContents(stringSelection, null);
    }
}
