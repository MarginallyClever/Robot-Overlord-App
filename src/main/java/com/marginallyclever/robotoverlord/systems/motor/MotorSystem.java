package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.motors.StepperMotorComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import java.util.LinkedList;
import java.util.List;

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
        if (component instanceof ServoComponent) decorateServo(view, component);
        if (component instanceof MotorComponent) decorateMotor(view, component);
    }

    private void decorateServo(ComponentPanelFactory view, Component component) {
        ServoComponent servo = (ServoComponent) component;

        ViewElementButton bCurve = view.addButton("Tune PID");
        bCurve.addActionEventListener(e -> editPID(bCurve, servo));
        view.add(servo.kP);
        view.add(servo.kI);
        view.add(servo.kD);
        view.add(servo.desiredAngle);
    }

    private void editPID(JComponent parent, ServoComponent servo) {
        TuneServoPIDPanel panel = new TuneServoPIDPanel(servo,this);
        EntitySystemUtils.makePanel(panel, parent, "Tune PID");
    }

    private void decorateMotor(ComponentPanelFactory view, Component component) {
        MotorComponent motor = (MotorComponent) component;
        view.add(motor.currentAngle);
        view.add(motor.currentVelocity);
        view.add(motor.desiredVelocity);
        view.add(motor.gearRatio);

        ViewElementButton bCurve = view.addButton("Torque curve");
        bCurve.addActionEventListener(e -> editCurve(bCurve, motor));
    }

    public void editCurve(JComponent parent, MotorComponent motor) {
        TorqueCurveEditPanel panel = new TorqueCurveEditPanel(motor);
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

    public void updateMotor(MotorComponent motor, double dt) {
        if(motor instanceof ServoComponent) updateServo((ServoComponent)motor,dt);
        else if(motor instanceof StepperMotorComponent) {}
        //else if(motor instanceof DCMotorComponent) {}
        else updateMotorBasic(motor, dt);
    }

    private void updateServo(ServoComponent servo, double dt) {
        if(!servo.enabled.get()) return;

        double desiredAngle = servo.desiredAngle.get();
        double currentAngle = servo.currentAngle.get();

        // PID control
        // Use a simple proportional control to move towards the desired angle.
        // The constant of proportionality would depend on your specific servo and application.
        double kP = servo.kP.get();
        double kI = servo.kI.get();
        double kD = servo.kD.get();

        // Calculate the angle difference
        double error = desiredAngle - currentAngle;

        double errorSum = servo.errorSum.get();
        double previousError = servo.lastError.get();
        errorSum += error;

        double derivative = error - previousError;
        double output = kP*error + kI*errorSum + kD*derivative;

        servo.lastError.set(error);
        servo.errorSum.set(errorSum);

        // Update the current angle.
        // Note that we're not considering physical limitations here like max speed or acceleration of the servo.
        servo.setDesiredVelocity(output);

        updateMotorBasic(servo, dt);
    }

    private void updateMotorBasic(MotorComponent motor, double dt) {
        if(!motor.enabled.get()) return;

        double currentVelocity = motor.getCurrentVelocity();
        double desiredVelocity = motor.getDesiredVelocity();
        if (currentVelocity == desiredVelocity) return;

        double torque = motor.getTorqueAtRpm(currentVelocity);
        // assume direct relationship
        double acceleration = torque;
        double dv = acceleration * dt;
        if (currentVelocity < desiredVelocity) {
            currentVelocity = Math.min(currentVelocity + dv, desiredVelocity);
        } else {
            currentVelocity = Math.max(currentVelocity - dv, desiredVelocity);
        }
        motor.setCurrentVelocity(currentVelocity);

        // adjust angle
        double newAngle = motor.currentAngle.get() + motor.getCurrentVelocity() * dt;

        rotateMotor(motor, newAngle);
    }

    public void rotateMotor(MotorComponent motor, double newAngle) {
        double oldAngle = motor.currentAngle.get();

        // Ensure the current angle stays within the valid range for a servo (typically -180 to 180 degrees,
        // but could be different depending on your servo)
        //if (newAngle > 360) newAngle -= 360;
        //else if (newAngle < 0) newAngle += 360;

        motor.currentAngle.set(newAngle);

        double diff = newAngle - oldAngle;
        if (diff > 180) diff -= 360;
        else if (diff < -180) diff += 360;

        List<Entity> children = motor.getEntity().getChildren();
        if(children.size()==0) return;

        Entity firstchild = children.get(0);
        PoseComponent childPose = firstchild.getComponent(PoseComponent.class);
        Matrix4d m = childPose.getLocal();
        Matrix4d rotZ = new Matrix4d();
        rotZ.rotZ(Math.toRadians(diff));
        m.mul(rotZ);
        childPose.setLocalMatrix4(m);
    }
}
