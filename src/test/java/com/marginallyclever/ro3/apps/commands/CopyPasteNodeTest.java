package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.ClipboardHelper;
import com.marginallyclever.convenience.helpers.JSONHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

public class CopyPasteNodeTest {
    /**
     * Create a tree of nodes.
     * Grab some non-contiguous nodes.  Move them to a new parent.  Check they are in the right place.
     * Undo the move.  Check they are back in the original place.
     */
    @Test
    public void testCopyPasteNode() {
        Registry.start();

        var root = Registry.getScene();

        Node[] list = new Node[10];
        for(int i=0;i<list.length;++i) {
            list[i] = Registry.nodeFactory.create("Pose");
            list[i].setName("Node "+i);
            root.addChild(list[i]);
        }

        List<Node> selection = new ArrayList<>();
        for(int i=0;i<4;++i) {
            selection.add(list[2 + i * 2]);
        }
        // check initial conditions.
        assert(root.getChildren().size() == list.length);

        Clipboard clipboard = ClipboardHelper.getClipboard();

        var beforeClip = clipboard.getContents(null);
        var copy = new CopyNode(selection);
        var afterClip = clipboard.getContents(null);
        // check clipboard contents changed.
        assert(!doTransferrablesMatch(beforeClip, afterClip));

        int beforeSize = root.getChildren().size();

        var list2 = new ArrayList<Node>();
        list2.add(Registry.getScene());
        var paste = new PasteNode(list2);
        int afterSize = root.getChildren().size();

        // check nodes were pasted.
        assert(afterSize-beforeSize == selection.size());
        // check none of the nodes have matching unique IDs.
        for (Node a : selection) {
            boolean found = false;
            for (Node b : list) {
                found = b.getUniqueID().equals(a.getUniqueID());
            }
            assert(!found);
        }

        paste.undo();
        // check nodes were removed.
        assert(root.getChildren().size() == list.length);
        // TODO check the nodes that remain are from list.

        copy.undo();
        var finalClip = clipboard.getContents(null);
        // check clipboard contents restored.
        assert(doTransferrablesMatch(beforeClip, finalClip));
    }

    /**
     * Checks two transferables for JSON equivalence; handles exceptions
     */
    private boolean doTransferrablesMatch(Transferable a, Transferable b) {
        if(a==null && b==null) return true;
        if(a==null || b==null) return false;
        try {
            if(!a.isDataFlavorSupported(JSONHelper.JSON_FLAVOR) && !b.isDataFlavorSupported(JSONHelper.JSON_FLAVOR)) {
                return true;
            }
            if(!a.isDataFlavorSupported(JSONHelper.JSON_FLAVOR) || !b.isDataFlavorSupported(JSONHelper.JSON_FLAVOR)) {
                return false;
            }
            String aString = (String)a.getTransferData(JSONHelper.JSON_FLAVOR);
            String bString = (String)b.getTransferData(JSONHelper.JSON_FLAVOR);
            return aString.equals(bString);
        } catch(Exception ex) {
            return false;
        }
    }
}
