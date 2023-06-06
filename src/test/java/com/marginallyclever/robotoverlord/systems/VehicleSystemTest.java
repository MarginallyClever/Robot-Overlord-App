package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.vehicle.CarComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.motor.MotorSystem;
import com.marginallyclever.robotoverlord.systems.motor.MotorSystemTest;
import org.junit.jupiter.api.*;

import javax.vecmath.Vector3d;
import java.util.List;

/**
 * Build and drive a variety of vehicles.
 *
 * @since 2.6.3
 * @author Dan Royer
 */
public class VehicleSystemTest {
    private EntityManager entityManager;
    private VehicleSystem vehicleSystem;
    private MotorSystem motorSystem;
    private List<EntitySystem> systems;
    private CarComponent car;

    @BeforeEach
    public void setUp() {
        entityManager = new EntityManager();
        vehicleSystem = new VehicleSystem(entityManager);
        motorSystem = new MotorSystem(entityManager);

        Entity carEntity = new Entity("car");
        car = new CarComponent();
        carEntity.addComponent(car);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());
    }

    @AfterEach
    public void tearDown() {
        entityManager =null;
        vehicleSystem =null;
        car=null;
    }

    /**
     * Build a tank with 2 wheels.  Steering is differential drive.
     */
    @Test
    public void buildTank() {
        Entity[] wheelEntity = new Entity[2];
        WheelComponent[] wheels = new WheelComponent[wheelEntity.length];
        MotorComponent[] motors = new MotorComponent[wheelEntity.length];
        for (int i = 0; i < wheelEntity.length; ++i) {
            wheelEntity[i] = new Entity("wheel" + i);
            entityManager.addEntityToParent(wheelEntity[i], car.getEntity());
            car.addWheel(wheelEntity[i]);

            wheels[i] = new WheelComponent();
            wheelEntity[i].addComponent(wheels[i]);
            wheels[i].diameter.set(2.0);
            wheels[i].width.set(0.5);

            motors[i] = new MotorComponent();
            MotorSystemTest.setMotorTestCurve(motors[i]);
            wheelEntity[i].addComponent(motors[i]);
            wheels[i].drive.set(motors[i].getEntity());
        }

        // place wheels at the corners of the car
        wheelEntity[0].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, 0, 1));
        wheelEntity[1].getComponent(PoseComponent.class).setPosition(new Vector3d(10, 0, 1));
    }

    /**
     * test drive a tank.
     */
    @Test
    public void driveTank() {
        buildTank();

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnVelocity.set(20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        for(int i=0;i<30;++i) {
            vehicleSystem.update(1.0 / 30.0);
            System.out.println(car.getEntity().getComponent(PoseComponent.class).getPosition());
        }

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        //Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }

    /**
     * Build a car with 3 wheels.  Steering is omni-style (wheels turn outwards).
     */
    @Test
    public void buildOmni() {
        Entity[] wheelEntity = new Entity[3];
        WheelComponent[] wheels = new WheelComponent[3];
        MotorComponent[] motors = new MotorComponent[3];

        car.wheelType.set(CarComponent.WHEEL_OMNI);

        for (int i = 0; i < 3; ++i) {
            wheelEntity[i] = new Entity("wheel" + i);
            entityManager.addEntityToParent(wheelEntity[i], car.getEntity());
            car.addWheel(wheelEntity[i]);

            wheels[i] = new WheelComponent();
            wheelEntity[i].addComponent(wheels[i]);
            wheels[i].diameter.set(2.0);
            wheels[i].width.set(0.5);

            // add motors to all wheels
            motors[i] = new MotorComponent();
            MotorSystemTest.setMotorTestCurve(motors[i]);
            wheelEntity[i].addComponent(motors[i]);
            wheels[i].drive.set(motors[i].getEntity());

            // rotate wheels so they point outwards
            wheelEntity[i].getComponent(PoseComponent.class).setRotation(new Vector3d(0, 0, 120*i));
            // place wheels at the corners of the car
            wheelEntity[i].getComponent(PoseComponent.class).setPosition(new Vector3d(
                    10*Math.cos(Math.toRadians(120*i)),
                    10*Math.sin(Math.toRadians(120*i)),
                    1));
        }
    }

    /**
     * Test drive an omni-wheel car.
     */
    @Test
    public void driveOmni() {
        buildOmni();

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnVelocity.set(20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        for(int i=0;i<30;++i) {
            vehicleSystem.update(1.0 / 30.0);
            System.out.println(car.getEntity().getComponent(PoseComponent.class).getPosition());
        }

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        //Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }

    /**
     * Build a car with 4 wheels and front wheel steering.
     */
    public void buildCarWithNoMotor() {
        // add 4 wheels
        Entity[] wheelEntity = new Entity[4];
        Entity[] steerEntity = new Entity[4];
        WheelComponent[] wheels = new WheelComponent[wheelEntity.length];
        for (int i = 0; i < wheelEntity.length; ++i) {
            steerEntity[i] = new Entity("steer" + i);
            entityManager.addEntityToParent(steerEntity[i], car.getEntity());
            wheelEntity[i] = new Entity("wheel" + i);
            entityManager.addEntityToParent(wheelEntity[i], steerEntity[i]);

            wheels[i] = new WheelComponent();
            wheelEntity[i].addComponent(wheels[i]);
            wheels[i].diameter.set(2.0);
            wheels[i].width.set(0.5);
            car.addWheel(wheelEntity[i]);
        }

        // place wheels at the corners of the car
        wheelEntity[0].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, 10, 1));
        wheelEntity[1].getComponent(PoseComponent.class).setPosition(new Vector3d( 10, 10, 1));
        wheelEntity[2].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, -10, 1));
        wheelEntity[3].getComponent(PoseComponent.class).setPosition(new Vector3d( 10, -10, 1));
    }

    /**
     * test drive a front-wheel drive car.
     */
    @Test
    public void driveFWD() {
        buildCarWithNoMotor();

        // add one motor
        MotorComponent mc = new MotorComponent();
        MotorSystemTest.setMotorTestCurve(mc);

        Entity motor = new Entity("Motor");
        motor.addComponent(mc);
        entityManager.addEntityToParent(motor,car.getEntity());

        // add steering
        ServoComponent sc = new ServoComponent();
        MotorSystemTest.setMotorTestCurve(sc);

        Entity steer = new Entity("Steering");
        steer.addComponent(sc);
        entityManager.addEntityToParent(steer,car.getEntity());

        for (int i = 0; i < 2; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            // add front wheel steering
            wheelEntity.getComponent(WheelComponent.class).steer.set(steer);
            entityManager.addEntityToParent(wheelEntity,steer);
            // connect the motor to the front wheels
            wheelEntity.getComponent(WheelComponent.class).drive.set(motor);
        }

        Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(0));

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnVelocity.set(20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // turn the steering wheel and confirm wheel rotates
        motorSystem.rotateMotor(sc,15);
        double z = wheelEntity.getComponent(PoseComponent.class).getRotation().z;
        Assertions.assertEquals(15.0,z,0.001);

        // move a bit
        for(int i=0;i<30;++i) {
            motorSystem.update(1.0 / 30.0);
            vehicleSystem.update(1.0 / 30.0);
            System.out.println(car.getEntity().getComponent(PoseComponent.class).getPosition());
        }


        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        //Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }

    /**
     * test drive a front-wheel drive car.
     */
    @Test
    public void driveRWD() {
        buildCarWithNoMotor();

        // add one motor
        MotorComponent mc = new MotorComponent();
        MotorSystemTest.setMotorTestCurve(mc);

        Entity motor = new Entity("Motor");
        motor.addComponent(mc);
        entityManager.addEntityToParent(motor,car.getEntity());

        // add steering
        ServoComponent sc = new ServoComponent();
        MotorSystemTest.setMotorTestCurve(sc);

        Entity steer = new Entity("Steering");
        steer.addComponent(sc);
        entityManager.addEntityToParent(steer,car.getEntity());

        // add front wheel steering
        for (int i = 0; i < 2; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            entityManager.addEntityToParent(wheelEntity,steer);
            wheelEntity.getComponent(WheelComponent.class).steer.set(steer);
        }

        // connect the motor to the front wheels
        for (int i = 2; i < 4; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            wheelEntity.getComponent(WheelComponent.class).drive.set(motor);
        }

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnVelocity.set(20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        for(int i=0;i<30;++i) {
            vehicleSystem.update(1.0 / 30.0);
            System.out.println(car.getEntity().getComponent(PoseComponent.class).getPosition());
        }

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        //Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }

    /**
     * test drive a mecanum-wheel car.
     */
    @Test
    public void driveMecanum() {
        buildCarWithNoMotor();

        // add motors to all wheels
        for (int i = 0; i < 4; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            MotorComponent mc = new MotorComponent();
            MotorSystemTest.setMotorTestCurve(mc);
            wheelEntity.addComponent(mc);
            wheelEntity.getComponent(WheelComponent.class).drive.set(wheelEntity);
        }

        // change wheel type to mecanum
        car.wheelType.set(CarComponent.WHEEL_MECANUM);

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnVelocity.set(20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        for(int i=0;i<30;++i) {
            vehicleSystem.update(1.0 / 30.0);
            System.out.println(car.getEntity().getComponent(PoseComponent.class).getPosition());
        }

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        //Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }
}

