package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.ComponentPasteEdit;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.swinginterface.robotlibrarypanel.RobotLibraryPanel;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;
import com.marginallyclever.robotoverlord.systems.motor.CreateVehiclePanel;
import com.marginallyclever.robotoverlord.systems.vehicle.VehicleFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

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
