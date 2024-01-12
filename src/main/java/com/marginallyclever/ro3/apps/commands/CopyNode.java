package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
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
    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public CopyNode(List<Node> selection) {
        super();
        this.selection = selection;
        this.before = clipboard.getContents(null);
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
        JSONArray list = new JSONArray();
        for(Node node : selection) {
            logger.debug("Copying {}",node.getAbsolutePath());
            list.put(node.toJSON());
        }
        JSONObject jsonWrapper = new JSONObject();
        jsonWrapper.put("copied",list);
        // store the json in the clipboard.
        StringSelection stringSelection = new StringSelection(jsonWrapper.toString());
        clipboard.setContents(stringSelection, null);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        clipboard.setContents(before, null);
    }
}
