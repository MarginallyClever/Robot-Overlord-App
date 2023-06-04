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
    public final DoubleParameter targetAngle = new DoubleParameter("Target angle", 0);

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("targetAngle", targetAngle.toJSON(context));
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        targetAngle.parseJSON(jo.getJSONObject("targetAngle"), context);
    }
}
