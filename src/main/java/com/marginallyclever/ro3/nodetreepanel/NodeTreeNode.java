package com.marginallyclever.ro3.nodetreepanel;

import com.marginallyclever.ro3.nodes.Node;

import javax.swing.tree.DefaultMutableTreeNode;

public class NodeTreeNode extends DefaultMutableTreeNode {

    public NodeTreeNode(Node node) {
        super(node);
    }

    public Node getNode() {
        return (Node)userObject;
    }

    @Override
    public String toString() {
        return getNode().getName();
    }

    public void setNode(Node node) {
        setUserObject(node);
    }
}
