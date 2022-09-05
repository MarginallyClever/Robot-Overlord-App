package com.marginallyclever.robotOverlord;

import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.StringEntity;

/**
 * one or more {@link Component}s are attached to an {@link Entity}.
 *
 * @author Dan Royer
 * @since 2022-08-03
 */
public class Component {
    private Entity myEntity;

    private StringEntity name = new StringEntity("name","");

    public Component() {
        this.name.set(this.getClass().getSimpleName());
    }

    /**
     * A Component may offer one or more {@link AbstractEntity} visual elements for the User to manipulate.
     * it does so by Decorating the given {@link ViewPanel} with these elements.
     *
     * @param view the ViewPanel to decorate.
     */
    public void getView(ViewPanel view) {
        view.add(name);
    }

    public Entity getEntity() {
        return myEntity;
    }

    public void set(Component b) {
        this.name.set(b.name.get());
    }

    public void setEntity(Entity entity) {
        myEntity=entity;
    }
}
