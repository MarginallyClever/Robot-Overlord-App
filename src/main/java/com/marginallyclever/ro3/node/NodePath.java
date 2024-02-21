package com.marginallyclever.ro3.node;

/**
 * <p>{@link NodePath} stores the uniqueID to a node.  It can be used to find a node in the scene graph.</p>
 * <p>This is made visible to the user as {@link com.marginallyclever.ro3.apps.nodeselector.NodeSelector} and
 * {@link com.marginallyclever.ro3.apps.nodeselector.NodeSelectionDialog}.</p>
 * @param <T> the type of node to allow
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

    public void setSubject(Node node) {
        this.uniqueID = (node == null) ? "" : node.getUniqueID();
    }

    public T getSubject() {
        return owner.getRootNode().findNodeByID(uniqueID,type);
    }

    public Class<T> getType() {
        return type;
    }
}
