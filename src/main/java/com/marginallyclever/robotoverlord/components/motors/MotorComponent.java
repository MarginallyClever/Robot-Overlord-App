package com.marginallyclever.robotoverlord.components.motors;

import com.marginallyclever.robotoverlord.components.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * A motor {@link Component} that approximates a torque curve.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class MotorComponent extends Component {
    private final Map<Integer, Double> torqueCurve = new HashMap<>();
    private int currentRPM=0;

    public MotorComponent() {
        super();
    }

    // Set the torque for a specific RPM
    public void setTorqueAtRPM(int rpm, double torque) {
        this.torqueCurve.put(rpm, torque);
    }

    // Get the torque for a specific RPM
    public double getTorqueAtRpm(int rpm) {
        // If the exact RPM is in the curve, return the torque for it
        if (this.torqueCurve.containsKey(rpm)) {
            return this.torqueCurve.get(rpm);
        }

        // If the exact RPM is not in the curve, perform linear interpolation
        Integer lowestRPM = null;
        Integer lowerRpm = null;
        Integer higherRpm = null;
        for (Integer key : this.torqueCurve.keySet()) {
            if (key <= rpm && (lowerRpm == null || key > lowerRpm)) {
                lowerRpm = key;
            }
            if (key >= rpm && (higherRpm == null || key < higherRpm)) {
                higherRpm = key;
            }
            if(lowestRPM == null || key < lowestRPM) {
                lowestRPM = key;
            }
        }

        // If no suitable lower or higher RPM is found, return 0.0
        if(lowerRpm == null) return this.torqueCurve.get(lowestRPM);
        if(higherRpm == null) return 0.0;

        // Perform linear interpolation
        double lowerTorque = this.torqueCurve.get(lowerRpm);
        double higherTorque = this.torqueCurve.get(higherRpm);
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
}
