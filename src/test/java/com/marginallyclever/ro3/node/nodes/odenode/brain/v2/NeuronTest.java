package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import javax.vecmath.Vector3d;

import static org.junit.jupiter.api.Assertions.*;

public class NeuronTest {
    @Test
    public void testNeuronInitialization() {
        Neuron neuron = new Neuron(0,0.0, 0.0, 0.0);
        assertEquals(new Vector3d(0.0, 0.0, 0.0), neuron.getPosition());
        assertEquals(0.0, neuron.getInputValue());
        assertEquals(0.0, neuron.getOutputValue());
    }

    @Test
    public void testNeuronActivation() {
        Neuron neuron = new Neuron(0,0.0, 0.0, 0.0);
        neuron.setInputValue(1.0);
        neuron.activate();
        assertEquals(Math.tanh(1.0), neuron.getOutputValue(), 1e-6);
    }

    @Test
    public void testNeuronReset() {
        Neuron neuron = new Neuron(0,0.0, 0.0, 0.0);
        neuron.setInputValue(1.0);
        neuron.activate();
        neuron.reset();
        assertEquals(0.0, neuron.getInputValue());
        assertEquals(0.0, neuron.getOutputValue());
    }

    @Test
    public void testAddOutgoingConnection() {
        Neuron neuron = new Neuron(0,0.0, 0.0, 0.0);
        Neuron neuron2 = new Neuron(1,1.0, 0.0, 0.0);
        Connection connection = new Connection(neuron, neuron2, 0.5);
        neuron.addOutgoingConnection(connection);
        assertEquals(1, neuron.getOutgoingConnections().size());
        assertEquals(connection, neuron.getOutgoingConnections().get(0));
    }

    @Test
    public void testJSON() {
        Neuron before = new Neuron(4,3.0, 2.0, 1.0);
        before.setInputValue(1.0);
        before.activate();
        JSONObject json = before.toJSON();
        Neuron after = new Neuron(0,0,0,0);
        after.fromJSON(json);
        assertEquals(before.getPosition(), after.getPosition());
        assertEquals(before.getInputValue(), after.getInputValue());
        assertEquals(before.getOutputValue(), after.getOutputValue());
    }
}

