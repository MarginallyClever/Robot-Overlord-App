package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * one or more {@link Component}s are attached to an {@link Entity}.
 *
 * @author Dan Royer
 * @since 2022-08-03
 */
public abstract class Component {
    private Entity myEntity;

    public final BooleanParameter enabled = new BooleanParameter("Enabled",true);

    private boolean isExpanded=true;

    public Component() {
        super();
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public Entity getEntity() {
        return myEntity;
    }

    public void setEntity(Entity entity) {
        myEntity=entity;
    }

    public void setEnable(boolean arg0) {
        enabled.set(arg0);
    }

    public boolean getEnabled() {
        return enabled.get();
    }

    @Deprecated
    public void update(double dt) {}

    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = new JSONObject();
        jo.put("type",this.getClass().getName());
        jo.put("enabled",enabled.get());
        jo.put("expanded",isExpanded);
        return jo;
    }

    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        enabled.set(jo.getBoolean("enabled"));
        if(jo.has("expanded")) isExpanded = jo.getBoolean("expanded");
    }

    @Override
    public String toString() {
        return enabled.toString();
    }

    public boolean getExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean arg0) {
        isExpanded = arg0;
    }

    /**
     * Called when this component is attached to an entity.
     */
    public void onAttach() {}
}
