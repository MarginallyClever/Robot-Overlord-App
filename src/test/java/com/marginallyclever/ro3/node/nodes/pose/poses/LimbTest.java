package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.actions.Import;
import com.marginallyclever.ro3.apps.actions.LoadScene;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class LimbTest {
    static Limb limb;

    private Limb build6AxisArm() {
        var load = new Import();
        File file = new File(Objects.requireNonNull(this.getClass().getResource("/com/marginallyclever/ro3/apps/node/nodes/marlinrobotarm/Sixi3-5.RO")).getFile());
        load.commitImport(file);
        return (Limb) Registry.getScene().findByPath("./Sixi3");
    }

    @BeforeEach
    public void setUp() {
        Registry.start();
        limb = build6AxisArm();
    }

    @Test
    public void testGetEndEffector() {
        assertNotNull(limb.getEndEffector());
    }

    @Test
    public void testGetMotors() {
        var list = limb.getMotors();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        int i=0;
        for(var m : list) {
            assertNotNull(m);
            assertEquals(limb.getJoint(i++),m.getSubject());
        }
    }

    @Test
    public void testGetNumJoints() {
        assertEquals(5,limb.getNumJoints());
    }

    @Test
    public void testGetSetAllJointAngles() {
        var list = limb.getAllJointAngles();
        assertNotNull(list);
        assertEquals(5,list.length);

        var list2 = new double[]{0,1,2,100,4};
        limb.setAllJointAngles(list2);
        for(int i=0;i<list2.length;++i) {
            assertEquals(list2[i],limb.getJoint(i).getHinge().getAngle(),"joint "+i+" angle");
        }
    }

    @Test
    public void testGetSetAllJointVelocities() {
        var list2 = new double[]{0,1,2,3,4};
        limb.setAllJointVelocities(list2);
        for(int i=0;i<list2.length;++i) {
            assertEquals(list2[i],limb.getJoint(i).getHinge().getVelocity(),"joint "+i+" angle");
        }
    }

    @Test
    public void toFromJSON() {
        Registry.start();

        var limb = new Limb("a");
        var target = new Pose("b");

        limb.setLinearVelocity(limb.getLinearVelocity()+10);
        limb.setGoalMarginOfError(limb.getGoalMarginOfError()+10);
        limb.setIsAtGoal(!limb.getIsAtGoal());
        limb.addChild(target);
        limb.setTarget(target);

        var json = limb.toJSON();
        var limb2 = new Limb();
        limb2.fromJSON(json);

        assertEquals(limb.getLinearVelocity(),limb2.getLinearVelocity());
        assertEquals(limb.getGoalMarginOfError(),limb2.getGoalMarginOfError());
        assertEquals(limb.getIsAtGoal(),limb2.getIsAtGoal());
        assertEquals(limb.getTarget().getSubject().getName(),limb2.getTarget().getSubject().getName());
        assertEquals(limb.getNumJoints(),limb2.getNumJoints());
        for(int i=0;i<limb.getNumJoints();++i) {
            assertEquals(limb.getJoint(i).getHinge().getAngle(),limb2.getJoint(i).getHinge().getAngle());
        }
    }
    
    @Test
    void testGetSetTarget() {
        Limb limb = new Limb();
        Pose pose = new Pose();
        limb.addChild(pose);
        limb.setTarget(pose);
        assertEquals(pose, limb.getTarget().getSubject());
    }

    @Test
    void testUpdate() {
        Limb limb = new Limb();
        limb.update(1.0);
        // Add assertions based on the expected state of the limb after calling update
    }

    @Test
    void testGetSetLinearVelocity() {
        Limb limb = new Limb();
        limb.setLinearVelocity(10.0);
        assertEquals(10.0, limb.getLinearVelocity());
    }

    @Test
    void testGetDistanceToTarget() {
        Limb limb = new Limb();
        // Add assertions based on the expected distance to target
    }

    @Test
    void testGetSetGoalMarginOfError() {
        Limb limb = new Limb();
        limb.setGoalMarginOfError(0.1);
        assertEquals(0.1, limb.getGoalMarginOfError());
    }
}