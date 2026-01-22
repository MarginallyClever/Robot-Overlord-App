package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.BigMatrixHelper;
import com.marginallyclever.convenience.helpers.ClipboardHelper;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;

/**
 * Copy the selected node(s) to the clipboard.
 */
public class CopyNode extends AbstractUndoableEdit {
    private final Logger logger = LoggerFactory.getLogger(com.marginallyclever.ro3.apps.actions.CopyNode.class);
    private final List<Node> selection;
    private final Transferable before;

    public CopyNode(List<Node> selection) {
        super();
        this.selection = selection;
        this.before = ClipboardHelper.getClipboard().getContents(null);
        execute();
    }

    @Override
    public String getPresentationName() {
        return "Copy";
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    public void execute() {
        try {
            JSONObject poses = new JSONObject();
            JSONArray list = new JSONArray();
            for (Node node : selection) {
                logger.debug("Copying {}", node.getAbsolutePath());
                list.put(node.toJSON());
                // store the world matrix for poses.
                if(node instanceof Pose pose) {
                    System.out.println("Storing world matrix for copied pose " + pose.getAbsolutePath());
                    double[] worldArray = BigMatrixHelper.matrix4dToArray(pose.getWorld());
                    poses.put(pose.getUniqueID(),new JSONArray(worldArray));
                }
            }
            JSONObject jsonWrapper = new JSONObject();
            jsonWrapper.put("copied", list);
            jsonWrapper.put("poses", poses);

            // store the json in the clipboard.
            StringSelection stringSelection = new StringSelection(jsonWrapper.toString());
            ClipboardHelper.getClipboard().setContents(stringSelection, null);
        } catch (Exception e) {
            logger.error("Error copying nodes to clipboard", e);
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        ClipboardHelper.getClipboard().setContents(before, null);
    }
}
