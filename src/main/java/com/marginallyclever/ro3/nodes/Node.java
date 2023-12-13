package com.marginallyclever.ro3.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A node in a tree.
 */
public class Node {
    private String name;
    private final List<Node> children = new ArrayList<>();
    private Node parent;
    private UUID nodeID;

    private final List<NodeListener> listeners = new ArrayList<>();

    public Node() {
        this("Node");
    }

    public Node(String name) {
        this.nodeID = java.util.UUID.randomUUID();
        this.name = name;
    }

    public void addChild(Node child) {
        children.add(child);
        child.setParent(this);
        fireNodeEvent(new NodeEvent(child,NodeEvent.ATTACHED));
        child.onAttach();
    }

    /**
     * Called after this node is added to its parent.
     */
    private void onAttach() {}

    public void removeChild(Node child) {
        child.onDetach();
        fireNodeEvent(new NodeEvent(child,NodeEvent.DETACHED));
        children.remove(child);
        child.setParent(null);
    }

    /**
     * Called after this node is removed from its parent.
     */
    private void onDetach() {}

    public String getName() {
        return name;
    }

    private void setParent(Node node) {
        this.parent = node;
    }

    public Node getParent() {
        return parent;
    }

    public UUID getNodeID() {
        return nodeID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node findParent(String name) {
        Node p = parent;
        while(p != null) {
            if(p.getName().equals(name)) {
                return p;
            }
            p = p.getParent();
        }
        return null;
    }

    public Node findChild(String name) {
        for(Node child : children) {
            if(child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public Node findParent(Class<? extends Node> type) {
        Node p = parent;
        while(p != null) {
            if(type.isInstance(p)) {
                return p;
            }
            p = p.getParent();
        }
        return null;
    }

    public void addNodeListener(NodeListener listener) {
        listeners.add(listener);
    }

    public void removeNodeListener(NodeListener listener) {
        listeners.remove(listener);
    }

    public void fireNodeEvent(NodeEvent event) {
        for(NodeListener listener : listeners) {
            listener.nodeEvent(event);
        }
    }
}
