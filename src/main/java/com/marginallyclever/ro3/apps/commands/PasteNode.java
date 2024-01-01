package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.JSONHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

public class PasteNode extends AbstractUndoableEdit {
    private final Logger logger = LoggerFactory.getLogger(PasteNode.class);
    Transferable transfer;
    private final List<Node> children = new ArrayList<>();
    private final List<Node> parents;

    /**
     * Paste the copied nodes as children of the parent nodes.
     * @param parents the parent nodes.
     */
    public PasteNode(List<Node> parents) {
        super();
        this.parents = parents;
        transfer = Registry.clipboard.getContents(null);
        execute();
    }

    @Override
    public String getPresentationName() {
        return "Paste";
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    public void execute() {
        if(transfer==null || !transfer.isDataFlavorSupported(JSONHelper.JSON_FLAVOR)) return;

        try {
            String jsonString = (String)transfer.getTransferData(JSONHelper.JSON_FLAVOR);
            var jsonWrapper = new JSONObject(jsonString);
            var jsonArray = jsonWrapper.getJSONArray("copied");
            for(int i=0;i<jsonArray.length();++i) {
                var jsonObject = jsonArray.getJSONObject(i);
                // import this json as a child of every selected node.
                for(Node parent : parents) {
                    Node child = ImportScene.createFromJSON(jsonObject);
                    parent.addChild(child);
                    children.add(child);
                }
            }

            Registry.selection.set(parents);
        } catch(Exception ex) {
            logger.error("Paste error.",ex);
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        for(Node child : children) {
            Node parent = child.getParent();
            parent.removeChild(child);
        }
        children.clear();
    }
}
