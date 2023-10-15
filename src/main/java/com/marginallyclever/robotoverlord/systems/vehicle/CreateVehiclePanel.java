package com.marginallyclever.robotoverlord.systems.vehicle;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.AbstractParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementFactory;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.edits.EntityAddEdit;

import javax.swing.*;
import java.awt.*;

public class CreateVehiclePanel extends JPanel {
    private final EntityManager entityManager;
    private final IntParameter vehicleType = new IntParameter("vehicle type",0);
    private final DoubleParameter wheelRadius = new DoubleParameter("wheel radius",2.0);
    private final DoubleParameter wheelWidth = new DoubleParameter("wheel width",0.5);
    private final DoubleParameter bodyLength = new DoubleParameter("body length",18);
    private final DoubleParameter bodyWidth = new DoubleParameter("body width",20);
    private final DoubleParameter bodyHeight = new DoubleParameter("body height",2);
    private final DoubleParameter bodyRadius = new DoubleParameter("body radius",8);
    private final DoubleParameter groundClearance = new DoubleParameter("ground clearance",1.0);

    public CreateVehiclePanel(EntityManager entityManager) {
        super(new BorderLayout(2,2));
        this.entityManager = entityManager;

        this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        ViewElementFactory factory = new ViewElementFactory(entityManager);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center,BoxLayout.Y_AXIS));


        center.add(factory.addComboBox(vehicleType,VehicleFactory.getNames()));

        AbstractParameter<?> [] list = {wheelRadius,wheelWidth,bodyLength,bodyWidth,bodyHeight,bodyRadius,groundClearance};
        for(AbstractParameter<?> p : list) {
            center.add(factory.add(p));
            p.addPropertyChangeListener(e->updateVehicleFactory());
        }
        add(center,BorderLayout.NORTH);

        JButton bAdd = new JButton("Add");
        add(bAdd,BorderLayout.SOUTH);
        bAdd.addActionListener(e -> addNow());
        this.setMinimumSize(this.getPreferredSize());
    }

    private void updateVehicleFactory() {
        VehicleFactory.setWheelRadius(wheelRadius.get());
        VehicleFactory.setWheelWidth(wheelWidth.get());
        VehicleFactory.setBodyLength(bodyLength.get());
        VehicleFactory.setBodyWidth(bodyWidth.get());
        VehicleFactory.setBodyHeight(bodyHeight.get());
        VehicleFactory.setBodyRadius(bodyRadius.get());
        VehicleFactory.setGroundClearance(groundClearance.get());
    }

    private void addNow() {
        Entity carEntity = VehicleFactory.createByID(vehicleType.get(), entityManager);
        entityManager.removeEntityFromParent(carEntity,entityManager.getRoot());
        UndoSystem.addEvent(new EntityAddEdit(entityManager,entityManager.getRoot(),carEntity));
    }

    public static void main(String[] args) {
        UndoSystem.start();
        EntityManager entityManager = new EntityManager();
        CreateVehiclePanel panel = new CreateVehiclePanel(entityManager);
        JFrame frame = new JFrame("CreateVehiclePanel");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(250,100));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}