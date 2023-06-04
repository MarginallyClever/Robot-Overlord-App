package com.marginallyclever.robotoverlord.components.vehicle;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CarComponent} references a list of {@link WheelComponent}s.  A CarSystem then uses these to move
 * the {@link com.marginallyclever.robotoverlord.entity.Entity} that owns the CarComponent.
 */
public class CarComponent extends Component {
    public final List<ReferenceParameter> wheels = new ArrayList<>();

    public CarComponent() {
        super();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("numWheels", wheels.size());
        for(int i=0;i<wheels.size();++i) {
            json.put("wheel"+i, wheels.get(i).toJSON(context));
        }
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        int numWheels = jo.getInt("numWheels");
        for(int i=0;i<numWheels;++i) {
            wheels.add(new ReferenceParameter("wheel"+i));
            wheels.get(i).parseJSON(jo.getJSONObject("wheel"+i), context);
        }
    }

    public void addWheel(Entity entity) {
        int next = getBiggestWheelNumber()+1;
        wheels.add(new ReferenceParameter("wheel"+next,entity.getUniqueID()));
    }

    private int getBiggestWheelNumber() {
        int biggest = 0;
        for(ReferenceParameter rp : wheels) {
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
