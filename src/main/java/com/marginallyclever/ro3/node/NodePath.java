package com.marginallyclever.ro3.node;

import com.marginallyclever.convenience.PathCalculator;

/**
 * Stores the relative path to a node.  Can be used to find a node in the scene graph.
 * @param <T> the type of node to find
 */
public class NodePath<T extends Node> {
    private String path;
    private final Node owner;
    private final Class<T> type;

    public NodePath(Node owner,Class<T> type) {
        this(owner,type,"");
    }

    public NodePath(Node owner,Class<T> type,String path) {
        this.owner = owner;
        this.type = type;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public T getSubject() {
        return owner.findNodeByPath(path,type);
    }

    public void setRelativePath(T origin, T goal) {
        setPath(PathCalculator.getRelativePath(origin,goal));
    }
}
