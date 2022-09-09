package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.BooleanEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * one or more {@link Component}s are attached to an {@link Entity}.
 *
 * @author Dan Royer
 * @since 2022-08-03
 */
public class Component {
    private Entity myEntity;

    private final BooleanEntity enabled = new BooleanEntity("Enabled",true);

    public Component() {
        super();
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * A Component may offer one or more {@link AbstractEntity} visual elements for the User to manipulate.
     * it does so by Decorating the given {@link ViewPanel} with these elements.
     *
     * @param view the ViewPanel to decorate.
     */
    public void getView(ViewPanel view) {
        view.add(enabled);
        // TODO enumerate all public AbstractEntity?
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

    public void update(double dt) {}

    public JSONObject toJSON() {
        JSONObject jo = new JSONObject();
        jo.put("type",this.getClass().getName());
        jo.put("enabled",enabled.get());
        return jo;
    }

    public void parseJSON(JSONObject jo) throws JSONException {
        enabled.set(jo.getBoolean("enabled"));
    }

    @Override
    public String toString() {
        return enabled.toString();
    }
}
