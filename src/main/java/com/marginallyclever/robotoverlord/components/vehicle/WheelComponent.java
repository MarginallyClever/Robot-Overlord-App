package com.marginallyclever.robotoverlord.components.vehicle;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.systems.vehicle.VehicleSystem;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link WheelComponent} is used by a {@link VehicleSystem}.
 * It is assumed that the local x axis is the forward direction.
 * It is assumed that the local y axis is the left/right direction.
 * It is assumed that the local z axis is the up/down direction.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
public class WheelComponent extends Component {
    public final DoubleParameter diameter = new DoubleParameter("Diameter", 1);
    public final DoubleParameter width = new DoubleParameter("Width", 1);
    public final ReferenceParameter drive = new ReferenceParameter("Drive motor");
    public final ReferenceParameter steer = new ReferenceParameter("Steer motor");

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("diameter", diameter.toJSON(context));
        jo.put("width", width.toJSON(context));
        jo.put("motor", drive.toJSON(context));
        jo.put("steer", steer.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        diameter.parseJSON(jo.getJSONObject("diameter"), context);
        width.parseJSON(jo.getJSONObject("width"), context);
        drive.parseJSON(jo.getJSONObject("motor"), context);
        steer.parseJSON(jo.getJSONObject("steer"), context);
    }
}
