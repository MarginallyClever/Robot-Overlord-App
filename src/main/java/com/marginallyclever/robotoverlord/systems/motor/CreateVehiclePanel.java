package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.systems.vehicle.VehicleFactory;

import javax.swing.*;
import java.awt.*;

public class CreateVehiclePanel extends JPanel {
    private final EntityManager entityManager;
    private final JComboBox<String> names;

    public CreateVehiclePanel(EntityManager entityManager) {
        super(new BorderLayout());
        this.entityManager = entityManager;

        names = new JComboBox<>(VehicleFactory.getNames());
        add(names,BorderLayout.CENTER);
        JButton bAdd = new JButton("Add");
        add(bAdd,BorderLayout.SOUTH);
        bAdd.addActionListener(e -> addNow());
    }

    private void addNow() {
        Entity carEntity = VehicleFactory.createByID(names.getSelectedIndex(), entityManager);
        entityManager.removeEntityFromParent(carEntity,entityManager.getRoot());
        UndoSystem.addEvent(new EntityAddEdit(entityManager,entityManager.getRoot(),carEntity));
    }
}
