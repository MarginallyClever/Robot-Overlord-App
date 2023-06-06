package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.vehicle.CarComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ViewElementButton;

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
    public void decorate(ComponentPanelFactory view, Component component) {
        if(component instanceof CarComponent) decorateCar(view, (CarComponent)component);
        if(component instanceof WheelComponent) decorateWheel(view, (WheelComponent)component);
    }

    private void decorateCar(ComponentPanelFactory view, CarComponent component) {
        view.addComboBox(component.wheelType, CarComponent.wheelTypeNames);

        // TODO: how to manage a list?

        ViewElementButton bDrive = view.addButton("Drive");
        bDrive.addActionEventListener(evt -> {
            // TODO: open panel to drive the car
        });
    }

    private void decorateWheel(ComponentPanelFactory view, WheelComponent component) {
        view.add(component.diameter);
        view.add(component.width);
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

            CarComponent found = e.getComponent(CarComponent.class);
            if (found != null) updateCar(found, dt);
        }
    }

    private void updateCar(CarComponent car, double dt) {
        if(car.wheels.size()==0) return;  // nothing to do

        switch (car.wheelType.get()) {
            case CarComponent.WHEEL_OMNI -> updateCarOmni(car, dt);
            case CarComponent.WHEEL_MECANUM -> updateCarMecanum(car, dt);
            //case CarComponent.WHEEL_TRACTION -> updateCarTraction(car, dt);
            default -> updateNormal(car, dt);
        }

        updateCarBody(car, dt);
    }

    private void updateCarBody(CarComponent car, double dt) {
        // Initialize total force as zero
        Vector3d totalForce = new Vector3d(0, 0,0);

        //System.out.println("  car="+car.getEntity().getName());

        for (ReferenceParameter wheelRef : car.wheels) {
            // Fetch the WheelComponent and MotorComponent.
            WheelComponent wheel = entityManager.findEntityByUniqueID(wheelRef.get()).getComponent(WheelComponent.class);
            String driveName = wheel.drive.get();
            if(driveName==null || driveName.isEmpty()) continue;
            MotorComponent driveMotor = entityManager.findEntityByUniqueID(wheel.drive.get()).getComponent(MotorComponent.class);

            // Calculate the thrust of the wheel.
            double forceMagnitude = calculateForceMagnitude(car,wheel,driveMotor);

            // For omni or mecanum wheels, the direction of the force also depends on the orientation of the wheel
            PoseComponent wheelPose = wheel.getEntity().getComponent(PoseComponent.class);
            // Get the orientation of the wheel. This depends on the details of PoseComponent.
            Vector3d wheelOrientation = MatrixHelper.getXAxis(wheelPose.getWorld());

            // Calculate the force vector based on the wheel type
            Vector3d wheelForce = new Vector3d(forceMagnitude * wheelOrientation.x, forceMagnitude * wheelOrientation.y,0);

            // Add the wheel's force to the total force
            totalForce.add(wheelForce);
            //System.out.println("  wheelForce="+wheelForce);
        }

        //System.out.println("  totalForce="+totalForce);

        // get
        PoseComponent carPose = car.getEntity().getComponent(PoseComponent.class);
        Matrix4d carWorld = carPose.getWorld();
        Vector3d pos = MatrixHelper.getPosition(carWorld);
        pos.add(totalForce);
        // use the turn velocity to rotate the car
        Matrix4d rot = MatrixHelper.createIdentityMatrix4();
        rot.rotZ(Math.toRadians(car.turnVelocity.get()));
        // put it together
        carWorld.mul(rot);
        MatrixHelper.setPosition(carWorld,pos);
        // apply
        carPose.setWorld(carWorld);
    }

    public double calculateForceMagnitude(CarComponent car, WheelComponent wheel, MotorComponent driveMotor) {
        // Approximate the actual torque output
        double torque = driveMotor.getCurrentTorque();
        // Calculate thrust (force) produced by the wheel
        return torque * driveMotor.gearRatio.get() / (wheel.diameter.get() / 2.0);
    }

    private void updateNormal(CarComponent car, double dt) {
        double vForward = car.forwardVelocity.get();
        double vTurn = Math.toRadians(car.turnVelocity.get());  // Fetch the turn velocity.

        // Compute the turn radius from the forward and turn velocities.
        double rTurn = vForward / vTurn;

        for (ReferenceParameter wheelRef : car.wheels) {
            WheelComponent wheel = entityManager.findEntityByUniqueID(wheelRef.get()).getComponent(WheelComponent.class);

            // Drive Motor
            String driveMotorName = wheel.drive.get();
            if(driveMotorName!=null && !driveMotorName.isEmpty()) {
                MotorComponent driveMotor = entityManager.findEntityByUniqueID(driveMotorName).getComponent(MotorComponent.class);

                // Update the motor's velocity.
                double r = wheel.diameter.get() * 0.5;
                double rpm = (vForward / (2 * Math.PI * r)) * 60;
                driveMotor.setDesiredVelocity(rpm);
            }

            // Steer Motor
            String steerMotorName = wheel.steer.get();
            if(steerMotorName!=null && !steerMotorName.isEmpty()) {
                ServoComponent steerMotor = entityManager.findEntityByUniqueID(steerMotorName).getComponent(ServoComponent.class);

                // Fetch the PoseComponent of the car and the wheel.
                PoseComponent carPose = car.getEntity().getComponent(PoseComponent.class);
                PoseComponent wheelPose = wheel.getEntity().getComponent(PoseComponent.class);

                // Calculate the position of the wheel relative to the car's center of mass.
                Matrix4d wheelLocal = wheelPose.getLocal();
                Vector3d wheelPositionRelative = MatrixHelper.getPosition(wheelLocal);

                // Calculate the desired steering angle based on the turn radius.
                double wheelBase = wheelPositionRelative.y;
                double desiredSteeringAngle = Math.toDegrees(Math.atan(wheelBase / rTurn));
                steerMotor.desiredAngle.set(desiredSteeringAngle);
            }
        }
    }

    /**
     * assumes a standard 3-wheel omni car setup with each wheel 120 degrees apart.
     * @param car the car to update
     * @param dt the time step in seconds
     */
    private void updateCarOmni(CarComponent car, double dt) {
        double vForward = car.forwardVelocity.get();
        double vStrafe = car.strafeVelocity.get();
        double vTurn = car.turnVelocity.get();  // Fetch the turn velocity.

        double[] wheelAngles = {0, 120, 240};  // Assumed relative wheel angles for 3-wheel omni.

        for (int i = 0; i < car.wheels.size(); i++) {
            ReferenceParameter wheelRef = car.wheels.get(i);
            WheelComponent wheel = entityManager.findEntityByUniqueID(wheelRef.get()).getComponent(WheelComponent.class);

            String driveMotorName = wheel.drive.get();
            assert driveMotorName != null && !driveMotorName.isEmpty();
            MotorComponent driveMotor = entityManager.findEntityByUniqueID(wheel.drive.get()).getComponent(MotorComponent.class);

            PoseComponent wheelPose = wheel.getEntity().getComponent(PoseComponent.class);
            Matrix4d wheelLocal = wheelPose.getLocal();
            Vector3d wheelPositionRelative = MatrixHelper.getPosition(wheelLocal);

            // Calculate the desired velocities.
            double vRotate = vTurn * wheelPositionRelative.y; // Assuming y corresponds to lateral distance from the center.

            // Calculate the wheel's contribution to the desired velocities.
            // Convert wheel angle to radians for trigonometric calculations.
            double wheelAngleRadians = Math.toRadians(wheelAngles[i]);
            double vWheelForward = vForward * Math.cos(wheelAngleRadians);
            double vWheelStrafe = vStrafe * Math.sin(wheelAngleRadians);

            // Update the motor's velocity.
            double v = vWheelForward + vWheelStrafe + vRotate;
            double r = wheel.diameter.get() * 0.5;
            double rpm = (v / (2 * Math.PI * r)) * 60;
            driveMotor.setCurrentVelocity(rpm);
        }
    }

    /**
     * Assumes a standard 4-wheel mecanum car setup.
     * @param car the car to update
     * @param dt the time step in seconds
     */
    private void updateCarMecanum(CarComponent car, double dt) {
        double vForward = car.forwardVelocity.get();
        double vStrafe = car.strafeVelocity.get();
        double vTurn = car.turnVelocity.get();  // Fetch the turn velocity.

        for (ReferenceParameter wheelRef : car.wheels) {
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

            // Calculate the desired velocities.
            double vRotate = vTurn * wheelPositionRelative.y;

            // Update the motor's velocity.
            double v = vForward + vStrafe + vRotate;
            double r = wheel.diameter.get()*0.5;
            double rpm = (v / (2 * Math.PI * r)) * 60;
            driveMotor.setCurrentVelocity(rpm);
        }
    }
}
