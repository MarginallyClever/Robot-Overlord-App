package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class BrainTest {
    @Test
    public void testBrainInitialization() {
        DopamineSimulator dopamineSimulator = new DopamineSimulator();
        CortisolSimulator cortisolSimulator = new CortisolSimulator();
        Brain brain = new Brain(3, 2, dopamineSimulator, cortisolSimulator);

        assertEquals(3, brain.getInputNeurons().size());
        assertEquals(2, brain.getOutputNeurons().size());
        assertEquals(3 * 2, brain.getConnections().size());
    }

    @Test
    public void testTrain() {
        DopamineSimulator dopamineSimulator = new DopamineSimulator();
        CortisolSimulator cortisolSimulator = new CortisolSimulator();
        Brain brain = new Brain(3, 2, dopamineSimulator, cortisolSimulator);

        List<Double> inputs = new ArrayList<>(List.of(new Double[]{0.5, -0.5, 0.1}));
        List<Double> expectedOutputs = new ArrayList<>(List.of(new Double[]{0.3, -0.1}));

        brain.train(inputs, expectedOutputs);

        // Check if the connections were adjusted properly
        List<Connection> activeConnections = brain.findActiveConnections();
        for (Connection connection : activeConnections) {
            // This is just a simple check to ensure the connections were indeed active
            assertTrue(connection.isActive());
        }
    }

    @Test
    public void testResetNetwork() {
        DopamineSimulator dopamineSimulator = new DopamineSimulator();
        CortisolSimulator cortisolSimulator = new CortisolSimulator();
        Brain brain = new Brain(3, 2, dopamineSimulator, cortisolSimulator);

        brain.resetNetwork();

        for (Neuron neuron : brain.getInputNeurons()) {
            assertEquals(0.0, neuron.getInputValue());
            assertEquals(0.0, neuron.getOutputValue());
        }

        for (Neuron neuron : brain.getOutputNeurons()) {
            assertEquals(0.0, neuron.getInputValue());
            assertEquals(0.0, neuron.getOutputValue());
        }

        for (Connection connection : brain.getConnections()) {
            assertFalse(connection.isActive());
        }
    }
}

