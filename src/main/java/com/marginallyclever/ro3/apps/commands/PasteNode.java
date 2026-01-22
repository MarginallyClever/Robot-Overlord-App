package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.BigMatrixHelper;
import com.marginallyclever.convenience.helpers.ClipboardHelper;
import com.marginallyclever.convenience.helpers.JSONHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

/**
 * Paste the copied nodes as children of the parent nodes.
 */
public class PasteNode extends AbstractUndoableEdit {
    private final Logger logger = LoggerFactory.getLogger(PasteNode.class);
    private final List<Node> children = new ArrayList<>();
    private final List<Node> parents;
    private final Transferable transfer;

    /**
     * Paste the copied nodes as children of the parent nodes.
     * @param parents the parent nodes.
     */
    public PasteNode(List<Node> parents) {
        super();
        this.parents = parents;
        transfer = ClipboardHelper.getClipboard().getContents(null);
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
            var poses = jsonWrapper.getJSONObject("poses");
            for(int i=0;i<jsonArray.length();++i) {
                var jsonObject = jsonArray.getJSONObject(i);
                // import this json as a child of every selected node.
                for(Node parent : parents) {
                    // import this json as a child of every selected node.
                    // guarantees the nodes go through witness protection.
                    Node child = ImportScene.createFromJSON(jsonObject);
                    var savedID = child.getUniqueID();
                    child.witnessProtection();
                    parent.addChild(child);
                    children.add(child);

                    // pasting needs to restore world matrices for Poses.
                    if(child instanceof Pose pose) {
                        System.out.println("Restoring world matrix for pasted pose " + pose.getAbsolutePath());
                        var worldArray = poses.getJSONArray(savedID);
                        if(worldArray!=null) {
                            double[] worldData = new double[16];
                            for (int j = 0; j < 16; j++) {
                                worldData[j] = worldArray.getDouble(j);
                            }
                            pose.setWorld(new Matrix4d(worldData));
                        }
                    }
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
