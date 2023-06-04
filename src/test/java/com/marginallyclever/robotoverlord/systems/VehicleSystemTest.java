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
    }

    @Test
    public void build4WheelCar() {
        Entity[] wheelEntity = new Entity[4];
        WheelComponent[] wheels = new WheelComponent[4];
        MotorComponent[] motors = new MotorComponent[4];
        for (int i = 0; i < 4; ++i) {
            wheelEntity[i] = new Entity("wheel" + i);
            em.addEntityToParent(wheelEntity[i], car.getEntity());
            car.addWheel(wheelEntity[i]);

            wheels[i] = new WheelComponent();
            wheelEntity[i].addComponent(wheels[i]);
            wheels[i].diameter.set(2.0);
            wheels[i].width.set(0.5);

            motors[i] = wheelEntity[i].getComponent(MotorComponent.class);
            Assertions.assertNotNull(motors[i]);
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
    }

    @Test
    public void drive4WheelCar() {
        build4WheelCar();

        // TODO set velocity to drive forward
        // TODO set the turn rate to turn left
        // TODO set the strafe rate to strafe right (should do nothing in this car)

        // move a bit
        vs.update(1.0/30.0);

        // TODO check position of car
    }

    @Test
    public void drive4WheelMecanumCar() {
        build4WheelCar();

        for (int i = 0; i < 4; ++i) {
            WheelComponent wheel = em.findEntityByUniqueID(car.getWheel(i)).getComponent(WheelComponent.class);
            wheel.type.set(WheelComponent.TYPE_MECANUM);
        }

        // TODO set velocity to drive forward
        // TODO set the turn rate to turn left
        // TODO set the strafe rate to strafe right

        // move a bit
        vs.update(1.0/30.0);

        // TODO check position of car
    }

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
            wheelEntity[i].addComponent(wheels[i]);
            wheels[i].diameter.set(2.0);
            wheels[i].width.set(0.5);

            motors[i] = wheelEntity[i].getComponent(MotorComponent.class);
            Assertions.assertNotNull(motors[i]);

            // rotate wheels so they point outwards
            wheelEntity[i].getComponent(PoseComponent.class).setRotation(new Vector3d(0, 90, 120*i));
            // place wheels at the corners of the car
            wheelEntity[i].getComponent(PoseComponent.class).setPosition(new Vector3d(
                    10*Math.cos(Math.toRadians(120*i)),
                    10*Math.sin(Math.toRadians(120*i)),
                    1));
        }
    }
}

