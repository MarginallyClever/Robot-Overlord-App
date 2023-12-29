package com.marginallyclever.ro3.listwithevents;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.List;

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
        for (ItemAddedListener<T> l : listenerList.getListeners(ItemAddedListener.class)) {
            l.itemAdded(this,item);
        }
    }

    protected void fireItemRemoved(T item) {
        for (ItemRemovedListener<T> l : listenerList.getListeners(ItemRemovedListener.class)) {
            l.itemRemoved(this,item);
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

    public void set(ArrayList<T> selection) {
        // remove only the items that are not in the new selection
        for(T item : list) {
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
}