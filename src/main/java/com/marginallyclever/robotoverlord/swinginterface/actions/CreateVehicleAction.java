package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;
import com.marginallyclever.robotoverlord.systems.vehicle.CreateVehiclePanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CreateVehicleAction extends AbstractAction {
    private final EntityManager entityManager;
    private final JFrame mainframe;

    public CreateVehicleAction(EntityManager entityManager,JFrame mainframe) {
        super("Create Vehicle");
        this.entityManager = entityManager;
        this.mainframe = mainframe;
    }

    /**
     * Invoked when an action occurs.
     *
     * @param event the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        CreateVehiclePanel panel = new CreateVehiclePanel(entityManager);
        EntitySystemUtils.makePanel(panel, mainframe, (String)this.getValue(Action.NAME));
    }
}
