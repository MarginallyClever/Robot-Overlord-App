package com.marginallyclever.ro3.listwithevents;

import java.util.EventListener;

/**
 * An interface for listening to item removal events from a {@link ListWithEvents}.
 * @param <T> the type of the items in the list
 */
public interface ItemRemovedListener<T> extends EventListener {
    /**
     * Called when an item is removed from the list.
     * @param source the list that was modified
     * @param item the item that was removed
     */
    void itemRemoved(Object source,T item);
}