package com.marginallyclever.ro3;

import com.marginallyclever.ro3.node.Node;

import java.util.EventListener;

/**
 * A listener for scene changes.
 */
public interface SceneChangeListener extends EventListener {
    /**
     * Called before the scene changes.  This is a good time to unregister listeners.
     * @param oldScene the scene that is about to be replaced.
     */
    void beforeSceneChange(Node oldScene);

    /**
     * Called after the scene changes.  This is a good time to register listeners.
     * @param newScene the scene that has just been added.
     */
    void afterSceneChange(Node newScene);
}
