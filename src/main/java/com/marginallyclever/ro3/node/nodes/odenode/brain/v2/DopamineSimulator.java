package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import java.util.List;

public class DopamineSimulator {
    double changeAmount = 1.0 + .01;

    public void releaseDopamine(List<Connection> activeConnections) {
        for (Connection connection : activeConnections) {
            // Strengthen the connection or add neurons along the pathway
            connection.scaleWeight(changeAmount);
        }
        // Optionally, add new neurons to reinforce successful pathways
    }
}
