package com.marginallyclever.robotoverlord;

public interface SceneChangeListener {
    void addEntityToParent(Entity parent,Entity child);
    void removeEntityFromParent(Entity parent,Entity child);
}
