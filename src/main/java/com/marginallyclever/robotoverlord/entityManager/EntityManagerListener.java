package com.marginallyclever.robotoverlord.entityManager;

/**
 * An interface for listening to changes in the {@link EntityManager}.
 */
public interface EntityManagerListener {
    void entityManagerEvent(EntityManagerEvent event);
}
