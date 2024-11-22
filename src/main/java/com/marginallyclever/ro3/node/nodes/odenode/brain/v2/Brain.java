package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Brain {
    private final List<Neuron> inputNeurons = new ArrayList<>();
    private final List<Neuron> outputNeurons = new ArrayList<>();
    private final List<Neuron> neurons = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final DopamineSimulator dopamineSimulator;
    private final CortisolSimulator cortisolSimulator;

    public Brain(DopamineSimulator dopamineSimulator, CortisolSimulator cortisolSimulator) {
        super();
        this.dopamineSimulator = dopamineSimulator;
        this.cortisolSimulator = cortisolSimulator;
    }

    public void setNumInputs(int numInputs) {
        // Initialize input neurons on the x=0 plane
        for (int i = 0; i < numInputs; i++) {
            var n = new Neuron(neurons.size()+i,0, i, 0);
            inputNeurons.add(n);
            neurons.add(n);
        }
    }

    public void setNumOutputs(int numOutputs) {
        // Initialize output neurons on the y=0 plane
        for (int i = 0; i < numOutputs; i++) {
            var n = new Neuron(neurons.size()+i, i,0, 0);
            outputNeurons.add(n);
            neurons.add(n);
        }
    }

    public void createInitialConnections() {
        // Create initial connections between input and output neurons
        for (Neuron inputNeuron : inputNeurons) {
            for (Neuron outputNeuron : outputNeurons) {
                Connection connection = new Connection(inputNeuron, outputNeuron, 0.000001);
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
        resetConnections();
        setInputs(inputs);
        propagate();

        // Evaluate the network's output based on the input
        if(expectedOutputs.size() != outputNeurons.size()) {
            throw new IllegalArgumentException("Number of expected outputs must match the number of output neurons");
        }
        double totalError = measureTotalError(expectedOutputs);

        // what happens here?

    }

    private double measureTotalError(List<Double> expectedOutputs) {
        double totalError = 0;
        for (int i = 0; i < expectedOutputs.size(); i++) {
            double error = expectedOutputs.get(i) - getOutput(i);
            totalError += Math.abs(error);
        }
        return totalError;
    }

    public void propagate() {
        // Activate input neurons
        for (Neuron neuron : neurons) {
            neuron.activate();
        }

        // Propagate the output value to outgoing connections
        for (Connection connection : connections) {
            connection.propagate();
        }
    }

    public double getOutput(int index) {
        return outputNeurons.get(index).getOutputValue();
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
        resetNeurons();
        resetConnections();
    }

    public void resetNeurons() {
        for (Neuron neuron : neurons) {
            neuron.reset();
        }
    }

    public void resetConnections() {
        for (Connection connection : connections) {
            connection.reset();
        }
    }

    public void addNeuron(Neuron neuron) {
        neurons.add(neuron);
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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("neurons", getNeuronsAsJSON(neurons));
        json.put("connections", getConnectionsAsJSON(connections));
        json.put("inputNeurons", getNeuronListAsJSON(inputNeurons));
        json.put("outputNeurons", getNeuronListAsJSON(outputNeurons));
        return json;
    }

    private JSONArray getNeuronListAsJSON(List<Neuron> neurons) {
        JSONArray json = new JSONArray();
        for (Neuron neuron : neurons) {
            json.put(neuron.getID());
        }
        return json;
    }

    private JSONArray getConnectionsAsJSON(List<Connection> connections) {
        JSONArray json = new JSONArray();
        for (int i = 0; i < connections.size(); i++) {
            Connection connection = connections.get(i);
            JSONObject connectionJSON = new JSONObject();
            connectionJSON.put("fromNeuron", connection.getFromNeuron().getID());
            connectionJSON.put("toNeuron", connection.getToNeuron().getID());
            connectionJSON.put("weight", connection.getWeight());
            json.put(connectionJSON);
        }
        return json;
    }

    private JSONArray getNeuronsAsJSON(List<Neuron> neurons) {
        JSONArray json = new JSONArray();
        for (Neuron neuron : neurons) {
            json.put(neuron.toJSON());
        }
        return json;
    }

    public void setInput(int i, double value) {
        inputNeurons.get(i).setInputValue(value);
    }

    public void fromJSON(JSONObject json) {
        JSONArray neuronList = json.getJSONArray("neurons");
        for (int i = 0; i < neuronList.length(); i++) {
            Neuron neuron = new Neuron(i,0,0,0);
            neurons.add(neuron);
            neuron.fromJSON(neuronList.getJSONObject(i));
        }

        JSONArray connectionsJSON = json.getJSONArray("connections");
        for (int i = 0; i < connectionsJSON.length(); i++) {
            JSONObject conn = connectionsJSON.getJSONObject(i);
            int fromNeuronID = conn.getInt("fromNeuron");
            int toNeuronID = conn.getInt("toNeuron");
            double weight = conn.getDouble("weight");
            Connection connection = new Connection(neurons.get(fromNeuronID), neurons.get(toNeuronID), weight);
            connections.add(connection);
        }

        JSONArray inputNeuronIDs = json.getJSONArray("inputNeurons");
        for (int i = 0; i < inputNeuronIDs.length(); i++) {
            int id = inputNeuronIDs.getInt(i);
            inputNeurons.add(neurons.get(id));
        }

        JSONArray outputNeuronIDs = json.getJSONArray("outputNeurons");
        for (int i = 0; i < outputNeuronIDs.length(); i++) {
            int id = outputNeuronIDs.getInt(i);
            outputNeurons.add(neurons.get(id));
        }
    }
}

