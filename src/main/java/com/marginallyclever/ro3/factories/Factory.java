package com.marginallyclever.ro3.factories;

import com.marginallyclever.ro3.apps.viewport.OpenGL3Resource;
import com.marginallyclever.ro3.listwithevents.ListListener;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for factories that manage resources with different lifetimes.
 * Factories extending this class should implement the {@link #removeSceneResources()} method to clear resources
 * that are not meant to persist for the lifetime of the application.
 */
public abstract class Factory {
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * Clears any cached resources that are not meant to persist for the lifetime of the application.
     * Typically, this would clear resources with a lifetime of SCENE, while retaining those with a lifetime of APPLICATION.
     */
    public abstract void removeSceneResources();

    public void addItemListener(ListListener<?> listener) {
        listenerList.add(ListListener.class, listener);
    }

    public void removeItemListener(ListListener<?> listener) {
        listenerList.remove(ListListener.class, listener);
    }
}
