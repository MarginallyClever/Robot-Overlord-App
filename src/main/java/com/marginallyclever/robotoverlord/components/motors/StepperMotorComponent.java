package com.marginallyclever.robotoverlord.components.motors;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import org.json.JSONException;
import org.json.JSONObject;

public class StepperMotorComponent extends MotorComponent {
    public static final String [] directionNames = {"Backward","Forward"};
    public static final int DIRECTION_BACKWARD=0;
    public static final int DIRECTION_FORWARD=1;

    public final IntParameter direction = new IntParameter("Direction",DIRECTION_FORWARD);
    public final IntParameter stepPerRevolution = new IntParameter("Step/turn",200);
    public final IntParameter microStepping = new IntParameter("Microsteps",1);

    /**
     * @param direction either DIRECTION_BACKWARD or DIRECTION_FORWARD
     */
    public void setDirection(int direction) {
        if(direction!=DIRECTION_BACKWARD && direction!=DIRECTION_FORWARD) {
            throw new IllegalArgumentException("direction must be either DIRECTION_BACKWARD or DIRECTION_FORWARD.");
        }
        this.direction.set(direction);
    }

    public void setStepPerRevolution(int stepPerRevolution) {
        this.stepPerRevolution.set(stepPerRevolution);
    }

    public int getStepPerRevolution() {
        return stepPerRevolution.get();
    }

    /**
     * @param microStepping must be power of two.  Must be greater than zero.
     */
    public void setMicroStepping(int microStepping) {
        if(microStepping<1) throw new IllegalArgumentException("microStepping must be greater than zero.");
        if(isNotPowerOf2(microStepping)) throw new IllegalArgumentException("microStepping must be power of two.");
        this.microStepping.set(microStepping);
    }

    private boolean isNotPowerOf2(int v) {
        return (v & (v - 1)) != 0;
    }

    public int getMicroStepping() {
        return microStepping.get();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("direction",direction.toJSON(context));
        json.put("stepPerRevolution",stepPerRevolution.toJSON(context));
        json.put("microStepping",microStepping.toJSON(context));
        return json;

    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        if(jo.has("direction")) direction.parseJSON(jo.getJSONObject("direction"),context);
        if(jo.has("stepPerRevolution")) stepPerRevolution.parseJSON(jo.getJSONObject("stepPerRevolution"),context);
        if(jo.has("microStepping")) microStepping.parseJSON(jo.getJSONObject("microStepping"),context);
    }
}
