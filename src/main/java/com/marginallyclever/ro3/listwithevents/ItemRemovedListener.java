package com.marginallyclever.ro3.listwithevents;

import java.util.EventListener;

public interface ItemRemovedListener<T> extends EventListener {
    void itemRemoved(T item);
}