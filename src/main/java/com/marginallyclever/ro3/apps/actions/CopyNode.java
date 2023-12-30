package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.convenience.helpers.JSONHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.apps.nodetreeview.NodeTreeView;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

public class CopyNode extends AbstractAction {

    public CopyNode() {
        super();
        putValue(Action.NAME,"Copy");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-copy-16.png"))));
        putValue(SHORT_DESCRIPTION,"Copy the selected node(s).");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        var selection = new ArrayList<>(Registry.selection.getList());
        if(selection.isEmpty()) return;
        UndoSystem.addEvent(new com.marginallyclever.ro3.apps.commands.CopyNode(selection));
    }
}
