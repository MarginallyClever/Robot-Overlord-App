package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.convenience.swing.LineGraph;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.motors.DCMotorComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.motors.StepperMotorComponent;
import com.marginallyclever.robotoverlord.components.vehicle.CarComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * <p>For motors</p>
 *
 * @author Dan Royer
 * @since 2.6.2
 */
public class MotorSystem implements EntitySystem {
    private final EntityManager entityManager;

    public MotorSystem(EntityManager entityManager) {
        super();
        this.entityManager = entityManager;
    }

    /**
     * Get the Swing view of this component.
     *
     * @param view      the factory to use to create the panel
     * @param component the component to visualize
     */
    @Override
    public void decorate(ComponentPanelFactory view, Component component) {
        if (component instanceof MotorComponent) decorateMotor(view, component);
        if (component instanceof ServoComponent) decorateServo(view, component);
    }

    private void decorateServo(ComponentPanelFactory view, Component component) {
        ServoComponent servo = (ServoComponent) component;
        view.add(servo.desiredAngle);
    }

    private void decorateMotor(ComponentPanelFactory view, Component component) {
        MotorComponent motor = (MotorComponent) component;
        view.add(motor.gearRatio);
        ViewElementButton bCurve = view.addButton("Torque curve");
        bCurve.addActionEventListener(e -> MotorSystem.editCurve(bCurve, motor));
    }

    public static void editCurve(JComponent parent, MotorComponent motor) {
        LineGraph graph = new LineGraph();
        RPMToTorqueTable table = new RPMToTorqueTable();

        TreeMap<Double, Double> curve = motor.getTorqueCurve();
        List<Double> keys = new ArrayList<>(curve.keySet());
        for (Double key : keys) {
            graph.addValue(key, curve.get(key));
            table.addValue(key, curve.get(key));
        }
        graph.setBoundsToData();
        graph.setXMin(0);
        graph.setYMin(0);

        table.addDataChangeListener((evt) -> {
            int row = evt.getFirstRow();
            int col = evt.getColumn();
            TableModel model = (TableModel) evt.getSource();

            if (evt.getType() == TableModelEvent.DELETE) {
                try {
                    int rpm = (int) Integer.parseInt((String) model.getValueAt(row, 0));
                    graph.removeValue(rpm);
                    motor.removeTorqueAtRPM(rpm);
                } catch (NumberFormatException ignore) {
                }

            } else if (evt.getType() == TableModelEvent.UPDATE ||
                    evt.getType() == TableModelEvent.INSERT) {
                try {
                    int rpm = (int) Integer.parseInt((String) model.getValueAt(row, 0));
                    double torque = (double) Double.parseDouble((String) model.getValueAt(row, 1));
                    graph.removeValue(rpm);
                    graph.addValue(rpm, torque);
                    motor.setTorqueAtRPM(rpm, torque);
                } catch (NumberFormatException ignore) {
                }
            }
            graph.setBoundsToData();
            graph.setXMin(0);
            graph.setYMin(0);
            graph.repaint();
        });

        graph.setPreferredSize(new Dimension(300, 200));
        table.setPreferredSize(new Dimension(300, 200));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(graph, BorderLayout.CENTER);
        panel.add(table, BorderLayout.SOUTH);


        EntitySystemUtils.makePanel(panel, parent, "Torque curve");
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    public void update(double dt) {
        List<Entity> list = new LinkedList<>(entityManager.getEntities());
        while (!list.isEmpty()) {
            Entity e = list.remove(0);
            list.addAll(e.getChildren());

            MotorComponent found = e.getComponent(MotorComponent.class);
            if (found != null) updateMotor(found, dt);
        }
    }

    private void updateMotor(MotorComponent motor, double dt) {
        if (motor instanceof ServoComponent) updateServo((ServoComponent)motor,dt);
        else if (motor instanceof DCMotorComponent) {}
        else if (motor instanceof StepperMotorComponent) {}
        else updateMotorBasic(motor, dt);
    }

    private void updateServo(ServoComponent motor, double dt) {
        double desiredAngle = motor.desiredAngle.get();
        double currentAngle = motor.currentAngle.get();

        // Use a simple proportional control to move towards the desired angle.
        // The constant of proportionality would depend on your specific servo and application.
        double k = 1.0;

        // Calculate the angle difference
        double deltaAngle = desiredAngle - currentAngle;

        // Update the current angle.
        // Note that we're not considering physical limitations here like max speed or acceleration of the servo.
        motor.setDesiredVelocity(k * deltaAngle);

        updateMotorBasic(motor, dt);

    }

    private void updateMotorBasic(MotorComponent motor, double dt) {
        double currentVelocity = motor.getCurrentVelocity();
        double desiredVelocity = motor.getDesiredVelocity();
        if(currentVelocity==desiredVelocity) return;

        double torque = motor.getTorqueAtRpm(currentVelocity);
        // assume direct relationship
        double acceleration = torque;
        double dv = acceleration * dt;
        if(currentVelocity<desiredVelocity) {
            currentVelocity = Math.min(currentVelocity+dv,desiredVelocity);
        } else {
            currentVelocity = Math.max(currentVelocity-dv,desiredVelocity);
        }
        motor.setCurrentVelocity(currentVelocity);

        // adjust angle
        double currentAngle = motor.currentAngle.get();
        currentAngle += motor.getCurrentVelocity() * dt;

        // Ensure the current angle stays within the valid range for a servo (typically -180 to 180 degrees,
        // but could be different depending on your servo)
        if (currentAngle > 360) currentAngle -= 360;
        else if (currentAngle < 0) currentAngle += 360;
        motor.currentAngle.set(currentAngle);
    }
}
