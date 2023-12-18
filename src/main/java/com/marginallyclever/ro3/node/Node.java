package com.marginallyclever.ro3.node;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * A node in a tree.
 */
public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private String name;
    private final List<Node> children = new ArrayList<>();
    private Node parent;
    private UUID nodeID;
    private final EventListenerList listeners = new EventListenerList();

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
        fireAttachEvent(child);
        child.onAttach();
        if(child.children.isEmpty()) {
            fireReadyEvent(child);
            child.onReady();
        }/* else {
            for(Node grandchild : child.children) {
                child.addChild(grandchild);
            }
        }*/
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

        for(Node grandchild : child.children) {
            fireAttachEvent(grandchild);
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
     * Called after this node is added to its parent.
     */
    protected void onAttach() {}

    public void removeChild(Node child) {
        child.onDetach();
        fireDetachEvent(child);
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
        if(isNameUsedBySibling(name)) {
            return;
        }
        this.name = name;
        fireRenameEvent(this);
    }

    /**
     * @return an iterator so that calling class cannot modify the list.
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
    public void update(double dt) {}

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(Node.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        // custom stuff
        pane.setLayout(new GridLayout(0,2));

        JTextField nameField = new JTextField(getName());
        nameField.addActionListener(e -> {
            // should not be allowed to match siblings?
            setName(nameField.getText());
        });

        addLabelAndComponent(pane,"Name",nameField);
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
     * A convenience method to add a label and component to a panel that is expected to be built with
     * <code>new GridLayout(0, 2)</code>.
     * @param pane the panel to add to
     * @param labelText the text for the label
     * @param component the component to add
     */
    protected void addLabelAndComponent(JPanel pane, String labelText, JComponent component) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(component);
        pane.add(label);
        pane.add(component);
    }

    /**
     * Find the first child of the given type.
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
     * Find the first sibling of the given type.
     * @param type the type of node to find
     * @return the first sibling of the given type, or null if none found.
     * @param <T> the type of node to find
     */
    public <T extends Node> T findFirstSibling(Class<T> type) {
        if(parent==null) return null;
        for(Node child : parent.children) {
            if(type.isInstance(child)) {
                return type.cast(child);
            }
        }
        return null;
    }

    /**
     * Serialize this node and its children to a JSON object and its children.
     * Classes that override this method should call super.toJSON() first, then add to the object returned.
     * @return the JSON object.
     */
    public JSONObject toJSON() {
        logger.info("Saving {}.",getAbsolutePath());
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
}
