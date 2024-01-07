package com.marginallyclever.ro3.listwithevents;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.Node;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A {@link List} that fires events when items are added or removed.
 * @param <T> The type of item in the list.
 */
public class ListWithEvents<T> {
    private final List<T> list = new ArrayList<>();
    private final EventListenerList listenerList = new EventListenerList();

    public void add(T item) {
        list.add(item);
        fireItemAdded(item);
    }

    public void remove(T item) {
        list.remove(item);
        fireItemRemoved(item);
    }

    public void addItemAddedListener(ItemAddedListener<T> listener) {
        listenerList.add(ItemAddedListener.class, listener);
    }

    public void removeItemAddedListener(ItemAddedListener<T> listener) {
        listenerList.remove(ItemAddedListener.class, listener);
    }

    public void addItemRemovedListener(ItemRemovedListener<T> listener) {
        listenerList.add(ItemRemovedListener.class, listener);
    }

    public void removeItemRemovedListener(ItemRemovedListener<T> listener) {
        listenerList.remove(ItemRemovedListener.class, listener);
    }

    protected void fireItemAdded(T item) {
        for (ItemAddedListener<T> listener : listenerList.getListeners(ItemAddedListener.class)) {
            listener.itemAdded(this,item);
        }
    }

    protected void fireItemRemoved(T item) {
        for (ItemRemovedListener<T> listener : listenerList.getListeners(ItemRemovedListener.class)) {
            listener.itemRemoved(this,item);
        }
    }

    public List<T> getList() {
        return list;
    }

    public void removeAll() {
        while(!list.isEmpty()) {
            remove(list.get(0));
        }
    }

    public int size() {
        return list.size();
    }

    public void addAll(ArrayList<T> selection) {
        for(T item : selection) {
            add(item);
        }
    }

    public void set(List<T> selection) {
        // remove only the items that are not in the new selection
        for(T item : new ArrayList<>(list)) {
            if(!selection.contains(item)) {
                remove(item);
            }
        }
        // add only the items that are not in the old selection
        for(T item : selection) {
            if(!list.contains(item)) {
                add(item);
            }
        }
    }

    public void set(T selection) {
        boolean found=false;
        for( T node : new ArrayList<>(list) ) {
            if(!node.equals(selection)) {
                remove(node);
            } else found=true;
        }
        if(!found && selection!=null) {
            add(selection);
        }
    }
}