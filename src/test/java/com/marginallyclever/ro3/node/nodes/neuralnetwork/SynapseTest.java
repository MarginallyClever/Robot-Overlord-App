package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SynapseTest {
    // test to/from json
    @Test
    public void testToFromJson() {
        Synapse s = new Synapse();
        s.setWeight(1.0);
        var json = s.toJSON();
        Synapse s2 = new Synapse();
        s2.fromJSON(json);
        Assertions.assertEquals(s.getWeight(), s2.getWeight());
    }
}
