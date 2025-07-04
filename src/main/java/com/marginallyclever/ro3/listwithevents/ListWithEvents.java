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

    @SuppressWarnings("unchecked")  // because getListeners is not generic
    protected void fireItemAdded(T item) {
        for (ItemAddedListener<T> listener : listenerList.getListeners(ItemAddedListener.class)) {
            listener.itemAdded(this,item);
        }
    }

    @SuppressWarnings("unchecked")  // because getListeners is not generic
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
        // remove everything except selection.
        if(selection==null) {
            removeAll();
            return;
        }
        for (T item : new ArrayList<>(list)) {
            if(!selection.equals(item)) {
                remove(item);
            }
        }
        // add selection if it's not already in the list.
        if(!list.contains(selection)) {
            add(selection);
        }
    }

    public boolean contains(T n) {
        return list.contains(n);
    }
}