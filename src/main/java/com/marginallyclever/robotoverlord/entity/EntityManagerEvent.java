package com.marginallyclever.robotoverlord.entity;

/**
 * An interface for listening to changes in the {@link EntityManager}.
 */
public class EntityManagerEvent {
    /**
     * When a child is added to a parent.  parent and child will be valid.
     */
    public static final int ENTITY_ADDED = 0;
    /**
     * When a child is removed from a parent.  parent and child will be valid.
     */
    public static final int ENTITY_REMOVED = 1;
    /**
     * When an entity is renamed.  child will be the entity that was renamed.
     */
    public static final int ENTITY_RENAMED = 2;

    public final int type;
    public final Entity parent;
    public final Entity child;

    public EntityManagerEvent(int type,Entity child,Entity parent) {
        this.type = type;
        this.parent = parent;
        this.child = child;
    }
}
