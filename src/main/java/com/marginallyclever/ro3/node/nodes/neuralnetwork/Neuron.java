package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.node.Node;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;

/**
 * Neuron is one of the nodes in a {@link Brain}.
 */
public class Neuron extends Node {
    public final Point position = new Point();
    // the sum is the weighted sum of all inputs.
    private double sum = 0;
    // the bias is added to the sum before the activation function is applied.
    private double bias = 0;

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
}
