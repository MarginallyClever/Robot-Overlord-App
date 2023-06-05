package com.marginallyclever.robotoverlord.systems;

import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.vehicle.CarComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import org.junit.jupiter.api.*;

import javax.vecmath.Vector3d;

public class VehicleSystemTest {
    private EntityManager em;
    private VehicleSystem vs;
    private CarComponent car;

    @BeforeEach
    public void setUp() {
        em = new EntityManager();
        vs = new VehicleSystem(em);

        Entity carEntity = new Entity("car");
        car = new CarComponent();
        carEntity.addComponent(car);
        em.addEntityToParent(carEntity, em.getRoot());
    }

    @AfterEach
    public void tearDown() {
        em=null;
        vs=null;
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
            em.addEntityToParent(wheelEntity[i], car.getEntity());
            car.addWheel(wheelEntity[i]);

            wheels[i] = new WheelComponent();
            wheelEntity[i].addComponent(wheels[i]);
            wheels[i].diameter.set(2.0);
            wheels[i].width.set(0.5);

            motors[i] = new MotorComponent();
            wheelEntity[i].addComponent(motors[i]);
        }

        // wheels should spin around their local z axis
        wheelEntity[0].getComponent(PoseComponent.class).setRotation(new Vector3d(0, -90, 0));
        wheelEntity[1].getComponent(PoseComponent.class).setRotation(new Vector3d(0, 90, 0));
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
        car.turnRadius.set(-20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        vs.update(1.0/30.0);

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }

    /**
     * Build a car with 3 wheels.  Steering is omni-style (wheels turn outwards).
     */
    @Test
    public void build3WheelOmniCar() {
        Entity[] wheelEntity = new Entity[3];
        WheelComponent[] wheels = new WheelComponent[3];
        MotorComponent[] motors = new MotorComponent[3];
        for (int i = 0; i < 3; ++i) {
            wheelEntity[i] = new Entity("wheel" + i);
            em.addEntityToParent(wheelEntity[i], car.getEntity());
            car.addWheel(wheelEntity[i]);

            wheels[i] = new WheelComponent();
            wheels[i].type.set(WheelComponent.TYPE_OMNI);
            wheelEntity[i].addComponent(wheels[i]);
            wheels[i].diameter.set(2.0);
            wheels[i].width.set(0.5);

            motors[i] = new MotorComponent();
            wheelEntity[i].addComponent(motors[i]);

            // rotate wheels so they point outwards
            wheelEntity[i].getComponent(PoseComponent.class).setRotation(new Vector3d(0, 90, 120*i));
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
    public void driveOmniCar() {
        build3WheelOmniCar();

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnRadius.set(-20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        vs.update(1.0/30.0);

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }

    /**
     * Build a car with 4 wheels
     */
    @Test
    public void buildCar() {
        Entity[] wheelEntity = new Entity[4];
        WheelComponent[] wheels = new WheelComponent[wheelEntity.length];
        for (int i = 0; i < wheelEntity.length; ++i) {
            wheelEntity[i] = new Entity("wheel" + i);
            em.addEntityToParent(wheelEntity[i], car.getEntity());
            car.addWheel(wheelEntity[i]);

            wheels[i] = new WheelComponent();
            wheelEntity[i].addComponent(wheels[i]);
            wheels[i].diameter.set(2.0);
            wheels[i].width.set(0.5);
        }

        // wheels should spin around their local z axis
        wheelEntity[0].getComponent(PoseComponent.class).setRotation(new Vector3d(0, -90, 0));
        wheelEntity[1].getComponent(PoseComponent.class).setRotation(new Vector3d(0, -90, 0));
        wheelEntity[2].getComponent(PoseComponent.class).setRotation(new Vector3d(0, 90, 0));
        wheelEntity[3].getComponent(PoseComponent.class).setRotation(new Vector3d(0, 90, 0));
        // place wheels at the corners of the car
        wheelEntity[0].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, 10, 1));
        wheelEntity[1].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, -10, 1));
        wheelEntity[2].getComponent(PoseComponent.class).setPosition(new Vector3d(10, 10, 1));
        wheelEntity[3].getComponent(PoseComponent.class).setPosition(new Vector3d(10, -10, 1));
        // TODO add steering to front wheels
    }

    /**
     * Build a car with front-wheel drive and steering.
     */
    @Test
    public void buildFWD() {
        buildCar();
        // add motors to front wheels
        for (int i = 0; i < 2; ++i) {
            Entity wheelEntity = em.findEntityByUniqueID(car.getWheel(i));
            wheelEntity.addComponent(new MotorComponent());
        }

    }

    /**
     * test drive a front-wheel drive car.
     */
    @Test
    public void driveFWD() {
        buildFWD();

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnRadius.set(-20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        vs.update(1.0/30.0);

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }

    /**
     * Build a car with front-wheel drive and steering.
     */
    @Test
    public void buildRWD() {
        buildCar();
        // add motors to the rear wheels
        for (int i = 2; i < 4; ++i) {
            Entity wheelEntity = em.findEntityByUniqueID(car.getWheel(i));
            wheelEntity.addComponent(new MotorComponent());
        }
    }

    /**
     * test drive a front-wheel drive car.
     */
    @Test
    public void driveRWD() {
        buildRWD();

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnRadius.set(-20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        vs.update(1.0/30.0);

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }

    /**
     * test drive a mecanum-wheel car.
     */
    @Test
    public void driveMecanumCar() {
        buildCar();

        // change wheel type to mecanum
        for (int i = 0; i < 4; ++i) {
            WheelComponent wheel = em.findEntityByUniqueID(car.getWheel(i)).getComponent(WheelComponent.class);
            wheel.type.set(WheelComponent.TYPE_MECANUM);
        }

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnRadius.set(-20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        vs.update(1.0/30.0);

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }
}

