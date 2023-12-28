package com.marginallyclever.ro3.listwithevents;

import java.util.EventListener;

public interface ItemAddedListener<T> extends EventListener {
    void itemAdded(Object source,T item);
}