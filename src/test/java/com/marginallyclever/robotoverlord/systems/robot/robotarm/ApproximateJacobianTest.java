package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.robotoverlord.components.ArmEndEffectorComponent;
import com.marginallyclever.robotoverlord.components.DHComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.robots.Robot;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checking if Approximate jacobians are commutative.  This test is ignored because they are not.
 *
 * @since 2.6.1
 * @author Dan Royer
 */
@Disabled
public class ApproximateJacobianTest {
    private EntityManager entityManager;

    @BeforeEach
    public void setup() {
        entityManager = new EntityManager();
    }

    @AfterEach
    public void teardown() {
        entityManager.clear();
        entityManager = null;
    }

    private RobotComponent build5AxisArm() {
        Entity base = new Entity("Sixi3-5");
        RobotComponent robot = new RobotComponent();
        base.addComponent(robot);

        // add target
        Entity target = new Entity(RobotComponent.TARGET_NAME);
        entityManager.addEntityToParent(target, base);

        // position arm
        List<Entity> joints = new ArrayList<>();
        List<DHComponent> dh = new ArrayList<>();
        Entity prev = base;
        int numJoints=5;
        for(int i=0;i<numJoints;++i) {
            Entity e = new Entity("J"+i);
            joints.add(e);
            entityManager.addEntityToParent(e,prev);
            prev = e;
            DHComponent dhc = new DHComponent();
            dh.add(dhc);
            e.addComponent(dhc);
        }

        dh.get(0).set( 8.020,     0,270,   0,170,-170,true);  dh.get(0).setJointHome(  0);
        dh.get(1).set( 9.131,17.889,  0, 270,370, 170,true);  dh.get(1).setJointHome(270);
        dh.get(2).set(     0,12.435,  0,   0,150,-150,true);  dh.get(2).setJointHome(  0);
        dh.get(3).set(     0,     0,270, 270,440, 100,true);  dh.get(3).setJointHome(270);
        dh.get(4).set(  5.12,     0,  0,   0,360,   0,true);  dh.get(4).setJointHome( 90);

        joints.get(4).addComponent(new ArmEndEffectorComponent());
        robot.findBones();
        robot.set(Robot.END_EFFECTOR_TARGET,robot.get(Robot.END_EFFECTOR));

        return robot;
    }

    private RobotComponent build6AxisArm() {
        Entity base = new Entity("Sixi3-6");
        RobotComponent robot = new RobotComponent();
        base.addComponent(robot);

        // add target
        Entity target = new Entity(RobotComponent.TARGET_NAME);
        entityManager.addEntityToParent(target, base);

        // position arm
        List<Entity> joints = new ArrayList<>();
        List<DHComponent> dh = new ArrayList<>();
        Entity prev = base;
        int numJoints=6;
        for(int i=0;i<numJoints;++i) {
            Entity e = new Entity("J"+i);
            joints.add(e);
            entityManager.addEntityToParent(e,prev);
            prev = e;
            DHComponent dhc = new DHComponent();
            dh.add(dhc);
            e.addComponent(dhc);
        }

        dh.get(0).set( 8.020,     0,270,   0,170,-170,true);  dh.get(0).setJointHome(  0);
        dh.get(1).set( 9.131,17.889,  0, 270,370, 170,true);  dh.get(1).setJointHome(270);
        dh.get(2).set(     0,12.435,  0,   0,150,-150,true);  dh.get(2).setJointHome(  0);
        dh.get(3).set(     0,     0,270, 270,440, 100,true);  dh.get(3).setJointHome(270);
        dh.get(4).set(15.616,     0, 90,  90,270, -90,true);  dh.get(4).setJointHome( 90);
        dh.get(5).set( 5.150,     0,  0, 180,360,   0,true);  dh.get(5).setJointHome(180);

        joints.get(5).addComponent(new ArmEndEffectorComponent());
        robot.findBones();
        robot.set(Robot.END_EFFECTOR_TARGET,robot.get(Robot.END_EFFECTOR));

        return robot;
    }

    /**
     * Compare the two methods.
     * @throws Exception if error
     */
    @Test
    @Disabled // TODO fix me!
    public void compare() throws Exception {
        RobotComponent robot = build6AxisArm();
        ApproximateJacobian finite = new ApproximateJacobianFiniteDifferences(robot);
        ApproximateJacobian screw = new ApproximateJacobianScrewTheory(robot);

        System.out.println("Finite "+finite);
        System.out.println("Screw "+screw);
        double [][] finiteJacobian = finite.getJacobian();
        double [][] screwJacobian = screw.getJacobian();
        for(int i=0;i<finiteJacobian.length;++i) {
            for(int j=0;j<finiteJacobian[0].length;++j) {
                Assertions.assertEquals(finiteJacobian[i][j],screwJacobian[i][j],0.01);
            }
        }

        double [] v = new double[] {1,2,3,4,5,6};
        double [] vFinite = finite.getJointForceFromCartesianForce(v);
        double [] vScrew = screw.getJointForceFromCartesianForce(v);
        System.out.println(Arrays.toString(vFinite));
        System.out.println(Arrays.toString(vScrew));
        for(int i=0;i<vFinite.length;++i) {
            Assertions.assertEquals(vFinite[i],vScrew[i],0.01);
        }
    }
}
