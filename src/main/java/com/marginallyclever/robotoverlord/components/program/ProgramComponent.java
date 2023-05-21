package com.marginallyclever.robotoverlord.components.program;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.ComponentWithReferences;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Stack;

/**
 * The ProgramComponent holds run-time information about the program being executed by a robot.
 * The ProgramExecutorSystem will use this information to execute the program.
 * When placed in the same {@link Entity} as a {@link RobotComponent}, this
 * {@link Component} will allow a ProgramExecutor a robot to a execute {@link ProgramPathComponent}s
 * and {@link ProgramEventComponent}s.
 *
 * @since 2.6.0
 * @author Dan Royer
 */
public class ProgramComponent extends Component implements ComponentWithReferences {
    public static final String[] MODE_NAMES = { "Step", "Run to End", "Loop" };
    public static int RUN_STEP = 0;
    public static int RUN_TO_END = 1;
    public static int RUN_LOOP = 2;

    public ReferenceParameter programEntity = new ReferenceParameter("Program",null);
    public ReferenceParameter stepEntity = new ReferenceParameter("Step",null);
    private BooleanParameter isRunning = new BooleanParameter("Running",false);
    public IntParameter mode = new IntParameter("mode",RUN_STEP);
    private final Stack<Object> stack = new Stack<>();

    public ProgramComponent() {
        super();
    }

    public boolean getRunning() {
        return isRunning.get();
    }

    public void setRunning(boolean arg0) {
        isRunning.set(arg0);
    }

    public void reset() {
        isRunning.set(false);
        stepEntity.set((String)null);
        stack.clear();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("programEntity",programEntity.toJSON(context));
        jo.put("stepEntity",stepEntity.toJSON(context));
        jo.put("isRunning",isRunning.toJSON(context));
        jo.put("mode",mode.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        programEntity.parseJSON(jo.getJSONObject("programEntity"),context);
        stepEntity.parseJSON(jo.getJSONObject("stepEntity"),context);
        isRunning.parseJSON(jo.getJSONObject("isRunning"),context);
        mode.parseJSON(jo.getJSONObject("mode"),context);
    }

    public void addRunningPropertyChangeListener(PropertyChangeListener arg0) {
        isRunning.addPropertyChangeListener(arg0);
    }

    public void popStack() {
        stack.pop();
    }

    public void pushStack(Object obj) {
        stack.push(obj);
    }

    public Object peekStack() {
        return stack.peek();
    }

    public int getStackDepth() {
        return stack.size();
    }

    @Override
    public void updateReferences(Map<String, String> oldToNewIDMap) {
        programEntity.updateReferences(oldToNewIDMap);
        stepEntity.updateReferences(oldToNewIDMap);
    }
}
