package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Move {@link Node}s from their current parent to another parent.
 */
public class MoveNode extends AbstractUndoableEdit {
    private final Map<Node,Node> childParentMap = new HashMap<>();
    private final Node newParent;
    private final int insertAt;

    public MoveNode(List<Node> selection, Node newParent,int insertAt) {
        super();
        this.newParent = newParent;
        this.insertAt = insertAt;

        selection.remove(Registry.getScene());
        for(var node : selection) {
            childParentMap.put(node,node.getParent());
        }
        execute();
    }

    @Override
    public String getPresentationName() {
        int count = childParentMap.size();
        return count>1? "Move "+count+" Nodes" : "Move node";
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    public void execute() {
        int newIndex = insertAt;
        for(Node child : childParentMap.keySet()) {
            // Remove node from its current parent
            Node oldParent = child.getParent();
            int oldIndex = -1;
            if (oldParent != null) {
                oldIndex = oldParent.getChildren().indexOf(child);
                oldParent.removeChild(child);
            }

            // Get the index at which the source node will be added
            if (newIndex == -1) {
                // If the drop location is a node, add the node at the end
                newParent.addChild(child);
            } else {
                // If oldParent and newParent are the same instance, adjust the index accordingly
                if (oldParent == newParent && oldIndex < newIndex) {
                    newIndex--;
                }
                // If the drop location is an index, add the node at the specified index
                newParent.addChild(newIndex, child);
            }
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        for(Map.Entry<Node, Node> entry : childParentMap.entrySet()) {
            Node parent = entry.getValue();
            Node child = entry.getKey();
            newParent.removeChild(child);
            parent.addChild(child);
        }
    }
}
