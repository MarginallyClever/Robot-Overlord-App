package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.convenience.helpers.JSONHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Objects;

public class PasteNode extends AbstractAction {
    private final Logger logger = LoggerFactory.getLogger(PasteNode.class);

    public PasteNode() {
        super();
        putValue(Action.NAME,"Paste");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-paste-16.png"))));
        putValue(SHORT_DESCRIPTION,"Paste the selected node(s).");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        // get the contents of Registry.clipboard if it's a JSON string
        Transferable transfer = Registry.clipboard.getContents(null);
        if(transfer==null) return;
        if(!transfer.isDataFlavorSupported(JSONHelper.JSON_FLAVOR)) return;

        try {
            // get list once is faster and safer.
            var selection = new ArrayList<>(Registry.selection.getList());

            String jsonString = (String)transfer.getTransferData(JSONHelper.JSON_FLAVOR);
            var jsonWrapper = new JSONObject(jsonString);
            var jsonArray = jsonWrapper.getJSONArray("copied");
            for(int i=0;i<jsonArray.length();++i) {
                var jsonObject = jsonArray.getJSONObject(i);
                // import this json as a child of every selected node.
                for(Node parent : selection) {
                    parent.addChild(ImportScene.createFromJSON(jsonObject));
                }
            }

            Registry.selection.set(selection);
        } catch(Exception ex) {
            logger.error("Paste error.",ex);
        }
    }
}
