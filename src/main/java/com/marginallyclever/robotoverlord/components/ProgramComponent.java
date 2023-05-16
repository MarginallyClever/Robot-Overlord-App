package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The ProgramComponent holds run-time information about the program being executed by a robot.
 * The ProgramExecutorSystem will use this information to execute the program.
 * When placed in the same {@link Entity} as a {@link RobotComponent}, this
 * {@link Component} will allow a ProgramExecutor a robot to a execute {@link PathComponent}s
 * and {@link ProgramEventComponent}s.
 *
 * @since 2.6.0
 * @author Dan Royer
 */
public class ProgramComponent extends Component {
    public static final String[] MODE_NAMES = { "Step", "Run to End", "Cycle" };
    public static int RUN_STEP = 0;
    public static int RUN_TO_END = 1;
    public static int RUN_CYCLE = 2;

    public ReferenceParameter programEntity = new ReferenceParameter("Program",null);
    public ReferenceParameter stepEntity = new ReferenceParameter("Step",null);
    public BooleanParameter isRunning = new BooleanParameter("Running",false);
    public IntParameter mode = new IntParameter("mode",RUN_STEP);

    public ProgramComponent() {
        super();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("programEntity",programEntity.toJSON());
        jo.put("stepEntity",stepEntity.toJSON());
        jo.put("isRunning",isRunning.toJSON());
        jo.put("mode",mode.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        programEntity.parseJSON(jo.getJSONObject("programEntity"));
        stepEntity.parseJSON(jo.getJSONObject("stepEntity"));
        isRunning.parseJSON(jo.getJSONObject("isRunning"));
        mode.parseJSON(jo.getJSONObject("mode"));
    }
}
