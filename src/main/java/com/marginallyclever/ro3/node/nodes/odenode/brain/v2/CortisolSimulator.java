package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import java.util.List;

public class CortisolSimulator {
    double changeAmount = 1.0 - .01;
    public void releaseCortisol(List<Connection> activeConnections) {
        for (Connection connection : activeConnections) {
            // Weaken or prune the connection
            connection.scaleWeight(changeAmount);
        }
    }
}
