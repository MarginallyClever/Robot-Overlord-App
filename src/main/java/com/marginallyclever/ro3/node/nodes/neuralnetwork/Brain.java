package com.marginallyclever.ro3.node.nodes.neuralnetwork;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodePath;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * {@link Brain} is a {@link Node} that represents a neural network.
 * Neurons that are children of this node are part of the network.
 */
public class Brain extends Node {
    // all neurons in this brain are collected here for convenience.
    private final List<Neuron> neurons = new ArrayList<>();
    // all synapses in this brain are collected here for convenience.
    private final List<Synapse> synapses = new ArrayList<>();
    private Rectangle bounds = null;
    // a list of NodePath<Neuron> that are the input neurons
    public final ListWithEvents<NodePath<Neuron>> inputs = new ListWithEvents<>();
    // a list of NodePath<Neuron> that are the output neurons
    public final ListWithEvents<NodePath<Neuron>> outputs = new ListWithEvents<>();

    // TODO put a single selection neuron activation here?
    // TODO put a single value for neuron sum decay here?

    public Brain() {
        this("Brain");
    }

    public Brain(String name) {
        super(name);
    }

    @Override
    public void update(double dt) {
        // update the children first
        super.update(dt);

        // if physics is enabled, update the physics
        if(!Registry.getPhysics().isPaused()) {
            //scan();

            // decay all neuron sums
            //for(Neuron n : neurons) n.setSum(0);  // reset the sum completely, most basic form of decay.
        }
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/neuralnetwork/icons8-neural-network-16.png")));
    }

    /**
     * Scan all children of this brain to collect all neurons and synapses.
     * Updates the bounding box of the brain, as well as the neurons and synapses lists
     */
    public void scan() {
        neurons.clear();
        synapses.clear();
        // also find their bounding box.

        List<Node> toScan = new LinkedList<>(getChildren());
        while(!toScan.isEmpty()) {
            Node child = toScan.remove(0);
            toScan.addAll(child.getChildren());
            if(child instanceof Neuron nn) {
                neurons.add(nn);
                if(bounds==null) {
                    bounds = new Rectangle(nn.position);
                } else {
                    bounds.add(nn.position);
                }
            }
            if(child instanceof Synapse s) {
                synapses.add(s);
            }
        }
    }

    public boolean isEmpty() {
        return (neurons.isEmpty() || synapses.isEmpty() || bounds==null);
    }

    public List<Neuron> getNeurons() {
        return neurons;
    }

    public List<Synapse> getSynapses() {
        return synapses;
    }

    @Override
    public void getComponents(List<JPanel> list) {
        list.add(new BrainPanel(this));
        super.getComponents(list);
    }

    @Override
    public JSONObject toJSON() {
        var json = super.toJSON();

        JSONArray inputsJson = new JSONArray();
        for(NodePath<Neuron> np : inputs.getList()) {
            inputsJson.put(np.getUniqueID());
        }
        json.put("inputs",inputsJson);

        JSONArray outputsJson = new JSONArray();
        for(NodePath<Neuron> np : outputs.getList()) {
            outputsJson.put(np.getUniqueID());
        }
        json.put("outputs",outputsJson);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        if(json.has("inputs")) {
            JSONArray inputsJson = json.getJSONArray("inputs");
            for(int i=0;i<inputsJson.length();i++) {
                inputs.add(new NodePath<>(this,Neuron.class,inputsJson.getString(i)));
            }
        }
        if(json.has("outputs")) {
            JSONArray outputsJson = json.getJSONArray("outputs");
            for(int i=0;i<outputsJson.length();i++) {
                outputs.add(new NodePath<>(this,Neuron.class,outputsJson.getString(i)));
            }
        }
    }

    public void setInputs(int newCount) {
        setListSize(inputs,newCount);
    }

    public void setOutputs(int newCount) {
        setListSize(outputs,newCount);
    }

    public void setListSize(ListWithEvents<NodePath<Neuron>> list, int newCount) {
        while(list.size()<newCount) {
            list.add(new NodePath<>(this,Neuron.class));
        }
        while(list.size()>newCount) {
            var lastItem = list.getList().get(list.size()-1);
            list.remove(lastItem);
        }
    }

    /**
     * Get the sum of output neuron i.
     * @param i the index of the neuron
     * @return the sum of the neuron or zero if the neuron is not found.
     * @throws InvalidParameterException if the index is out of range
     */
    public double getOutput(int i) {
        if(i<0 || i>=outputs.size()) throw new InvalidParameterException("index out of range");
        Neuron n = outputs.getList().get(i).getSubject();
        if(n==null) return 0;
        return n.getSum();
    }

    /**
     * Set the sum of input neuron i.
     * @param i the index of the neuron
     * @param v the new sum
     * @throws InvalidParameterException if the index is out of range
     */
    public void setInput(int i,double v) {
        if(i<0 || i>=inputs.size()) throw new InvalidParameterException("index out of range");
        Neuron n = inputs.getList().get(i).getSubject();
        if(n!=null) n.setSum(v);
    }
}
