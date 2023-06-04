package com.marginallyclever.robotoverlord.components.vehicle;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.ComponentDependency;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link WheelComponent} is used by a CarSystem to calculate the correct velocities for adjacent
 * {@link MotorComponent}s.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
@ComponentDependency(components = {PoseComponent.class,MotorComponent.class})
public class WheelComponent extends Component {
    public static final String [] names = {"Normal","Omni","Mecanum"};
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
