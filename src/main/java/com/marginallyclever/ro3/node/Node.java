package com.marginallyclever.ro3.node;

import com.marginallyclever.ro3.Registry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>{@link Node} is the base class for all nodes in the scene tree.</p>
 * <p>Each Node can have a parent and multiple children, forming a tree-like structure. This class provides several
 * functionalities:</p>
 * <ul>
 *  <li>Nodes have a unique IDs and a name.</li>
 *  <li>Nodes can be attached or detached from their parents.</li>
 *  <li>Nodes can be renamed.</li>
 *  <li>Nodes can be serialized to and from JSON format.</li>
 *  <li>Nodes can be updated every frame, which is useful for animations or game logic.</li>
 *  <li>Nodes can be searched by their name or type.</li>
 *  <li>Nodes can be found by their absolute path in the tree.</li>
 *  <li>Nodes can be checked if their name is used by a sibling.</li>
 *  <li>Nodes can be found by their unique ID.</li>
 *  <li>Nodes can be found by their path, which can be relative or absolute.</li>
 * </ul>
 * <p>This class also provides several events:</p>
 * <ul>
 *     <li>{@link NodeAttachListener}: called when a node is attached to a parent.</li>
 *     <li>{@link NodeDetachListener}: called when a node is detached from a parent.</li>
 *     <li>{@link NodeReadyListener}: called when a node is attached and all children are ready.</li>
 *     <li>{@link NodeRenameListener}: called when a node is renamed.</li>
 * </ul>
 * <p>Nodes can be serialized to and from JSON.</p>
 */
