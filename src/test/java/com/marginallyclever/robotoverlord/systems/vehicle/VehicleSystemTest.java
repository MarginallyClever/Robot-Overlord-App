package com.marginallyclever.robotoverlord.systems.vehicle;

import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.vehicle.VehicleComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.motor.MotorSystem;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
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
    private final List<EntitySystem> systems = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        entityManager = new EntityManager();
        vehicleSystem = new VehicleSystem(entityManager);
        motorSystem = new MotorSystem(entityManager);
        systems.add(vehicleSystem);
        systems.add(motorSystem);
    }

    @AfterEach
    public void tearDown() {
        entityManager =null;
        vehicleSystem =null;
    }

    /**
     * test drive a tank.
     */
    @Test
    public void driveTank() {
        Entity carEntity = VehicleFactory.buildTank(entityManager);
        VehicleComponent car = carEntity.getComponent(VehicleComponent.class);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());
        testShared(car);
    }


    /**
     * Test drive an omni-wheel car.
     */
    @Test
    public void driveOmni() {
        Entity carEntity = VehicleFactory.buildOmni(entityManager);
        VehicleComponent car = carEntity.getComponent(VehicleComponent.class);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());
        testShared(car);
    }


    /**
     * test drive a front-wheel drive car.
     */
    @Test
    public void driveFWD() {
        Entity carEntity = VehicleFactory.buildFWD(entityManager);
        VehicleComponent car = carEntity.getComponent(VehicleComponent.class);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());

        // turn the steering wheel and confirm wheel rotates
        Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(0));
        WheelComponent wheel = wheelEntity.getComponent(WheelComponent.class);
        Entity steerEntity = entityManager.findEntityByUniqueID(wheel.steer.get());
        ServoComponent sc = steerEntity.getComponent(ServoComponent.class);
        motorSystem.rotateMotor(sc,15);
        double z = wheelEntity.getComponent(PoseComponent.class).getRotation().z;
        Assertions.assertEquals(15.0,z,0.001);

        testShared(car);
    }

    /**
     * test drive a front-wheel drive car.
     */
    @Test
    public void driveRWD() {
        Entity carEntity = VehicleFactory.buildRWD(entityManager);
        VehicleComponent car = carEntity.getComponent(VehicleComponent.class);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());
        testShared(car);
    }

    /**
     * test drive a mecanum-wheel car.
     */
    @Test
    public void driveMecanum() {
        Entity carEntity = VehicleFactory.buildMecanum(entityManager);
        VehicleComponent car = carEntity.getComponent(VehicleComponent.class);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());
        testShared(car);
    }

    private void testShared(VehicleComponent car) {
        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnVelocity.set(20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // move a bit
        double dt=1.0/30.0;
        for(int i=0;i<30;++i) {
            for(EntitySystem es : systems) {
                es.update(dt);
            }
            System.out.println(car.getEntity().getComponent(PoseComponent.class).getPosition());
        }

        // TODO check position of car
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().x);
        Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().y);
        //Assertions.assertNotEquals(0.0,car.getEntity().getComponent(PoseComponent.class).getPosition().z);
    }
}

