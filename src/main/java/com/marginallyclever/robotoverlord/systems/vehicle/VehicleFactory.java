package com.marginallyclever.robotoverlord.systems.vehicle;

import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.vehicle.CarComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.motor.MotorFactory;

import javax.vecmath.Vector3d;

public class VehicleFactory {
    /**
     * Build a car with 4 wheels and front wheel steering.
     * Immediately add it to the entityManager root.
     */
    private static Entity build4WheelCarWithNoMotor(EntityManager entityManager) {
        Entity carEntity = new Entity("carWithNoMotor");
        CarComponent car = new CarComponent();
        carEntity.addComponent(car);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());

        // add 4 wheels
        Entity[] wheelEntity = new Entity[4];
        for (int i = 0; i < wheelEntity.length; ++i) {
            // add suspension to the body
            Entity suspension = new Entity("suspension" + i);
            entityManager.addEntityToParent(suspension, carEntity);
            // add wheel to suspension
            wheelEntity[i] = new Entity("wheel" + i);
            entityManager.addEntityToParent(wheelEntity[i], suspension);

            WheelComponent wheel = new WheelComponent();
            wheelEntity[i].addComponent(wheel);
            wheel.diameter.set(2.0);
            wheel.width.set(0.5);
            car.addWheel(wheelEntity[i]);
        }

        // place wheels at the corners of the car
        wheelEntity[0].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, 10, 1));
        wheelEntity[1].getComponent(PoseComponent.class).setPosition(new Vector3d( 10, 10, 1));
        wheelEntity[2].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, -10, 1));
        wheelEntity[3].getComponent(PoseComponent.class).setPosition(new Vector3d( 10, -10, 1));

        return carEntity;
    }


    /**
     * Build a car with 3 wheels.  Steering is omni-style (wheels turn outwards).
     */
    public static Entity buildOmni(EntityManager entityManager) {
        Entity carEntity = new Entity("omni");
        CarComponent car = new CarComponent();
        carEntity.addComponent(car);

        car.wheelType.set(CarComponent.WHEEL_OMNI);

        for (int i = 0; i < 3; ++i) {
            Entity wheelEntity = new Entity("wheel" + i);
            entityManager.addEntityToParent(wheelEntity, carEntity);
            car.addWheel(wheelEntity);

            WheelComponent wc = new WheelComponent();
            wheelEntity.addComponent(wc);
            wc.diameter.set(2.0);
            wc.width.set(0.5);

            // add motors to all wheels
            MotorComponent motor = MotorFactory.createDefaultMotor();
            wheelEntity.addComponent(motor);
            wc.drive.set(motor.getEntity());

            // rotate wheels so they point outwards
            wheelEntity.getComponent(PoseComponent.class).setRotation(new Vector3d(0, 0, 120*i));
            // place wheels at the corners of the car
            wheelEntity.getComponent(PoseComponent.class).setPosition(new Vector3d(
                    10*Math.cos(Math.toRadians(120*i)),
                    10*Math.sin(Math.toRadians(120*i)),
                    1));
        }

        return carEntity;
    }

    /**
     * Build a tank with 2 wheels.  Steering is differential drive.
     */
    public static Entity buildTank(EntityManager entityManager) {
        Entity carEntity = new Entity("tank");
        CarComponent car = new CarComponent();
        carEntity.addComponent(car);

        Entity[] wheelEntity = new Entity[2];
        MotorComponent[] motors = new MotorComponent[wheelEntity.length];
        for (int i = 0; i < wheelEntity.length; ++i) {
            wheelEntity[i] = new Entity("wheel" + i);
            entityManager.addEntityToParent(wheelEntity[i], carEntity);
            car.addWheel(wheelEntity[i]);

            WheelComponent wc = new WheelComponent();
            wheelEntity[i].addComponent(wc);
            wc.diameter.set(2.0);
            wc.width.set(0.5);

            MotorComponent motor = MotorFactory.createDefaultMotor();
            wheelEntity[i].addComponent(motor);
            wc.drive.set(motor.getEntity());
        }

        // place wheels at either side of the car
        wheelEntity[0].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, 0, 1));
        wheelEntity[1].getComponent(PoseComponent.class).setPosition(new Vector3d(10, 0, 1));
        return carEntity;
    }

    public static Entity buildMecanum(EntityManager entityManager) {
        Entity carEntity = build4WheelCarWithNoMotor(entityManager);
        carEntity.setName("Mecanum");
        CarComponent car = carEntity.getComponent(CarComponent.class);

        // add motors to all wheels
        for (int i = 0; i < 4; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            wheelEntity.addComponent(MotorFactory.createDefaultMotor());
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.drive.set(wheelEntity);
        }

        // change wheel type to mecanum
        car.wheelType.set(CarComponent.WHEEL_MECANUM);
        return carEntity;
    }

    /**
     * make car with real-wheel drive
     * @param entityManager
     * @return
     */
    public static Entity buildRWD(EntityManager entityManager) {
        Entity carEntity = build4WheelCarWithNoMotor(entityManager);
        carEntity.setName("RWD");
        CarComponent car = carEntity.getComponent(CarComponent.class);

        // add one motor
        Entity motor = new Entity("Motor");
        motor.addComponent(MotorFactory.createDefaultMotor());
        entityManager.addEntityToParent(motor,carEntity);

        // add front wheel steering
        for (int i = 0; i < 2; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            Entity suspension = wheelEntity.getParent();
            suspension.addComponent(MotorFactory.createDefaultServo());
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.steer.set(suspension);
        }

        // connect the motor to the back wheels
        for (int i = 2; i < 4; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.drive.set(motor);
        }

        return carEntity;
    }

    public static Entity buildFWD(EntityManager entityManager) {
        Entity carEntity = build4WheelCarWithNoMotor(entityManager);
        carEntity.setName("FWD");
        CarComponent car = carEntity.getComponent(CarComponent.class);

        // add one motor
        Entity motor = new Entity("Motor");
        motor.addComponent(MotorFactory.createDefaultMotor());
        entityManager.addEntityToParent(motor,carEntity);

        // add front wheel steering
        for (int i = 0; i < 2; ++i) {
            // add servo in the suspension
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            Entity suspension = wheelEntity.getParent();
            suspension.addComponent(MotorFactory.createDefaultServo());
            // tell wheel for reference later
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.steer.set(suspension);
        }

        // connect the motor to the front wheels
        for (int i = 0; i < 2; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.drive.set(motor);
        }

        return carEntity;
    }
}
