package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NeuronTest {
    // test to/from json
    @Test
    public void testToFromJson() {
        Neuron n = new Neuron();
        n.setBias(1.0);
        n.setModulation(0.5);
        n.setSum(0.25);
        n.setNeuronType(Neuron.Type.Exciter);
        var json = n.toJSON();
        Neuron n2 = new Neuron();
        n2.fromJSON(json);
        Assertions.assertEquals(n.getBias(), n2.getBias());
        Assertions.assertEquals(n.getModulation(), n2.getModulation());
        Assertions.assertEquals(n.getSum(), n2.getSum());
        Assertions.assertEquals(n.getNeuronType(), n2.getNeuronType());
    }
}
