package com.marginallyclever.ro3.nodes;

/**
 * {@link NodeEvent} is for {@link Node} events sent to {@link NodeListener}s.
 */
public record NodeEvent(Node source,int type) {
    public static final int ATTACHED = 0;
    public static final int DETACHED = 1;
    public static final int READY = 2;
}
