package com.marginallyclever.ro3.nodes;

import com.marginallyclever.robotoverlord.components.Component;

import java.util.ArrayList;
import java.util.Iterator;
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
        super();
        this.nodeID = java.util.UUID.randomUUID();
        this.name = name;
    }

    public void addChild(Node child) {
        children.add(child);
        child.setParent(this);
        fireNodeEvent(new NodeEvent(child,NodeEvent.ATTACHED));
        child.onAttach();
        if(child.children.isEmpty()) {
            child.fireNodeEvent(new NodeEvent(child,NodeEvent.READY));
            child.onReady();
        } else {
            for(Node grandchild : child.children) {
                child.addChild(grandchild);
            }
        }
    }

    /**
     * Called after this node is added to its parent.
     */
    protected void onAttach() {}

    public void removeChild(Node child) {
        child.onDetach();
        fireNodeEvent(new NodeEvent(child,NodeEvent.DETACHED));
        children.remove(child);
        child.setParent(null);
    }

    /**
     * Called after this node is removed from its parent.
     */
    protected void onDetach() {}

    /**
     * Called when the node is attached and all children are ready.
     */
    protected void onReady() {}

    public String getName() {
        return name;
    }

    private void setParent(Node node) {
        this.parent = node;
    }

    public Node getParent() {
        return parent;
    }

    /**
     * @return the unique ID of this node.
     */
    public UUID getNodeID() {
        return nodeID;
    }

    /**
     * @param name the new name of this node.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return an iterator so that calling class cannot modify the list.
     */
    public Iterator<Node> getChildren() {
        return children.iterator();
    }

    /**
     * Find the first parent with the given name.
     * @param name the name to match.
     * @return the first parent, or null if none found.
     */
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

    /**
     * Find the first parent of the given type.
     * @param type the type of node to find
     * @return the first parent of the given type, or null if none found.
     */
    public <T extends Node> T findParent(Class<T> type) {
        Node p = parent;
        while(p != null) {
            if(type.isInstance(p)) {
                return type.cast(p);
            }
            p = p.getParent();
        }
        return null;
    }

    /**
     * Find the first child of this node with the given name.
     * @param name the name to match.
     * @return the child, or null if none found.
     */
    public Node findChild(String name) {
        for(Node child : children) {
            if(child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Find the node in the tree with the given path.
     * @param path the path to the node.  can be relative or absolute.  understands ".." to go up one level.
     * @return the node, or null if none found.
     */
    public Node get(String path) {
        String[] parts = path.split("/");
        Node node = this;
        if(parts[0].isEmpty()) {
            node = getRootNode();
        }
        for(String part : parts) {
            if(part.equals("..")) {
                node = node.getParent();
            } else {
                node = node.findChild(part);
            }
            if(node == null) {
                return null;
            }
        }
        return node;
    }

    public Node getRootNode() {
        Node node = this;
        while(node.getParent() != null) {
            node = node.getParent();
        }
        return node;
    }

    /**
     * @return the absolute path to this node.
     */
    public String getAbsolutePath() {
        StringBuilder sb = new StringBuilder();
        Node node = this;
        do {
            sb.insert(0,node.getName());
            sb.insert(0,"/");
            node = node.getParent();
        } while(node != null);
        return sb.toString();
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

    /**
     * Called every frame.
     * @param dt the time since the last frame.
     */
    public void update(double dt) {}
}
