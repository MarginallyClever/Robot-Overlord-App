package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import org.json.JSONObject;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * Synapse is a connection between two {@link Neuron}s.
 */
public class Synapse extends Node {
    private final NodePath<Neuron> from = new NodePath<>(this,Neuron.class);
    private final NodePath<Neuron> to = new NodePath<>(this,Neuron.class);
    // typically anything but zero.
    public double weight;

    public Synapse() {
        this("Synapse");
    }

    public Synapse(String name) {
        super(name);
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new SynapsePanel(this));
        super.getComponents(list);
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/neuralnetwork/icons8-s-16.png")));
    }

    public Neuron getFrom() {
        return from.getSubject();
    }

    public void setFrom(Neuron n) {
        from.setUniqueIDByNode(n);
    }

    public Neuron getTo() {
        return to.getSubject();
    }

    public void setTo(Neuron n) {
        to.setUniqueIDByNode(n);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("to",to.getUniqueID());
        json.put("from",from.getUniqueID());
        json.put("weight",weight);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        if(json.has("to")) to.setUniqueID(json.getString("to"));
        if(json.has("from")) from.setUniqueID(json.getString("from"));
        if(json.has("weight")) weight = json.getDouble("weight");
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
