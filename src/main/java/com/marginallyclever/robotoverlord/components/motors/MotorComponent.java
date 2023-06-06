package com.marginallyclever.robotoverlord.components.motors;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
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
    private final TreeMap<Double, Double> torqueCurve = new TreeMap<>();
    private final DoubleParameter currentVelocity = new DoubleParameter("Current Velocity (rpm)",0);  // rpm
    private final DoubleParameter desiredVelocity = new DoubleParameter("Desired Velocity (rpm)",0);  // rpm
    public final DoubleParameter gearRatio = new DoubleParameter("Gear Ratio",1.0);
    public final DoubleParameter currentAngle = new DoubleParameter("Current angle", 0);

    public MotorComponent() {
        super();
    }

    // Set the torque for a specific RPM
    public void setTorqueAtRPM(double rpm, double torque) {
        this.torqueCurve.put(rpm, torque);
    }

    /**
     * @param rpm The RPM to get the torque for
     * @return The torque at the given RPM, or 0.0 if the RPM is outside the curve.
     */
    public double getTorqueAtRpm(double rpm) {
        // If the exact RPM is in the curve, return the torque for it
        if (this.torqueCurve.containsKey(rpm)) {
            return this.torqueCurve.get(rpm);
        }

        // If the exact RPM is not in the curve, perform linear interpolation
        Double lowestRPM = null;
        Double lowerRpm = null;
        Double higherRpm = null;
        for(Double key : this.torqueCurve.keySet()) {
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

    public void setCurrentVelocity(double rpm) {
        this.currentVelocity.set(rpm);
    }

    public double getCurrentVelocity() {
        return this.currentVelocity.get();
    }

    public void setDesiredVelocity(double rpm) {
        this.desiredVelocity.set(rpm);
    }

    public double getDesiredVelocity() {
        return this.desiredVelocity.get();
    }

    // Get current torque based on current RPM
    public double getCurrentTorque() {
        return getTorqueAtRpm(this.currentVelocity.get());
    }

    public double getMaxRPM() {
        List<Double> keys = new ArrayList<>(torqueCurve.keySet());
    	return keys.get(keys.size()-1);
    }

    public TreeMap<Double,Double> getTorqueCurve() {
    	return torqueCurve;
    }

    public void removeTorqueAtRPM(int rpm) {
        torqueCurve.remove(rpm);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        JSONObject curve = new JSONObject();
        for(Map.Entry<Double, Double> entry : torqueCurve.entrySet()) {
            curve.put(entry.getKey().toString(),entry.getValue());
        }
        jo.put("torqueCurve",curve);
        jo.put("gearRatio",gearRatio.get());
        jo.put("currentVelocity",currentVelocity.toJSON(context));
        jo.put("desiredVelocity",desiredVelocity.toJSON(context));
        jo.put("currentAngle",currentAngle.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        gearRatio.set(jo.getDouble("gearRatio"));
        torqueCurve.clear();
        JSONObject curve = jo.getJSONObject("torqueCurve");
        Iterator<String> keys = curve.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Double value = curve.getDouble(key);
            torqueCurve.put(Double.parseDouble(key),value);
        }
        currentVelocity.parseJSON(jo.getJSONObject("currentVelocity"),context);
        desiredVelocity.parseJSON(jo.getJSONObject("desiredVelocity"),context);
        currentAngle.parseJSON(jo.getJSONObject("currentAngle"),context);
    }

    @Override
    public String toString() {
        return super.toString()
                + ", gearRatio=" + gearRatio.get()
                + ", torqueCurve=" + torqueCurve.toString()
                + ", currentRPM=" + currentVelocity
                + ", desiredRPM=" + desiredVelocity
                + ", currentAngle=" + currentAngle
                + ",\n";
    }

    public double getGearAngle() {
        return currentAngle.get() * gearRatio.get();
    }

    public double getGearVelocity() {
        return currentVelocity.get() * gearRatio.get();
    }

    public void setAngle(double angle) {
        currentAngle.set(angle);
    }
}
