package com.marginallyclever.ro3.node.nodes;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MotorTest {
    @Test
    void testUpdate() {
        Motor motor = new Motor();
        motor.update(1.0);
        // Add assertions based on the expected state of the motor after calling update
    }

    @Test
    void testGetSetHinge() {
        Motor motor = new Motor();
        HingeJoint hingeJoint = new HingeJoint();
        motor.addChild(hingeJoint);
        motor.setHinge(hingeJoint);
        assertEquals(hingeJoint, motor.getHinge());
    }

    @Test
    void testHasHinge() {
        Motor motor = new Motor();
        assertFalse(motor.hasHinge());
        HingeJoint hingeJoint = new HingeJoint();
        motor.addChild(hingeJoint);
        motor.setHinge(hingeJoint);
        assertTrue(motor.hasHinge());
    }
}