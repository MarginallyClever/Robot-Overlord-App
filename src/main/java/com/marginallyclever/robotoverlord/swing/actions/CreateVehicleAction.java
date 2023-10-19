package com.marginallyclever.robotoverlord.swing.actions;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;
import com.marginallyclever.robotoverlord.systems.vehicle.CreateVehiclePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CreateVehicleAction extends AbstractAction {
    private final EntityManager entityManager;
    private final Component parentComponent;

    public CreateVehicleAction(EntityManager entityManager,Component parentComponent) {
        super("Create Vehicle");
        this.entityManager = entityManager;
        this.parentComponent = parentComponent;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param event the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        CreateVehiclePanel panel = new CreateVehiclePanel(entityManager);
        EntitySystemUtils.makePanel(panel, parentComponent, (String)this.getValue(Action.NAME));
    }
}
