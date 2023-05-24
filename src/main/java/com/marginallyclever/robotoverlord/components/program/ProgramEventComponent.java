package com.marginallyclever.robotoverlord.components.program;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import org.json.JSONObject;

/**
 * <p>A {@link ProgramEventComponent} is a component that can be used to trigger events in a robot program.
 * These events are not part of the robot's state, but are instead used to trigger actions in the robot's program.</p>
 * They include:</p>
 * <ul>
 *     <li>Home the robot</li>
 *     <li>Dwell for a period of time</li>
 *     <li>Wait for pin value</li>
 *     <li>Set pin value</li>
 * </ul>
 *
 * @author Dan Royer
 * @since 2.6.0
 */
public class ProgramEventComponent extends ProgramStepComponent {
    public static String [] names = {
            "Home",
            "Dwell",
            "Wait for Pin",
            "Set Pin",
            "Gripper Grab",
            "Gripper Release"
    };
    public static final int HOME = 0;
    public static final int DWELL = 1;
    public static final int WAIT_PIN = 2;
    public static final int SET_PIN = 3;
    public static final int GRIPPER_GRAB = 4;
    public static final int GRIPPER_RELEASE = 5;

    public final IntParameter type = new IntParameter("Type",DWELL);
    public final IntParameter extra = new IntParameter("Extra",0);
    public final StringParameter custom = new StringParameter("Custom","");

    public ProgramEventComponent() {
        super();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("type",type.toJSON(context));
        jo.put("extra",extra.toJSON(context));
        jo.put("custom",custom.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) {
        super.parseJSON(jo, context);
        type.parseJSON(jo.getJSONObject("type"),context);
        extra.parseJSON(jo.getJSONObject("extra"),context);
        custom.parseJSON(jo.getJSONObject("custom"),context);
    }
}
