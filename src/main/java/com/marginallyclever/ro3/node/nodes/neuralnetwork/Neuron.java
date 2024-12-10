package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.node.Node;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * <p>{@link Neuron} is one of the nodes in a {@link Brain}.  Neurons have a type, a bias, a sum, and a modulation.</p>
 * <p>There are several types of neuron.  When the activation function is triggered,
 * <ul>
 *     <li>Workers transmit a "normal" signal.</li>
 *     <li>Exciters change modulation in postsynaptic neurons, increasing firing probability.</li>
 *     <li>Suppressors change modulation in postsynaptic neurons, decreasing firing probability.</li>
 * </ul></p>
 * <p>Neurons are connected to other neurons by {@link Synapse}s.  Neurons fire signals along synapses when
 * <code>bias + modulation + sum</code> triggers the activation function (currently ReLU, aka >0).</p>
 * <p>The "from" end of a synapse is also known as the pre synaptic neuron.  the "to" end is the post synaptic neuron.</p>
 */
public class Neuron extends Node {
    public enum Type {
        Worker,
        Exciter,
        Suppressor,
    };

    public final Point position = new Point();
    // the sum is the weighted sum of all inputs.
    private double sum = 0;
    // the bias is added to the sum before the activation function is applied.
    private double bias = 0.5;
    private double modulation = 0;
    private Type neuronType = Type.Worker;

    public Neuron() {
        this("Neuron");
    }

    public Neuron(String name) {
        super(name);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        var pos = new JSONObject();
        pos.put("x", position.x);
        pos.put("y", position.y);
        json.put("position", pos);
        json.put("bias", bias);
        json.put("sum", sum);
        json.put("neuronType", neuronType.toString());
        json.put("modulation", modulation);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        if(json.has("position")) {
            var p = json.getJSONObject("position");
            position.x= p.getInt("x");
            position.y= p.getInt("y");
        }
        if(json.has("bias")) bias = json.getDouble("bias");
        if(json.has("sum")) sum = json.getDouble("sum");
        if(json.has("neuronType")) neuronType = Type.valueOf(json.getString("neuronType"));
        if(json.has("modulation")) modulation = json.getDouble("modulation");
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/neuralnetwork/icons8-neuron-16.png")));
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new NeuronPanel(this));
        super.getComponents(list);
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public Type getNeuronType() {
        return neuronType;
    }

    public void setNeuronType(Type neuronType) {
        this.neuronType = neuronType;
    }

    public double getModulation() {
        return modulation;
    }

    public void setModulation(double modulation) {
        this.modulation = modulation;
    }

    /**
     * ReLU activation function
     * @return true if the neuron should fire.
     */
    public boolean activationFunction() {
        return sum+bias+modulation>0;
    }
}
