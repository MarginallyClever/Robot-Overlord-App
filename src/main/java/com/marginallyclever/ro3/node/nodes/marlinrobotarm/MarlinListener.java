package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import java.util.EventListener;

/**
 * A listener for messages from the {@link MarlinRobotArm}.
 */
public interface MarlinListener extends EventListener {
    /**
     * Called when a message is received from the Marlin robot.
     * @param message the event
     */
    void messageFromMarlin(String message);
}
