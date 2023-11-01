package com.marginallyclever.robotoverlord.systems.vehicle;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.vehicle.VehicleComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementButton;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.EntitySystemUtils;

import javax.swing.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.LinkedList;
import java.util.List;

/**
 * A system that manages all vehicles.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
public class VehicleSystem implements EntitySystem {
    private final EntityManager entityManager;

    public VehicleSystem(EntityManager entityManager) {
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
        if(component instanceof VehicleComponent) decorateCar(view, (VehicleComponent)component);
        if(component instanceof WheelComponent) decorateWheel(view, (WheelComponent)component);
    }

    private void decorateCar(ComponentSwingViewFactory view, VehicleComponent car) {
        view.addComboBox(car.wheelType, VehicleComponent.wheelTypeNames);
        view.add(car.turnVelocity);
        view.add(car.forwardVelocity);
        view.add(car.strafeVelocity);
        view.add(car.wheels);

        // TODO: how to manage a list?

        ViewElementButton bDrive = view.addButton("Drive");
        bDrive.addActionEventListener(evt -> openDrivePanel(bDrive,car));
    }

    private void openDrivePanel(JComponent parent, VehicleComponent car) {
        DriveVehiclePanel panel = new DriveVehiclePanel(car);;
        EntitySystemUtils.makePanel(panel, parent, "Drive Vehicle");
    }

    private void decorateWheel(ComponentSwingViewFactory view, WheelComponent wheel) {
        view.add(wheel.diameter);
        view.add(wheel.width);
        view.add(wheel.drive);
        view.add(wheel.steer);
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {
        List<Entity> list = new LinkedList<>(entityManager.getEntities());
        while (!list.isEmpty()) {
            Entity e = list.remove(0);
            list.addAll(e.getChildren());

            VehicleComponent found = e.getComponent(VehicleComponent.class);
            if (found != null) updateCar(found, dt);
        }
    }

    private void updateCar(VehicleComponent car, double dt) {
        if(!car.getEnabled()) return;
        if(car.wheels.isEmpty()) return;  // nothing to do

        switch (car.wheelType.get()) {
            case VehicleComponent.WHEEL_OMNI -> updateCarOmni(car, dt);
            case VehicleComponent.WHEEL_MECANUM -> updateCarMecanum(car, dt);
            case VehicleComponent.WHEEL_DIFFERENTIAL -> updateCarDifferential(car, dt);
            default -> updateCarNormal(car, dt);
        }
    }

    /**
     * Assumes wheel 0 is left and wheel 1 is right
     * @param car the car to update
     * @param dt time step
     */
    private void updateCarDifferential(VehicleComponent car, double dt) {
        double forwardVel = car.forwardVelocity.get();
        double turnVel = Math.toRadians(car.turnVelocity.get());
        //=double strafeVel = car.strafeVelocity.get();

        Entity wheelEntity0 = entityManager.findEntityByUniqueID(car.wheels.get(0).get());
        WheelComponent wheel0 = wheelEntity0.getComponent(WheelComponent.class);
        MotorComponent driveMotor0 = entityManager.findEntityByUniqueID(wheel0.drive.get()).getComponent(MotorComponent.class);
        Vector3d local0 = MatrixHelper.getPosition(wheelEntity0.getComponent(PoseComponent.class).getLocal());
        double wheelDistanceFromCenter0 = local0.length();

        Entity wheelEntity1 = entityManager.findEntityByUniqueID(car.wheels.get(1).get());
        WheelComponent wheel1 = wheelEntity1.getComponent(WheelComponent.class);
        MotorComponent driveMotor1 = entityManager.findEntityByUniqueID(wheel1.drive.get()).getComponent(MotorComponent.class);
        Vector3d local1 = MatrixHelper.getPosition(wheelEntity1.getComponent(PoseComponent.class).getLocal());
        double wheelDistanceFromCenter1 = local1.length();

        // Calculate the desired linear velocity of each wheel.
        double vLeft = forwardVel + turnVel * wheelDistanceFromCenter0;
        double vRight = forwardVel - turnVel * wheelDistanceFromCenter1;

        // Convert these to angular velocities (RPM) for each wheel.
        double leftWheelRPM  = getRPMFromWheelVelocity(vLeft, wheel0.diameter.get());
        double rightWheelRPM = getRPMFromWheelVelocity(vRight, wheel1.diameter.get());

        driveMotor0.setDesiredRPM(leftWheelRPM);
        driveMotor1.setDesiredRPM(rightWheelRPM);

        adjustCarPosition(car, forwardVel, 0.0, turnVel, dt);
    }

    private void updateCarNormal(VehicleComponent car, double dt) {
        WheelComponent poweredWheel = null;
        double forwardVel = car.forwardVelocity.get();
        if(Math.abs(forwardVel)>1e-6) {
            // Drive Motors
            for (ReferenceParameter wheelRef : car.wheels) {
                if(wheelRef.get()==null || wheelRef.get().isEmpty()) continue;
                WheelComponent wheel = entityManager.findEntityByUniqueID(wheelRef.get()).getComponent(WheelComponent.class);

                String driveMotorName = wheel.drive.get();
                if(driveMotorName!=null && !driveMotorName.isEmpty()) {
                    poweredWheel = wheel;
                    MotorComponent driveMotor = entityManager.findEntityByUniqueID(driveMotorName).getComponent(MotorComponent.class);

                    // Update the motor's velocity.
                    double wheelDiameter = wheel.diameter.get();
                    double rpm = getRPMFromWheelVelocity(forwardVel, wheelDiameter);
                    driveMotor.setDesiredRPM(rpm);
                }
            }
        }

        double turnVel = car.turnVelocity.get();
        for (ReferenceParameter wheelRef : car.wheels) {
            if(wheelRef.get()==null || wheelRef.get().isEmpty()) continue;
            WheelComponent wheel = entityManager.findEntityByUniqueID(wheelRef.get()).getComponent(WheelComponent.class);
            steerOneWheel(wheel,car,turnVel);
        }

        if(poweredWheel!=null) {
            updateCarFrontWheelSteering(car, dt, poweredWheel);
        }
    }

    /**
     * @param linearVelocityPerSecond the linear velocity of the wheel in units per second
     * @param wheelDiameter the diameter of the wheel in units
     * @return the revolutions per minute of the wheel
     */
    private double getRPMFromWheelVelocity(double linearVelocityPerSecond, double wheelDiameter) {
        return (linearVelocityPerSecond / (Math.PI * wheelDiameter)) * 60.0;
    }

    /**
     * Assumes first wheel is front of car and last wheel is back of car to get the wheel base.
     * @param car the car to update
     * @param dt time step
     * @param poweredWheel a wheel that is powered
     */
    private void updateCarFrontWheelSteering(VehicleComponent car, double dt, WheelComponent poweredWheel) {
        // get the wheel base from the front and rear wheels
        PoseComponent pose0 = entityManager.findEntityByUniqueID(car.wheels.get(0).get()).getComponent(PoseComponent.class);
        PoseComponent pose2 = entityManager.findEntityByUniqueID(car.wheels.get(car.wheels.size() - 1).get()).getComponent(PoseComponent.class);
        Vector3d p0 = pose0.getPosition();
        Vector3d p2 = pose2.getPosition();
        p2.sub(p0);
        double wheelBase = p2.length();

        // get the thrust of a powered wheel
        String driveName = poweredWheel.drive.get();
        MotorComponent driveMotor = entityManager.findEntityByUniqueID(driveName).getComponent(MotorComponent.class);
        // 2 * pi * r * rpm / 60
        double linearVelocity = poweredWheel.diameter.get() * Math.PI * (driveMotor.getCurrentRPM()/60.0);

        // find the rate of turn based on the wheel base and the turn velocity
        double steeringAngleRadians = Math.toRadians(car.turnVelocity.get());
        double turningRadius = wheelBase / Math.tan(steeringAngleRadians);
        double rateOfTurnRadians = linearVelocity / turningRadius;

        adjustCarPosition(car, linearVelocity, 0.0, rateOfTurnRadians, dt);
    }

    private void adjustCarPosition(VehicleComponent car, double linearVelocity, double strafeVelocity, double rateOfTurnRadians, double dt) {
        // get car position
        PoseComponent carPose = car.getEntity().getComponent(PoseComponent.class);
        Matrix4d carWorld = carPose.getWorld();
        Vector3d pos = MatrixHelper.getPosition(carWorld);

        // Use the turn velocity to rotate the car
        Matrix4d rot = new Matrix4d();
        rot.rotZ(rateOfTurnRadians * dt);

        // put it together
        carWorld.mul(rot,carWorld);
        Vector3d wx = MatrixHelper.getXAxis(carWorld);
        Vector3d wy = MatrixHelper.getYAxis(carWorld);
        wx.scale(linearVelocity * dt);
        wy.scale(strafeVelocity * dt);
        pos.add(wx);
        pos.add(wy);
        MatrixHelper.setPosition(carWorld,pos);

        // apply
        carPose.setWorld(carWorld);
    }

    private void steerOneWheel(WheelComponent wheel, VehicleComponent car, double wheelAngle) {
        String steerMotorName = wheel.steer.get();
        if(steerMotorName==null || steerMotorName.isEmpty()) return;
        ServoComponent steerMotor = entityManager.findEntityByUniqueID(steerMotorName).getComponent(ServoComponent.class);
        if(steerMotor==null) return;

        steerMotor.desiredAngle.set(wheelAngle);
    }

    /**
     * assumes a standard 3-wheel omni car setup with each wheel 120 degrees apart.
     * @param car the car to update
     * @param dt the time step in seconds
     */
    private void updateCarOmni(VehicleComponent car, double dt) {
        double vForward = car.forwardVelocity.get();
        double vStrafe = car.strafeVelocity.get();
        double vTurn = Math.toRadians(car.turnVelocity.get());

        double[] wheelAngles = {0, 2.0 * Math.PI/3.0, 4.0 * Math.PI/3.0};  // Assumed relative wheel angles for 3-wheel omni.

        for (int i = 0; i < car.wheels.size(); i++) {
            ReferenceParameter wheelRef = car.wheels.get(i);
            if(wheelRef.get()==null || wheelRef.get().isEmpty()) continue;
            WheelComponent wheel = entityManager.findEntityByUniqueID(wheelRef.get()).getComponent(WheelComponent.class);

            String driveMotorName = wheel.drive.get();
            assert driveMotorName != null && !driveMotorName.isEmpty();
            MotorComponent driveMotor = entityManager.findEntityByUniqueID(wheel.drive.get()).getComponent(MotorComponent.class);

            PoseComponent wheelPose = wheel.getEntity().getComponent(PoseComponent.class);
            Vector3d wheelPositionRelative = MatrixHelper.getPosition(wheelPose.getLocal());
            double d = wheelPositionRelative.length();

            // Calculate the desired velocities.
            double vWheel = -vForward * Math.sin(wheelAngles[i]) + vStrafe * Math.cos(wheelAngles[i]) + d * vTurn;

            // Update the motor's velocity.
            double rpm = getRPMFromWheelVelocity(-vWheel, wheel.diameter.get());
            driveMotor.setCurrentRPM(rpm);
        }

        adjustCarPosition(car, vForward, vStrafe, vTurn, dt);
    }

    /**
     * Assumes a standard 4-wheel mecanum car setup.
     * @param car the car to update
     * @param dt the time step in seconds
     */
    private void updateCarMecanum(VehicleComponent car, double dt) {
        double vForward = car.forwardVelocity.get();
        double vStrafe = car.strafeVelocity.get();
        double vTurn = Math.toRadians(car.turnVelocity.get());

        for(int i=0;i<car.wheels.size();++i) {
            ReferenceParameter wheelRef = car.wheels.get(i);
            if(wheelRef.get()==null || wheelRef.get().isEmpty()) continue;

            // Fetch the WheelComponent and MotorComponent.
            WheelComponent wheel = entityManager.findEntityByUniqueID(wheelRef.get()).getComponent(WheelComponent.class);
            String driveMotorName = wheel.drive.get();
            assert driveMotorName != null && !driveMotorName.isEmpty();
            MotorComponent driveMotor = entityManager.findEntityByUniqueID(wheel.drive.get()).getComponent(MotorComponent.class);

            // Fetch the PoseComponent of the car and the wheel.
            PoseComponent carPose = car.getEntity().getComponent(PoseComponent.class);
            PoseComponent wheelPose = wheel.getEntity().getComponent(PoseComponent.class);

            // Calculate the position of the wheel relative to the car's center of mass.
            // This will depend on the details of your PoseComponent and Matrix4d classes.
            // Placeholder line, replace it with your actual computation.
            Matrix4d wheelLocal = wheelPose.getLocal();
            Vector3d wheelPositionRelative = MatrixHelper.getPosition(wheelLocal);
            double dx = Math.abs(wheelPositionRelative.x);
            double dy = Math.abs(wheelPositionRelative.y);

            // Determine the wheel's relative orientation (front-left and rear-right are positive, front-right and rear-left are negative)
            int orientation = (i % 2 == 0) ? 1 : -1;

            // Calculate the desired velocities.
            // This depends on the specifics of your setup, but for a common setup, it might look like this:
            double vWheel = vForward
                    + orientation * vStrafe
                    + ((i < 2) ? -1 : 1) * (dx + dy) * vTurn;

            // Update the motor's velocity.
            double rpm = getRPMFromWheelVelocity(vWheel, wheel.diameter.get());
            driveMotor.setCurrentRPM(rpm);
        }

        adjustCarPosition(car, vForward, vStrafe, vTurn, dt);
    }
}
