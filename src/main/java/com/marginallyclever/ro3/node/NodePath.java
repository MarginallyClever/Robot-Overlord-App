package com.marginallyclever.ro3.node;

import javax.swing.event.EventListenerList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * <p>{@link NodePath} stores the uniqueID to a node.  It can be used to find a node in the scene graph.</p>
 * <p>This is made visible to the user as {@link com.marginallyclever.ro3.apps.nodeselector.NodeSelector} and
 * {@link com.marginallyclever.ro3.apps.nodeselector.NodeSelectionDialog}.</p>
 * @param <T> the type of node to allow
 */
public class NodePath<T extends Node> {
    public static final String PROP_UNIQUEID = "uniqueID";

    private String uniqueID;
    private final Node owner;
    private final Class<T> type;
    private final EventListenerList listenerList = new EventListenerList();

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
        if(uniqueID.equals(this.uniqueID)) return;

        String oldID = this.uniqueID;
        this.uniqueID = uniqueID;
        firePropertyChange(PROP_UNIQUEID, oldID, uniqueID);
    }

    public void setUniqueIDByNode(Node node) {
        setUniqueID((node == null) ? "" : node.getUniqueID());
    }

    public T getSubject() {
        return owner.getRootNode().findNodeByID(uniqueID,type);
    }

    public Class<T> getType() {
        return type;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent event = null;
        for( var listener : listenerList.getListeners(PropertyChangeListener.class) ) {
            if(event == null) {
                // Lazily create the event
                event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
            }
            listener.propertyChange(event);
        }
    }
}
