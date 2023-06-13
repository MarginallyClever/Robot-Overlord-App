package com.marginallyclever.robotoverlord.systems.vehicle;

import com.marginallyclever.robotoverlord.components.vehicle.VehicleComponent;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that allows the user to drive a vehicle.
 * @since 2.6.4
 * @author Dan Royer
 */
public class DriveVehiclePanel extends JPanel {
    private final VehicleComponent vehicleComponent;
    private final JSlider sliderThrottle = new JSlider(-100,100,0);
    private final JSlider sliderStrafe = new JSlider(-100,100,0);
    private final JSlider sliderSteering = new JSlider(-90,90,0);

    public DriveVehiclePanel(VehicleComponent vehicleComponent) {
        super();
        this.vehicleComponent = vehicleComponent;

        this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

        this.setLayout(new GridLayout(3,2));
        this.add(new JLabel("Throttle"));
        this.add(sliderThrottle);
        this.add(new JLabel("Steering"));
        this.add(sliderSteering);
        this.add(new JLabel("Strafe"));
        this.add(sliderStrafe);

        sliderThrottle.setMajorTickSpacing(25);
        sliderThrottle.setMinorTickSpacing(5);
        sliderThrottle.setPaintTicks(true);

        sliderStrafe.setMajorTickSpacing(25);
        sliderStrafe.setMinorTickSpacing(5);
        sliderStrafe.setPaintTicks(true);

        sliderSteering.setMajorTickSpacing(45);
        sliderSteering.setMinorTickSpacing(15);
        sliderSteering.setPaintTicks(true);

        sliderSteering.setValue(vehicleComponent.turnVelocity.get().intValue());
        sliderThrottle.setValue(vehicleComponent.forwardVelocity.get().intValue());
        sliderStrafe.setValue(vehicleComponent.strafeVelocity.get().intValue());

        sliderSteering.addChangeListener(e -> {
            vehicleComponent.turnVelocity.set((double)sliderSteering.getValue());
        });
        sliderThrottle.addChangeListener(e -> {
            vehicleComponent.forwardVelocity.set((double)sliderThrottle.getValue());
        });
        sliderStrafe.addChangeListener(e -> {
            vehicleComponent.strafeVelocity.set((double)sliderStrafe.getValue());
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DriveVehiclePanel");
        frame.setContentPane(new DriveVehiclePanel(new VehicleComponent()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(400,200));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
