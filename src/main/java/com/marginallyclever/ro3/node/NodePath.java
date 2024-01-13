package com.marginallyclever.ro3.node;

import com.marginallyclever.convenience.PathCalculator;

/**
 * <p>{@link NodePath} stores the uniqueID to a node.
 * Can be used to find a node in the scene graph.</p>
 * @param <T> the type of node to find
 */
public class NodePath<T extends Node> {
    private String uniqueID;
    private final Node owner;
    private final Class<T> type;

    public NodePath(Node owner,Class<T> type) {
        this(owner,type,"");
    }

    public NodePath(Node owner,Class<T> type,String uniqueID) {
        this.owner = owner;
        this.type = type;
        this.uniqueID = uniqueID;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public void setUniqueIDByNode(Node node) {
        if (node != null) {
            this.uniqueID = node.getUniqueID().toString();
        } else {
            this.uniqueID = "";
        }
    }

    public T getSubject() {
        return owner.getRootNode().findNodeByID(uniqueID,type);
    }
}
