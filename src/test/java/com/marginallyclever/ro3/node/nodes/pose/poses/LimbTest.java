package com.marginallyclever.ro3.node.nodes.pose.poses;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.actions.LoadScene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class LimbTest {
    static Limb limb;

    private static Limb build6AxisArm() {
        var load = new LoadScene(null,null);
        File file = new File("src/test/resources/com/marginallyclever/ro3/apps/node/nodes/marlinrobotarm/Sixi3-5.RO");
        load.commitLoad(file);
        return (Limb) Registry.getScene().findByPath("./Sixi3");
    }

    @BeforeAll
    public static void setUp() {
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
        var json = limb.toJSON();
        var limb2 = new Limb();
        limb2.fromJSON(json);
        assertEquals(limb.getNumJoints(),limb2.getNumJoints());
        for(int i=0;i<limb.getNumJoints();++i) {
            assertEquals(limb.getJoint(i).getHinge().getAngle(),limb2.getJoint(i).getHinge().getAngle());
        }
    }
}