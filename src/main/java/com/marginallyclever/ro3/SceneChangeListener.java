package com.marginallyclever.ro3;

import com.marginallyclever.ro3.node.Node;

import java.util.EventListener;

public interface SceneChangeListener extends EventListener {
    void beforeSceneChange(Node oldScene);
    void afterSceneChange(Node newScene);
}
