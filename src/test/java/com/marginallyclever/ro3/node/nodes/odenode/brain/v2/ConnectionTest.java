package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest {

    @Test
    public void testConnectionInitialization() {
        Neuron neuron1 = new Neuron(0.0, 0.0, 0.0);
        Neuron neuron2 = new Neuron(1.0, 0.0, 0.0);
        Connection connection = new Connection(neuron1, neuron2, 0.5);
        assertEquals(neuron1, connection.getFromNeuron());
        assertEquals(neuron2, connection.getToNeuron());
        assertEquals(0.5, connection.getWeight(), 1e-6);
    }

    @Test
    public void testConnectionPropagation() {
        Neuron neuron1 = new Neuron(0.0, 0.0, 0.0);
        Neuron neuron2 = new Neuron(1.0, 0.0, 0.0);
        Connection connection = new Connection(neuron1, neuron2, 0.5);
        neuron1.setInputValue(1.0);
        neuron1.activate();
        connection.propagate();
        assertTrue(connection.isActive());
        assertEquals(0.5 * Math.tanh(1.0), neuron2.getInputValue(), 1e-6);
    }

    @Test
    public void testConnectionReset() {
        Neuron neuron1 = new Neuron(0.0, 0.0, 0.0);
        Neuron neuron2 = new Neuron(1.0, 0.0, 0.0);
        Connection connection = new Connection(neuron1, neuron2, 0.5);
        connection.propagate();
        connection.reset();
        assertFalse(connection.isActive());
    }

    @Test
    public void testStrengthenConnection() {
        Neuron neuron1 = new Neuron(0.0, 0.0, 0.0);
        Neuron neuron2 = new Neuron(1.0, 0.0, 0.0);
        Connection connection = new Connection(neuron1, neuron2, 0.5);
        connection.scaleWeight(1.1);
        assertEquals(0.55, connection.getWeight(), 1e-6);
    }

    @Test
    public void testWeakenConnection() {
        Neuron neuron1 = new Neuron(0.0, 0.0, 0.0);
        Neuron neuron2 = new Neuron(1.0, 0.0, 0.0);
        Connection connection = new Connection(neuron1, neuron2, 0.5);
        connection.scaleWeight(0.9);
        assertEquals(0.45, connection.getWeight(), 1e-6);
    }
}
