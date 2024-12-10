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

    // TODO add activation function selector here?
    private boolean hebbianLearningActive=false;
    // how fast does the network learn?  larger is faster.
    private double learningRate=0.1;  // 0 for none, 1 for full.
    // How fast does the network forget?  larger is faster.
    private double forgettingRate=0.001;  // 0 for none, 1 for full.
    // modulation degradation rate.  larger is faster.
    private double modulationDegradationRate = 0.1;  // 0 for none, 1 for full.
    // a single value for neuron sum decay every frame.
    private double sumDecay = 1.0;

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

        // if physics is enabled, update the brain as well.
        if(!Registry.getPhysics().isPaused()) {
            step();
        }
    }

    /**
     * Update the brain by one step.  This is the core of the neural network.
     */
    void step() {
        scan();

        // fire the activated neurons
        var toFire = collectFiringNeurons();
        for(Neuron n : toFire) {
            // find all synapses from this neuron
            var found = getAllSynapsesFrom(n);

            var w = n.getSum();  // use the sum
            //w -= n.getBias();  // use the overflow
            //w /= found.size();  // split the overflow between all synapses

            // fire the synapses
            for(Synapse s : found) {
                var to = s.getTo();
                var ws = w * s.getWeight();
                switch(n.getNeuronType()) {
                    default        :  to.setSum( to.getSum() + ws );  break;
                    case Exciter   :  to.setModulation( to.getModulation() + ws );  break;
                    case Suppressor:  to.setModulation( to.getModulation() - ws );  break;
                }
            }
        }

        if(hebbianLearningActive) {
            hebbianLearning();
        }
        degradeAllModulations();
        // don't decay the sums here.  The limbic system needs to read data out before the sums are decayed.
    }

    private void degradeAllModulations() {
        for(Neuron n : neurons) {
            var m = n.getModulation();
            n.setModulation(m - m * modulationDegradationRate);
        }
    }

    /**
     * Hebbian learning: Neurons that fire together, wire together.  We need to find all the neurons that fired this
     * tick AND the ones that will fire on the next because the firings may have activated new neurons.
     */
    private void hebbianLearning() {
        for(Synapse s : synapses) {
            var from = s.getFrom();
            var to = s.getTo();

            if(from.activationFunction() && to.activationFunction()) {
                s.setWeight( s.getWeight() + learningRate * from.getSum() * to.getSum() );
            } else {
                s.setWeight( s.getWeight() - forgettingRate * s.getWeight() );
            }
        }
    }

    /**
     * Return all synapses that have "from" as the match neuron and that connect to a valid neuron.
     * @param match the neuron to search for
     */
    private ArrayList<Synapse> getAllSynapsesFrom(Neuron match) {
        var found = new ArrayList<Synapse>();
        for(Synapse s : synapses) {
            var from = s.getFrom();
            if (from != match) continue;
            var to = s.getTo();
            if (to == null) continue;
            found.add(s);
        }
        return found;
    }

    /**
     * @return a list of neurons that are currently firing
     */
    private ArrayList<Neuron> collectFiringNeurons() {
        var toFire = new ArrayList<Neuron>();
        for(Neuron n : neurons) {
            if(n.activationFunction()) toFire.add(n);
        }
        return toFire;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/node/nodes/neuralnetwork/icons8-brain-16.png")));
    }

    /**
     * Scan all children of this brain to collect all neurons and synapses.
     * Updates the bounding box of the brain, as well as the neurons and synapses lists
     */
    public void scan() {
        neurons.clear();
        synapses.clear();
        // also find their bounding box.
        bounds=null;

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

        json.put("sumDecay", sumDecay);
        json.put("learningRate", learningRate);
        json.put("forgettingRate", forgettingRate);
        json.put("hebbianLearningActive", hebbianLearningActive);
        json.put("modulationDegradationRate", modulationDegradationRate);

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

        if(json.has("sumDecay")) sumDecay = json.getDouble("sumDecay");
        if(json.has("learningRate")) learningRate = json.getDouble("learningRate");
        if(json.has("forgettingRate")) forgettingRate = json.getDouble("forgettingRate");
        if(json.has("hebbianLearningActive")) hebbianLearningActive = json.getBoolean("hebbianLearningActive");
        if(json.has("modulationDegradationRate")) modulationDegradationRate = json.getDouble("modulationDegradationRate");

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

    // decay all neuron sums
    public void decaySums() {
        for(Neuron n : getNeurons()) n.setSum(n.getSum()* sumDecay);
    }

    public double getSumDecay() {
        return sumDecay;
    }

    public void setSumDecay(double v) {
        sumDecay = v;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double v) {
        learningRate = v;
    }

    public double getForgettingRate() {
        return forgettingRate;
    }

    public void setForgettingRate(double v) {
        forgettingRate = v;
    }

    public boolean isHebbianLearningActive() {
        return hebbianLearningActive;
    }

    public void setHebbianLearningActive(boolean v) {
        hebbianLearningActive = v;
    }

    public void setModulationDegradationRate(double v) {
        modulationDegradationRate = v;
    }

    public double getModulationDegradationRate() {
        return modulationDegradationRate;
    }
}
