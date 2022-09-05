package com.marginallyclever.robotOverlord;

import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.BooleanEntity;

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
}
