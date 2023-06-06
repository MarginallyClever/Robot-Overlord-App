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
import com.marginallyclever.robotoverlord.systems.vehicle.VehicleFactory;
import com.marginallyclever.robotoverlord.systems.vehicle.VehicleSystem;
import org.junit.jupiter.api.*;

import javax.swing.event.CaretEvent;
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

    @BeforeEach
    public void setUp() {
        entityManager = new EntityManager();
        vehicleSystem = new VehicleSystem(entityManager);
        motorSystem = new MotorSystem(entityManager);
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
        CarComponent car = carEntity.getComponent(CarComponent.class);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());

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
     * Test drive an omni-wheel car.
     */
    @Test
    public void driveOmni() {
        Entity carEntity = VehicleFactory.buildOmni(entityManager);
        CarComponent car = carEntity.getComponent(CarComponent.class);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());

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
     * test drive a front-wheel drive car.
     */
    @Test
    public void driveFWD() {
        Entity carEntity = VehicleFactory.buildFWD(entityManager);
        CarComponent car = carEntity.getComponent(CarComponent.class);

        // drive forward
        car.forwardVelocity.set(10.0);
        // set the turn rate to turn left
        car.turnVelocity.set(20.0);
        // set the strafe rate to strafe right
        car.strafeVelocity.set(10.0);

        // turn the steering wheel and confirm wheel rotates
        Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(0));
        WheelComponent wheel = wheelEntity.getComponent(WheelComponent.class);
        Entity steerEntity = entityManager.findEntityByUniqueID(wheel.steer.get());
        ServoComponent sc = steerEntity.getComponent(ServoComponent.class);
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
        Entity carEntity = VehicleFactory.buildRWD(entityManager);
        CarComponent car = carEntity.getComponent(CarComponent.class);

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
        Entity carEntity = VehicleFactory.buildMecanum(entityManager);
        CarComponent car = carEntity.getComponent(CarComponent.class);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());

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

