package com.marginallyclever.robotoverlord.renderpanel;

/**
 * Used to notify the listeners that it's time to update.
 *
 */
public interface UpdateCallback {
    void update(double deltaTime);
}

