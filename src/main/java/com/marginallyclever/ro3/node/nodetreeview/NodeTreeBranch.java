package com.marginallyclever.ro3.node.nodetreeview;

import com.marginallyclever.ro3.node.Node;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * {@link NodeTreeBranch} is a tree branch that contains a {@link Node}.
 */
public class NodeTreeBranch extends DefaultMutableTreeNode {
    public NodeTreeBranch(Node node) {
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
    public void setUserObject(Object userObject) {
        if(userObject instanceof Node) {
            super.setUserObject(userObject);
            return;
        }
        Node node = this.getNode();
        if(node==null) throw new NullPointerException("Node is null");
        node.setName(userObject.toString());
    }

    @Override
    public String toString() {
        return getNode().getName();
    }

    public void setNode(Node node) {
        setUserObject(node);
    }
}
