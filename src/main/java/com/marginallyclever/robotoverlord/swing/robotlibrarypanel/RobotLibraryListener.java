package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

/**
 * Used to notify the listeners that a robot has been installed.
 *
 */
public interface RobotLibraryListener {
    /**
     * Called when a robot is installed to the local library.
     */
    void onRobotAdded();
}
