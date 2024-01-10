package com.marginallyclever.robotoverlord.renderpanel;

/**
 * Used to notify the listeners that it's time to update.
 *
 */
@Deprecated public interface UpdateCallback {
    void update(double deltaTime);
}

