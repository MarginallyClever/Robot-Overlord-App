package com.marginallyclever.robotoverlord.entity;

/**
 * An interface for listening to changes in the {@link EntityManager}.
 */
public interface EntityManagerListener {
    void entityManagerEvent(EntityManagerEvent event);
}
