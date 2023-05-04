package com.marginallyclever.robotoverlord;

public interface EntityManagerListener {
    void addEntityToParent(Entity parent,Entity child);
    void removeEntityFromParent(Entity parent,Entity child);
}
