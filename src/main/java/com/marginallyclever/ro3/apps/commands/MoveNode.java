package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.util.ArrayList;
import java.util.List;

/**
 * Move {@link Node}s from their current parent to another parent.
 */
public class MoveNode extends AbstractUndoableEdit {
    private static final Logger logger = LoggerFactory.getLogger(MoveNode.class);

    record MoveData(Node child, Node oldParent, int oldIndex) {}

    private final List<MoveData> childParentMap = new ArrayList<>();
    private final Node newParent;
    private final int insertStartingAt;

    /**
     * Move a list of nodes to a new parent.
     * @param selection the list of nodes to move
     * @param newParent the new parent
     * @param insertStartingAt -1 to add to the end, otherwise the index to insert at.
     */
    public MoveNode(List<Node> selection, Node newParent,int insertStartingAt) {
        super();
        this.newParent = newParent;

        if(insertStartingAt==-1) insertStartingAt = newParent.getChildren().size();
        this.insertStartingAt = insertStartingAt;

        selection.remove(Registry.getScene());
        for(var node : selection) {
            Node oldParent = node.getParent();
            int oldIndex = oldParent.getChildren().indexOf(node);
            childParentMap.add(new MoveData(node,oldParent,oldIndex));
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
        // remove the children from their old parents
        for(var data : childParentMap) {
            Node child = data.child;
            Node oldParent = data.oldParent;
            logger.debug("take "+child.getAbsolutePath()+" from "+oldParent.getAbsolutePath()+" @ "+data.oldIndex);
            oldParent.removeChild(child);
        }

        // check if the insert index is valid
        logger.debug("newIndex before "+insertStartingAt);
        int newIndex = insertStartingAt;
        if(newIndex > newParent.getChildren().size()) newIndex = newParent.getChildren().size();
        logger.debug("newIndex after "+insertStartingAt);

        // add the children to the new parent
        for(var data : childParentMap) {
            Node child = data.child;
            newParent.addChild(newIndex++, child);
            logger.debug("put "+child.getAbsolutePath()+" @ "+(newIndex-1));
        }
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        for(var data : childParentMap) {
            Node child = data.child;
            newParent.removeChild(child);
            data.oldParent.addChild(data.oldIndex,child);
        }
    }
}
