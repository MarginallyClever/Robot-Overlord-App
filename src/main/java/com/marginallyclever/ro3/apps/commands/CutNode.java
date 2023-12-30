package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.node.Node;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import java.util.List;

/**
 * Cut is a Copy followed by a Remove.
 */
public class CutNode extends AbstractUndoableEdit {
    private final CopyNode copyNode;
    private final RemoveNode removeNode;

    public CutNode(List<Node> selection) {
        super();
        this.copyNode = new CopyNode(selection);
        this.removeNode = new RemoveNode(selection);
        // copy and remove already call their respective execute() methods.
    }

    @Override
    public String getPresentationName() {
        return "Cut";
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    public void execute() {
        copyNode.execute();
        removeNode.execute();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        removeNode.reverse();
        copyNode.reverse();
    }
}
