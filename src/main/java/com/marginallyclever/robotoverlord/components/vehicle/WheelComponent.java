package com.marginallyclever.robotoverlord.components.vehicle;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link WheelComponent} is used by a {@link com.marginallyclever.robotoverlord.systems.VehicleSystem}.
 * It is assumed that the local x axis is the forward direction.
 * It is assumed that the local y axis is the left/right direction.
 * It is assumed that the local z axis is the up/down direction.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
public class WheelComponent extends Component {
    public static final String [] wheelTypeNames = {"Normal","Omni","Mecanum"};
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_OMNI = 1;
    public static final int TYPE_MECANUM = 2;

    public final IntParameter type = new IntParameter("Type", WheelComponent.TYPE_NORMAL);
    public final DoubleParameter diameter = new DoubleParameter("Diameter", 1);
    public final DoubleParameter width = new DoubleParameter("Width", 1);

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("type", type.toJSON(context));
        jo.put("diameter", diameter.toJSON(context));
        jo.put("width", width.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        type.parseJSON(jo.getJSONObject("type"), context);
        diameter.parseJSON(jo.getJSONObject("diameter"), context);
        width.parseJSON(jo.getJSONObject("width"), context);
    }
}
