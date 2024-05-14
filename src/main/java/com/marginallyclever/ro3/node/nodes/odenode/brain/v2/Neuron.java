package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

public class Neuron {
    private final Vector3d position = new Vector3d();
    private double inputValue;
    private double outputValue;
    private final List<Connection> outgoingConnections = new ArrayList<>();

    public Neuron(double x, double y, double z) {
        this.inputValue = 0.0;
        this.outputValue = 0.0;
        position.set(x, y, z);
    }

    public Vector3d getPosition() {
        return position;
    }

    public double getInputValue() {
        return inputValue;
    }

    public void setInputValue(double inputValue) {
        this.inputValue = inputValue;
    }

    public double getOutputValue() {
        return outputValue;
    }

    public void addInputValue(double inputValue) {
        this.inputValue += inputValue;
    }

    public void activate() {
        // Using tanh as the activation function to normalize between -1 and 1
        this.outputValue = Math.tanh(this.inputValue);
        // Propagate the output value to outgoing connections
        for (Connection connection : outgoingConnections) {
            connection.propagate();
        }
    }

    public void reset() {
        this.inputValue = 0.0;
        this.outputValue = 0.0;
    }

    public void addOutgoingConnection(Connection connection) {
        outgoingConnections.add(connection);
    }

    public List<Connection> getOutgoingConnections() {
        return outgoingConnections;
    }
}
