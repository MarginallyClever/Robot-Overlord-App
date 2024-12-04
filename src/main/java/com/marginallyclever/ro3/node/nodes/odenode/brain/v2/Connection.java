package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

/**
 * Positive weights will act as excitatory connections.
 * Negative weights will act as inhibitory connections.
 */
public class Connection {
    private Neuron fromNeuron;
    private Neuron toNeuron;
    private double weight;
    private boolean active;

    public Connection(Neuron fromNeuron, Neuron toNeuron, double weight) {
        this.fromNeuron = fromNeuron;
        this.toNeuron = toNeuron;
        this.weight = weight;
        this.active = false;
    }

    public void propagate() {
        if(weight==0) return;

        double propagatedValue = fromNeuron.getOutputValue() * weight;
        toNeuron.addInputValue(propagatedValue);
        active = true;
    }

    public void reset() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public Neuron getFromNeuron() {
        return fromNeuron;
    }

    public Neuron getToNeuron() {
        return toNeuron;
    }

    public void addWeight(double amount) {
        // Example: Increase the weight by a small factor
        weight += amount;
        //weight = Math.max(-1.0, Math.min(1.0, weight));
    }

    public void scaleWeight(double scale) {
        // Example: Decrease the weight by a small factor
        weight *= scale;
        //weight = Math.max(-1.0, Math.min(1.0, weight));
    }

    public double getWeight() {
        return weight;
    }
}
