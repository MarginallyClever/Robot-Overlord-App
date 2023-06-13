package com.marginallyclever.robotoverlord.components.vehicle;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.ListParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.systems.vehicle.VehicleSystem;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link VehicleComponent} references a list of {@link WheelComponent}s.  A
 * {@link VehicleSystem} uses these to move
 * the {@link com.marginallyclever.robotoverlord.entity.Entity} that owns the CarComponent.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
public class VehicleComponent extends Component {
    public static final String [] wheelTypeNames = {"Normal","Omni","Mecanum","Traction"};
    public static final int WHEEL_NORMAL = 0;
    // omni wheels have rollers at a right angle to the plane of the wheel
    public static final int WHEEL_OMNI = 1;
    // mecanum wheels have rollers at a 45 degree angle to the plane of the wheel
    public static final int WHEEL_MECANUM = 2;
    // traction wheels use differential steering
    public static final int WHEEL_DIFFERENTIAL = 3;

    public final IntParameter wheelType = new IntParameter("Wheel type", VehicleComponent.WHEEL_NORMAL);
    public final ListParameter<ReferenceParameter> wheels = new ListParameter<>("Wheels",new ReferenceParameter());

    public final DoubleParameter forwardVelocity = new DoubleParameter("Desired forward (cm/s)", 0);  // cm/s
    public final DoubleParameter strafeVelocity = new DoubleParameter("Desired strafe (cm/s)", 0);  // cm/s
    public final DoubleParameter turnVelocity = new DoubleParameter("Desired turn (deg/s)", 0);  // deg/s at center of car

    public VehicleComponent() {
        super();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("wheelType", wheelType.toJSON(context));
        json.put("wheels", wheels.toJSON(context));
        json.put("forwardVelocity", forwardVelocity.toJSON(context));
        json.put("strafeVelocity", strafeVelocity.toJSON(context));
        json.put("turnVelocity", turnVelocity.toJSON(context));
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        wheelType.parseJSON(jo.getJSONObject("wheelType"), context);
        wheels.parseJSON(jo.getJSONObject("wheels"), context);
        forwardVelocity.parseJSON(jo.getJSONObject("forwardVelocity"), context);
        strafeVelocity.parseJSON(jo.getJSONObject("strafeVelocity"), context);
        turnVelocity.parseJSON(jo.getJSONObject("turnVelocity"), context);
    }

    public void addWheel(Entity entity) {
        addWheelToList(entity,wheels);
    }

    private void addWheelToList(Entity entity, ListParameter<ReferenceParameter> list) {
        list.add(new ReferenceParameter("",entity.getUniqueID()));
    }

    /**
     *
     * @param i
     * @return UUID of wheel Entity
     */
    public String getWheel(int i) {
        return wheels.get(i).get();
    }
}
