package com.marginallyclever.ro3.node.nodetreepanel;

import com.marginallyclever.ro3.node.Node;

import javax.swing.tree.DefaultMutableTreeNode;

public class NodeTreeNode extends DefaultMutableTreeNode {

    public NodeTreeNode(Node node) {
        super(node);
    }

    public Node getNode() {
        if (userObject instanceof Node) {
            return (Node) userObject;
        } else {
            throw new ClassCastException("UserObject is not an instance of Node");
        }
    }

    @Override
    public String toString() {
        return getNode().getName();
    }

    public void setNode(Node node) {
        if (node instanceof Node) {
            setUserObject(node);
        } else {
            throw new IllegalArgumentException("Argument is not an instance of Node");
        }
    }
}
