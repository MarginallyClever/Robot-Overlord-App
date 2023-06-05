package com.marginallyclever.robotoverlord.components.vehicle;

import com.marginallyclever.convenience.AABB;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.ListParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CarComponent} references a list of {@link WheelComponent}s.  A
 * {@link com.marginallyclever.robotoverlord.systems.VehicleSystem} uses these to move
 * the {@link com.marginallyclever.robotoverlord.entity.Entity} that owns the CarComponent.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
public class CarComponent extends Component {
    public final ListParameter<ReferenceParameter> wheels = new ListParameter<>();
    public final ListParameter<ReferenceParameter> steerWheels = new ListParameter<>();
    public final ListParameter<ReferenceParameter> poweredWheels = new ListParameter<>();

    public final DoubleParameter forwardVelocity = new DoubleParameter("Forward Velocity", 0);  // cm/s
    public final DoubleParameter strafeVelocity = new DoubleParameter("Strafe Velocity", 0);  // cm/s
    public final DoubleParameter turnRadius = new DoubleParameter("Turn radius", 0);  // cm, at center of car

    public CarComponent() {
        super();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("wheels", wheels.toJSON(context));
        json.put("forwardVelocity", forwardVelocity.toJSON(context));
        json.put("strafeVelocity", strafeVelocity.toJSON(context));
        json.put("turnRadius", turnRadius.toJSON(context));
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        wheels.parseJSON(jo.getJSONObject("wheels"), context);
        forwardVelocity.parseJSON(jo.getJSONObject("forwardVelocity"), context);
        strafeVelocity.parseJSON(jo.getJSONObject("strafeVelocity"), context);
        turnRadius.parseJSON(jo.getJSONObject("turnRadius"), context);
    }

    public void addWheel(Entity entity) {
        int next = getBiggestWheelNumber()+1;
        wheels.add(new ReferenceParameter("wheel"+next,entity.getUniqueID()));
    }

    private int getBiggestWheelNumber() {
        int biggest = 0;
        for(ReferenceParameter rp : wheels.get()) {
            String name = rp.getName();
            if(!name.startsWith("wheel")) continue;
            try {
                int num = Integer.parseInt(name.substring(5));
                if (num > biggest) biggest = num;
            } catch(NumberFormatException ignored) {}
        }
        return biggest;
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