public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private String name;
    private final List<Node> children = new ArrayList<>();
    private Node parent;
    private UUID nodeID;

    protected final EventListenerList listeners = new EventListenerList();

    public Node() {
        this("Node");
    }

    public Node(String name) {
        super();
        this.nodeID = java.util.UUID.randomUUID();
        this.name = name;
    }

    /**
     * Append a child to this node.
     * @param child the child to add.
     */
    public void addChild(Node child) {
        addChild(children.size(),child);
    }

    /**
     * Add a child to this node at the given index.
     * @param index the index to add the child at.
     * @param child the child to add.
     */
    public void addChild(int index,Node child) {
        if(index<0|| index>children.size()) {
            throw new IndexOutOfBoundsException("Index "+index+" is out of bounds.");
        }

        children.add(index,child);
        child.setParent(this);
        child.onAttach();
        fireAttachEvent(child);
        child.onReady();
        fireReadyEvent(child);
    }

    public void removeChild(Node child) {
        children.remove(child);
        child.setParent(null);
        child.onDetach();
        fireDetachEvent(child);
    }

    private void fireReadyEvent(Node child) {
        for(NodeReadyListener listener : listeners.getListeners(NodeReadyListener.class)) {
            listener.nodeReady(child);
        }
    }

    private void fireAttachEvent(Node child) {
        for(NodeAttachListener listener : listeners.getListeners(NodeAttachListener.class)) {
            listener.nodeAttached(child);
        }
    }

    private void fireDetachEvent(Node child) {
        for(NodeDetachListener listener : listeners.getListeners(NodeDetachListener.class)) {
            listener.nodeDetached(child);
        }
    }

    private void fireRenameEvent(Node child) {
        for(NodeRenameListener listener : listeners.getListeners(NodeRenameListener.class)) {
            listener.nodeRenamed(child);
        }
    }

    /**
     * Called after this {@link Node} is added to a new parent {@link Node}.
     */
    protected void onAttach() {}

    /**
     * Called after this {@link Node} is removed from its parent {@link Node}.
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
    public String getUniqueID() {
        return nodeID.toString();
    }

    /**
     * @param name the new name of this node.
     * @throws IllegalArgumentException if the new name is already used by a sibling.
     */
    public void setName(String name) {
        if(isNameUsedBySibling(name)) {
            throw new IllegalArgumentException("Name "+name+" is already used by a sibling.");
        }
        this.name = name;
        fireRenameEvent(this);
    }

    /**
     * @return the original list.  This is not a copy.  This is dangerous!
     */
    public List<Node> getChildren() {
        return children;
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
        return findChild(name,1);
    }

    /**
     * Find the first child of this node with the given name.
     * @param name the name to match.
     * @param maxDepth the maximum depth to search.
     * @return the child, or null if none found.
     */
    public Node findChild(String name, int maxDepth) {
        if(maxDepth==0) return null;

        for(Node child : children) {
            if(child.getName().equals(name)) {
                return child;
            }
            Node found = child.findChild(name,maxDepth-1);
            if(found!=null) return found;
        }
        return null;
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

    public void addAttachListener(NodeAttachListener listener) {
        listeners.add(NodeAttachListener.class,listener);
    }

    public void removeAttachListener(NodeAttachListener listener) {
        listeners.remove(NodeAttachListener.class,listener);
    }

    public void addDetachListener(NodeDetachListener listener) {
        listeners.add(NodeDetachListener.class,listener);
    }

    public void removeDetachListener(NodeDetachListener listener) {
        listeners.remove(NodeDetachListener.class,listener);
    }

    public void addReadyListener(NodeReadyListener listener) {
        listeners.add(NodeReadyListener.class,listener);
    }

    public void removeReadyListener(NodeReadyListener listener) {
        listeners.remove(NodeReadyListener.class,listener);
    }

    public void addRenameListener(NodeRenameListener listener) {
        listeners.add(NodeRenameListener.class,listener);
    }

    public void removeRenameListener(NodeRenameListener listener) {
        listeners.remove(NodeRenameListener.class,listener);
    }

    /**
     * Called every frame.
     * @param dt the time since the last frame.
     */
    public void update(double dt) {
        for(Node child : children) {
            child.update(dt);
        }
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JPanel> list) {
        list.add(new NodePanel(this));
    }

    /**
     * @param newName the new name to check
     * @return true if the new name is already used by a sibling
     */
    public boolean isNameUsedBySibling(String newName) {
        // Check if the new name is already used by a sibling
        Node parent = getParent();
        if (parent != null) {
            for (Node sibling : parent.getChildren()) {
                if (sibling != this && sibling.getName().equals(newName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Find the first child of the given type.  The type must be an exact match - it will not match subclasses.
     * @param type the type of node to find
     * @return the first sibling of the given type, or null if none found.
     * @param <T> the type of node to find
     */
    public <T extends Node> T findFirstChild(Class<T> type) {
        for(Node child : children) {
            if(type.isInstance(child)) {
                return type.cast(child);
            }
        }
        return null;
    }

    /**
     * Find the first sibling of the given type.  The type must be an exact match - it will not match subclasses.
     * @param type the type of node to find
     * @return the first sibling of the given type, or null if none found.
     * @param <T> the type of node to find
     */
    public <T extends Node> T findFirstSibling(Class<T> type) {
        return (parent==null) ? null : parent.findFirstChild(type);
    }

    /**
     * Serialize this node and its children to a JSON object and its children.
     * Classes that override this method should call super.toJSON() first, then add to the object returned.
     * @return the JSON object.
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("type",getClass().getSimpleName());
        json.put("name",name);
        json.put("nodeID",nodeID.toString());
        JSONArray childrenArray = new JSONArray();
        for (Node child : this.children) {
            childrenArray.put(child.toJSON());
        }
        json.put("children",childrenArray);
        return json;
    }

    /**
     * Deserialize this node and its children from a JSON object and its children.
     * Classes that override this method should call super.fromJSON().  When they do it will trigger the creation of
     * child nodes.  The child nodes will then call their own fromJSON() methods.
     * @param from the JSON object to read from.
     */
    public void fromJSON(JSONObject from) {
        name = from.getString("name");
        nodeID = UUID.fromString(from.getString("nodeID"));
        children.clear();
        for (Object o : from.getJSONArray("children")) {
            JSONObject child = (JSONObject) o;
            Node n = Registry.nodeFactory.create(child.getString("type"));
            if(n==null) {
                logger.error("{}: Could not create type {}.",getAbsolutePath(),child.getString("type"));
                n = new Node();
            }
            n.fromJSON(child);
            addChild(n);
        }
    }

    /**
     * @param subject the node to search for
     * @return true if the given node is a parent of this node.
     */
    public boolean hasParent(Node subject) {
        Node p = parent;
        while(p != null) {
            if(p == subject) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    /**
     * Everybody in this tree gets a new unique ID.
     */
    public void witnessProtection() {
        logger.debug("Witness Protection for {}.",getAbsolutePath());
        List<Node> toScan = new ArrayList<>();
        toScan.add(this);
        while(!toScan.isEmpty()) {
            Node n = toScan.removeFirst();
            n.nodeID = UUID.randomUUID();
            toScan.addAll(n.getChildren());
        }
    }

    /**
     * Depth-first search for a node with a matching ID and type.  Type match can be any subclass.
     * @param nodeID the ID to search for
     * @param type the type of node to search for
     * @return the first node found with a matching ID and type, or null if none found.
     * @param <T> the type of node to search for
     */
    public <T extends Node> T findNodeByID(String nodeID, Class<T> type) {
        List<Node> toScan = new ArrayList<>();
        toScan.add(this);
        while(!toScan.isEmpty()) {
            Node node = toScan.removeFirst();
            if(type.isInstance(node)) {
                if(node.getUniqueID().equals(nodeID)) {
                    return type.cast(node);
                }
            }
            toScan.addAll(node.getChildren());
        }
        return null;
    }

    /**
     * Find the first node with the given path.  It can be relative or absolute.  Understands ".." to go up one level.
     * Understands "." to stay at the current level.
     * @param target the path to the node.
     * @param type the type of node to search for
     * @return the first node found with a matching ID and type, or null if none found.
     * @param <T> the type of node to search for
     */
    protected <T extends Node> T findNodeByPath(String target, Class<T> type) {
        if(target==null) return null;

        String[] parts = target.split("/");
        Node node = this;
        int i=0;
        if(parts[0].isEmpty()) {
            node = getRootNode();
            ++i;
        }
        for(;i<parts.length;++i) {
            String part = parts[i];
            if(part.equals("..")) {
                node = node.getParent();
            } else if(!part.equals(".")) {
                node = node.findChild(part);
            } // else if part.equals(".") do nothing.
            if(node == null) {
                return null;
            }
        }
        if(type.isInstance(node)) {
            return type.cast(node);
        }
        return null;
    }

    /**
     * Find the node in the tree with the given path.
     * @param path the path to the node.  can be relative or absolute.  understands ".." to go up one level.
     * @return the node, or null if none found.
     */
    public Node findByPath(String path) {
        return findNodeByPath(path,Node.class);
    }

    /**
     * Set a custom icon for this node.
     * @return the icon, or null if none.
     */
    public Icon getIcon() {
        return null;
    }
}
