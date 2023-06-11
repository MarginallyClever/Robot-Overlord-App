package com.marginallyclever.robotoverlord.systems.vehicle;

import com.marginallyclever.robotoverlord.components.vehicle.CarComponent;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that allows the user to drive a vehicle.
 * @since 2.6.4
 * @author Dan Royer
 */
public class DriveVehiclePanel extends JPanel {
    private final CarComponent car;
    private final JSlider sliderThrottle = new JSlider(-100,100,0);
    private final JSlider sliderStrafe = new JSlider(-100,100,0);
    private final JSlider sliderSteering = new JSlider(-90,90,0);

    public DriveVehiclePanel(CarComponent car) {
        super();
        this.car = car;

        this.setLayout(new GridLayout(3,2));
        this.add(new JLabel("Throttle"));
        this.add(sliderThrottle);
        this.add(new JLabel("Steering"));
        this.add(sliderSteering);
        this.add(new JLabel("Strafe"));
        this.add(sliderStrafe);

        sliderSteering.setValue(car.turnVelocity.get().intValue());
        sliderThrottle.setValue(car.forwardVelocity.get().intValue());
        sliderStrafe.setValue(car.strafeVelocity.get().intValue());

        sliderSteering.addChangeListener(e -> {
            car.turnVelocity.set((double)sliderSteering.getValue());
        });
        sliderThrottle.addChangeListener(e -> {
            car.forwardVelocity.set((double)sliderThrottle.getValue());
        });
        sliderStrafe.addChangeListener(e -> {
            car.strafeVelocity.set((double)sliderStrafe.getValue());
        });
    }
}
