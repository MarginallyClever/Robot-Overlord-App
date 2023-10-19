package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

/**
 * Used to notify the listeners that a robot has been installed.
 *
 * @author Dan Royer
 * @since 2.5.3
 */
public interface RobotLibraryListener {
    /**
     * Called when a robot is installed to the local library.
     */
    void onRobotAdded();
}
