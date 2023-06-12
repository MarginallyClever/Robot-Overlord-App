package com.marginallyclever.robotoverlord.components.motors;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link ServoComponent} is a motor that can be set to a specific angle.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
public class ServoComponent extends MotorComponent {
    public final DoubleParameter desiredAngle = new DoubleParameter("Target angle", 0);
    public final DoubleParameter minAngle = new DoubleParameter("Min angle", -90);
    public final DoubleParameter maxAngle = new DoubleParameter("Max angle", 90);
    public final DoubleParameter kP = new DoubleParameter("kP", 1.0);
    public final DoubleParameter kI = new DoubleParameter("kI", 0.0);
    public final DoubleParameter kD = new DoubleParameter("kD", 0.0);
    public final DoubleParameter errorSum = new DoubleParameter("Integral", 0);
    public final DoubleParameter lastError = new DoubleParameter("Last error", 0);

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("desiredAngle", desiredAngle.toJSON(context));
        json.put("minAngle", minAngle.toJSON(context));
        json.put("maxAngle", maxAngle.toJSON(context));
        json.put("kP", kP.toJSON(context));
        json.put("kI", kI.toJSON(context));
        json.put("kD", kD.toJSON(context));
        json.put("integral", errorSum.toJSON(context));
        json.put("lastError", lastError.toJSON(context));
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        desiredAngle.parseJSON(jo.getJSONObject("desiredAngle"), context);
        minAngle.parseJSON(jo.getJSONObject("minAngle"), context);
        maxAngle.parseJSON(jo.getJSONObject("maxAngle"), context);
        kP.parseJSON(jo.getJSONObject("kP"), context);
        kI.parseJSON(jo.getJSONObject("kI"), context);
        kD.parseJSON(jo.getJSONObject("kD"), context);
        errorSum.parseJSON(jo.getJSONObject("integral"), context);
        lastError.parseJSON(jo.getJSONObject("lastError"), context);
    }

    @Override
    public String toString() {
        return super.toString()
                + ", desiredAngle=" + desiredAngle
                + ", minAngle=" + minAngle
                + ", maxAngle=" + maxAngle
                + ", kP=" + kP
                + ", kI=" + kI
                + ", kD=" + kD
                + ",\n";
    }

    public void resetPIDMemory() {
        errorSum.set(0.0);
        lastError.set(0.0);
    }
}
