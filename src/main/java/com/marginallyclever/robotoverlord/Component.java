package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.BooleanEntity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

    public void save(BufferedWriter writer) throws IOException {
        enabled.save(writer);
    }

    public void load(BufferedReader reader) throws Exception {
        enabled.load(reader);
    }

    @Override
    public String toString() {
        return enabled.toString();
    }
}
