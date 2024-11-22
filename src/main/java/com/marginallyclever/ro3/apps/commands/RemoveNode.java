package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Remove {@link Node}s from the scene.
 */
public class RemoveNode extends AbstractUndoableEdit {
    private record RemoveNodeEvent(Node parent,Node child, int index) {}
    private final List<RemoveNodeEvent> childParentMap = new ArrayList<>();

    public RemoveNode(List<Node> selection) {
        super();
        selection.remove(Registry.getScene());
        for(var child : selection) {
            Node parent = child.getParent();
            childParentMap.add(new RemoveNodeEvent(parent,child,parent.getChildren().indexOf(child)));
        }
        execute();
    }

    @Override
    public String getPresentationName() {
        int count = childParentMap.size();
        return count>1? "Remove "+count+" Nodes" : "Remove node";
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    public void execute() {
        for(RemoveNodeEvent entry : childParentMap) {
            Node parent = entry.parent();
            Node child = entry.child();
            parent.removeChild(child);
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        for(RemoveNodeEvent entry : childParentMap) {
            Node parent = entry.parent();
            Node child = entry.child();
            int index = entry.index();
            parent.addChild(index,child);
        }
    }
}
