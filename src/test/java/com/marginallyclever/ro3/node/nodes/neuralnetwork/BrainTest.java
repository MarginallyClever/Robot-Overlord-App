package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BrainTest {
    // test to/from json
    @Test
    public void testToFromJson() {
        Brain b = new Brain();
        b.setLearningRate(0.5);
        b.setForgettingRate(0.25);
        var json = b.toJSON();
        Brain b2 = new Brain();
        b2.fromJSON(json);
        Assertions.assertEquals(b.getLearningRate(), b2.getLearningRate());
        Assertions.assertEquals(b.getForgettingRate(), b2.getForgettingRate());
    }

    @Test
    public void fireNeuron() {
        var brain = new Brain();
        var a = new Neuron();
        var b = new Neuron();
        var s = new Synapse();
        s.setFrom(a);
        s.setTo(b);
        s.setWeight(1.0);
        a.setBias(0.0);
        a.setSum(1.0);

        brain.addChild(a);
        brain.addChild(b);
        brain.addChild(s);
        brain.scan();
        brain.step();

    }
}
