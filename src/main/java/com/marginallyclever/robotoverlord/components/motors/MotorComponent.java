package com.marginallyclever.robotoverlord.components.motors;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * A motor {@link Component} that approximates a torque curve.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class MotorComponent extends Component {
    private final TreeMap<Integer, Double> torqueCurve = new TreeMap<>();
    private int currentRPM=0;

    public MotorComponent() {
        super();
    }

    // Set the torque for a specific RPM
    public void setTorqueAtRPM(int rpm, double torque) {
        this.torqueCurve.put(rpm, torque);
    }

    /**
     * @param rpm The RPM to get the torque for
     * @return The torque at the given RPM, or 0.0 if the RPM is outside the curve.
     */
    public double getTorqueAtRpm(int rpm) {
        // If the exact RPM is in the curve, return the torque for it
        if (this.torqueCurve.containsKey(rpm)) {
            return this.torqueCurve.get(rpm);
        }

        // If the exact RPM is not in the curve, perform linear interpolation
        Integer lowestRPM = null;
        Integer lowerRpm = null;
        Integer higherRpm = null;
        for(Integer key : this.torqueCurve.keySet()) {
            if(key <= rpm && ( lowerRpm == null || key >  lowerRpm))  lowerRpm = key;
            if(key >= rpm && (higherRpm == null || key < higherRpm)) higherRpm = key;
            if(lowestRPM == null || key < lowestRPM) lowestRPM = key;
        }

        // If no suitable lower or higher RPM is found, return 0.0
        if(lowerRpm == null) return torqueCurve.get(lowestRPM);
        if(higherRpm == null) return 0.0;

        // Perform linear interpolation
        double lowerTorque = torqueCurve.get(lowerRpm);
        double higherTorque = torqueCurve.get(higherRpm);
        double slope = (higherTorque - lowerTorque) / (higherRpm - lowerRpm);
        return lowerTorque + slope * (rpm - lowerRpm);
    }

    // Set current RPM
    public void setCurrentRPM(int rpm) {
        this.currentRPM = rpm;
    }

    // Get current RPM
    public int getCurrentRPM() {
        return this.currentRPM;
    }

    // Get current torque based on current RPM
    public double getCurrentTorque() {
        return getTorqueAtRpm(this.currentRPM);
    }

    public int getMaxRPM() {
        List<Integer> x = new ArrayList<>(torqueCurve.keySet());
    	return x.get(x.size()-1);
    }

    public TreeMap<Integer,Double> getTorqueCurve() {
    	return torqueCurve;
    }

    public void removeTorqueAtRPM(int rpm) {
        torqueCurve.remove(rpm);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        JSONObject curve = new JSONObject();
        for(Map.Entry<Integer, Double> entry : torqueCurve.entrySet()) {
            curve.put(entry.getKey().toString(),entry.getValue());
        }
        jo.put("torqueCurve",curve);
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        torqueCurve.clear();
        JSONObject curve = jo.getJSONObject("torqueCurve");
        Iterator<String> keys = curve.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Double value = curve.getDouble(key);
            torqueCurve.put(Integer.parseInt(key),value);
        }
    }
}
