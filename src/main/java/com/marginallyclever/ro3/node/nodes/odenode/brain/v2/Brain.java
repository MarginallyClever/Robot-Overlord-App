package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import java.util.ArrayList;
import java.util.List;

public class Brain {
    private List<Neuron> inputNeurons = new ArrayList<>();
    private List<Neuron> outputNeurons = new ArrayList<>();
    private List<Neuron> hiddenNeurons = new ArrayList<>();
    private List<Connection> connections = new ArrayList<>();
    private final DopamineSimulator dopamineSimulator;
    private final CortisolSimulator cortisolSimulator;

    public Brain(int numInputs, int numOutputs, DopamineSimulator dopamineSimulator, CortisolSimulator cortisolSimulator) {
        super();
        this.dopamineSimulator = dopamineSimulator;
        this.cortisolSimulator = cortisolSimulator;

        // Initialize input neurons on the x=0 plane
        for (int i = 0; i < numInputs; i++) {
            inputNeurons.add(new Neuron(0, i, 0));
        }

        // Initialize output neurons on the y=0 plane
        for (int i = 0; i < numOutputs; i++) {
            outputNeurons.add(new Neuron(i, 0, 0));
        }

        // Create initial connections between input and output neurons
        for (Neuron inputNeuron : inputNeurons) {
            for (Neuron outputNeuron : outputNeurons) {
                Connection connection = new Connection(inputNeuron, outputNeuron, Math.random() * 0.2 - 0.1);
                addConnection(connection);
            }
        }
    }

    public void setInputs(List<Double> inputs) {
        if(inputs.size() != inputNeurons.size()) {
            throw new IllegalArgumentException("Number of inputs must match the number of input neurons");
        }
        // Feed the input through the network
        for (int i = 0; i < inputs.size(); i++) {
            inputNeurons.get(i).setInputValue(inputs.get(i));
        }
    }

    /**
     * Train the network with a set of inputs and expected outputs
     * @param inputs List of input values
     * @param expectedOutputs List of expected output values
     */
    public void train(List<Double> inputs, List<Double> expectedOutputs) {
        setInputs(inputs);

        propagate();

        if(expectedOutputs.size() != outputNeurons.size()) {
            throw new IllegalArgumentException("Number of expected outputs must match the number of output neurons");
        }
        double[] output = getOutputs();
        for (int i = 0; i < expectedOutputs.size(); i++) {
            double d = outputNeurons.get(i).getOutputValue();
            double error = expectedOutputs.get(i) - d;

            // what happens here?
        }
    }

    private void propagate() {
        // Activate input neurons
        for (Neuron neuron : inputNeurons) {
            neuron.activate();
        }
        // Activate hidden and output neurons
        for (Neuron neuron : hiddenNeurons) {
            neuron.activate();
        }
        for (Neuron neuron : outputNeurons) {
            neuron.activate();
        }
    }

    public double[] getOutputs() {
        // Evaluate the network's output based on the input
        double[] output = new double[outputNeurons.size()];
        for (int i = 0; i < outputNeurons.size(); i++) {
            output[i] = outputNeurons.get(i).getOutputValue();
        }
        return output;
    }

    public List<Connection> findActiveConnections() {
        List<Connection> activeConnections = new ArrayList<>();
        for (Connection connection : connections) {
            if (connection.isActive()) {
                activeConnections.add(connection);
            }
        }
        return activeConnections;
    }

    public void resetNetwork() {
        for (Neuron neuron : inputNeurons) {
            neuron.reset();
        }
        for (Neuron neuron : hiddenNeurons) {
            neuron.reset();
        }
        for (Neuron neuron : outputNeurons) {
            neuron.reset();
        }
        for (Connection connection : connections) {
            connection.reset();
        }
    }

    public void addNeuron(Neuron neuron) {
        hiddenNeurons.add(neuron);
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
        connection.getFromNeuron().addOutgoingConnection(connection);
    }

    public List<Neuron> getInputNeurons() {
        return inputNeurons;
    }

    public List<Neuron> getOutputNeurons() {
        return outputNeurons;
    }

    public List<Connection> getConnections() {
        return connections;
    }
}

