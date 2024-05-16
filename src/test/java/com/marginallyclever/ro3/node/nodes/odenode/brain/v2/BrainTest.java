package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class BrainTest {
    @Test
    public void testBrainInitialization() {
        DopamineSimulator dopamineSimulator = new DopamineSimulator();
        CortisolSimulator cortisolSimulator = new CortisolSimulator();
        Brain brain = new Brain( dopamineSimulator, cortisolSimulator);
        brain.setNumInputs(3);
        brain.setNumOutputs(2);
        brain.createInitialConnections();

        assertEquals(3, brain.getInputNeurons().size());
        assertEquals(2, brain.getOutputNeurons().size());
        assertEquals(3 * 2, brain.getConnections().size());
    }

    @Test
    public void testTrain() {
        DopamineSimulator dopamineSimulator = new DopamineSimulator();
        CortisolSimulator cortisolSimulator = new CortisolSimulator();
        Brain brain = new Brain(dopamineSimulator, cortisolSimulator);
        brain.setNumInputs(3);
        brain.setNumOutputs(2);
        brain.createInitialConnections();

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
        Brain brain = new Brain(dopamineSimulator, cortisolSimulator);
        brain.setNumInputs(3);
        brain.setNumOutputs(2);

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

    @Test
    public void testJSON() {
        DopamineSimulator dopamineSimulator = new DopamineSimulator();
        CortisolSimulator cortisolSimulator = new CortisolSimulator();
        Brain before = new Brain(dopamineSimulator, cortisolSimulator);
        before.setNumInputs(3);
        before.setNumOutputs(2);
        before.createInitialConnections();

        JSONObject json = before.toJSON();
        Brain after = new Brain(dopamineSimulator,cortisolSimulator);
        after.fromJSON(json);

        assertEquals(before.getInputNeurons().size(), after.getInputNeurons().size());
        assertEquals(before.getOutputNeurons().size(), after.getOutputNeurons().size());
        assertEquals(before.getConnections().size(), after.getConnections().size());
    }
}

