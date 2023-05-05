package com.marginallyclever.robotoverlord;

/**
 * An interface for listening to changes in the {@link EntityManager}.
 */
public interface EntityManagerListener {
    void addEntityToParent(Entity parent,Entity child);
    void removeEntityFromParent(Entity parent,Entity child);
}
