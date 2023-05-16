package com.marginallyclever.robotoverlord.renderpanel;

/**
 * Used to notify the listeners that it's time to update.
 *
 * @author Dan Royer
 * @since 2.5.7
 */
public interface UpdateCallback {
    void update(double deltaTime);
}

