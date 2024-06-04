package com.marginallyclever.ro3.node.nodes.odenode.brain.v2;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * A neuron in the brain.
 */
public class Neuron {
    private final Vector3d position = new Vector3d();
    private int id;
    private double inputValue;
    private double outputValue;
    private final List<Connection> outgoingConnections = new ArrayList<>();

    public Neuron(int id,double x, double y, double z) {
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

    public int getID() {
        return id;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("id", getID());
        json.put("position", positionToJSON());
        json.put("inputValue", getInputValue());
        json.put("outputValue", getOutputValue());
        return json;
    }

    public void fromJSON(JSONObject json) {
        id = json.getInt("id");
        JSONArray p = json.getJSONArray("position");
        position.set(p.getDouble(0), p.getDouble(1), p.getDouble(2));
        setInputValue(json.getDouble("inputValue"));
        outputValue = json.getDouble("outputValue");
    }

    private JSONArray positionToJSON() {
        JSONArray json = new JSONArray();
        json.put(position.x);
        json.put(position.y);
        json.put(position.z);
        return json;
    }
}
