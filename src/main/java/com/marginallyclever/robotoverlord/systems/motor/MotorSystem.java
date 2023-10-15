package com.marginallyclever.robotoverlord.systems.motor;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.DCMotorComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.motors.StepperMotorComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementButton;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
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
    public void decorate(ComponentSwingViewFactory view, Component component) {
        if (component instanceof ServoComponent) decorateServo(view, (ServoComponent) component);
        if (component instanceof StepperMotorComponent) decorateStepper(view, (StepperMotorComponent)component);
        if (component instanceof MotorComponent) decorateMotor(view, (MotorComponent) component);
    }

    private void decorateStepper(ComponentSwingViewFactory view, StepperMotorComponent stepper) {
        view.add(stepper.stepPerRevolution);
        view.add(stepper.microStepping);
        view.addComboBox(stepper.direction,StepperMotorComponent.directionNames);
        view.addButton("Step").addActionEventListener(e -> stepNow(stepper));
    }

    private void stepNow(StepperMotorComponent stepper) {
        double degreePerStep = 360.0/(stepper.microStepping.get() * stepper.stepPerRevolution.get());
        double angle = stepper.currentAngle.get();
        if(stepper.direction.get()==StepperMotorComponent.DIRECTION_BACKWARD) angle -= degreePerStep;
        else angle += degreePerStep;

        rotateMotor(stepper,angle);
    }

    private void decorateServo(ComponentSwingViewFactory view, ServoComponent servo) {
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

    private void decorateMotor(ComponentSwingViewFactory view, MotorComponent motor) {
        view.add(motor.currentAngle);
        view.add(motor.currentRPM);
        view.add(motor.desiredRPM);
        view.add(motor.gearRatio);
        view.add(motor.connectedTo);

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
        else if(motor instanceof StepperMotorComponent) updateStepper((StepperMotorComponent)motor,dt);
        else if(motor instanceof DCMotorComponent) updateMotorBasic(motor, dt);
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

        servo.setDesiredRPM(output);

        updateMotorBasic(servo, dt);
    }

    private void updateStepper(StepperMotorComponent motor, double dt) {
        if(!motor.enabled.get()) return;

        // adjust angle
        double degreesPerSecond = motor.getCurrentRPM()*360.0/60.0;
        double newAngle = motor.currentAngle.get() + degreesPerSecond * dt;

        rotateMotor(motor, newAngle);
    }

    private void updateMotorBasic(MotorComponent motor, double dt) {
        if(!motor.enabled.get()) return;

        double currentRPM = motor.getCurrentRPM();
        double desiredRPM = motor.getDesiredRPM();
        if (currentRPM != desiredRPM) {
            double torque = motor.getTorqueAtRpm(currentRPM);
            // assume direct relationship
            double acceleration = torque;
            double dv = acceleration * dt;
            if (currentRPM < desiredRPM) {
                currentRPM = Math.min(currentRPM + dv, desiredRPM);
            } else {
                currentRPM = Math.max(currentRPM - dv, desiredRPM);
            }
            motor.setCurrentRPM(currentRPM);
        }

        // adjust angle
        double degreesPerSecond = motor.getCurrentRPM()*6.0;  // 1 rpm = 6 deg/s.
        double newAngle = motor.currentAngle.get() + degreesPerSecond * dt;

        rotateMotor(motor, newAngle);
    }

    /**
     * Rotate the motor to the specified angle.
     * @param motor the motor to rotate
     * @param newAngle the new angle in degrees
     */
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

        rotateAffectedEntities(motor, diff);
    }

    private void rotateAffectedEntities(MotorComponent motor, double diff) {
        Entity motorEntity = motor.getEntity();
        if(motorEntity==null) return;

        for(ReferenceParameter name : motor.connectedTo) {
            String uid = name.get();
            if(uid==null || uid.isEmpty()) continue;
            Entity connection = entityManager.findEntityByUniqueID(name.get());
            PoseComponent childPose = connection.getComponent(PoseComponent.class);
            Matrix4d m = childPose.getLocal();
            Matrix4d rotZ = new Matrix4d();
            rotZ.rotZ(Math.toRadians(diff));
            m.mul(rotZ);
            childPose.setLocalMatrix4(m);
        }
    }
}
