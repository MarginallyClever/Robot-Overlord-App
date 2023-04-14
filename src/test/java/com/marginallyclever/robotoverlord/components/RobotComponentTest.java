package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.robot.RobotComponent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

public class RobotComponentTest {
    private RobotComponent build3AxisArm() {
        Entity base = new Entity("Base");
        RobotComponent robot = new RobotComponent();
        base.addComponent(robot);
        List<Entity> joints = new ArrayList<>();
        List<DHComponent> dh = new ArrayList<>();
        Entity prev = base;
        for(int i=0;i<3;++i) {
            Entity e = new Entity("J"+i);
            joints.add(e);
            prev.addEntity(e);
            prev = e;
            DHComponent dhc = new DHComponent();
            dh.add(dhc);
            e.addComponent(dhc);
        }
        dh.get(0).set(0,0,90,0,60,-60);
        dh.get(1).set(0,10,0,45,170,-170);
        dh.get(2).set(0,10,0,-90,170,-170);
        joints.get(2).addComponent(new ArmEndEffectorComponent());
        robot.findBones();
        return robot;
    }

    private void testEndEffectorAtPosition(RobotComponent robot, Point3d expected) {
        Point3d actual = (Point3d)robot.get(RobotComponent.END_EFFECTOR_TARGET_POSITION);
        Assertions.assertTrue(actual.distance(expected)<0.0001,"expected="+expected+", actual="+actual);
    }

    /**
     * Build a simple 3 axis arm and check the end effector is at the expected position.
     */
    @Test
    public void test3AxisArmAtPosition() {
        RobotComponent robot = build3AxisArm();
        testEndEffectorAtPosition(robot,new Point3d(14.14214,0,0));
    }

    /**
     * Build a simple 3 axis arm and check the end effector is at the expected position.
     * Move the end effector before checking the new position.
     */
    @Test
    public void test3AxisArmAtPosition2() {
        RobotComponent robot = build3AxisArm();
        // a few iterations are needed to refine the movement.
        for(int i=0;i<4;++i) {
            robot.set(RobotComponent.END_EFFECTOR_TARGET_POSITION, new Point3d(15.14214, 0, 0));
        }
        testEndEffectorAtPosition(robot,new Point3d(15.14214,0,0));
    }

    /**
     * Build a simple 3 axis arm and check the end effector is at the expected position.
     * This time also move the arm away from the origin.
     */
    @Test
    public void test3AxisArmAtPosition3() {
        RobotComponent robot = build3AxisArm();
        Matrix4d pose = MatrixHelper.createIdentityMatrix4();
        pose.setTranslation(new Vector3d(10,0,0));
        robot.set(RobotComponent.POSE,pose);
        testEndEffectorAtPosition(robot,new Point3d(14.14214,0,0));
    }

    /**
     * Build a simple 3 axis arm and check the end effector is at the expected position.
     * This time also move the arm away from the origin.
     */
    @Test
    public void test3AxisArmAtPosition4() {
        RobotComponent robot = build3AxisArm();
        Matrix4d pose = (Matrix4d)robot.get(RobotComponent.POSE);
        robot.set(RobotComponent.POSE,pose);
        // a few iterations are needed to refine the movement.
        for(int i=0;i<4;++i) {
            robot.set(RobotComponent.END_EFFECTOR_TARGET_POSITION, new Point3d(15.14214, 0, 0));
        }
        testEndEffectorAtPosition(robot,new Point3d(15.14214,0,0));
    }
}
