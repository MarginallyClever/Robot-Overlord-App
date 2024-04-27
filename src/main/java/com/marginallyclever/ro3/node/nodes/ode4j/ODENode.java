package com.marginallyclever.ro3.node.nodes.ode4j;

import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.json.JSONObject;

public class ODENode extends Pose {
    /**
     * Should not be serialized, should be reset on every deserialize (such as {@link #fromJSON(JSONObject)})
     */
    private boolean runFirstUpdate=true;


    public ODENode() {
        this("ODE Node");
    }

    public ODENode(String name) {
        super(name);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        runFirstUpdate=true;
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        if(runFirstUpdate) {
            runFirstUpdate=false;
            onFirstUpdate();
        }
    }

    @Override
    public void fromJSON(JSONObject from) {
        super.fromJSON(from);
        runFirstUpdate=true;
    }

    /**
     * Called once at the start of the first {@link #update(double)}
     */
    protected void onFirstUpdate() {
        // override me
    }
}
