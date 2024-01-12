package com.marginallyclever.ro3.node.nodes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HingeJointTest {
    @Test
    void testGetSetAngle() {
        HingeJoint hingeJoint = new HingeJoint();
        hingeJoint.setAngle(45.0);
        assertEquals(45.0, hingeJoint.getAngle());
    }

    @Test
    void testGetMinAngle() {
        HingeJoint hingeJoint = new HingeJoint();
        assertEquals(0.0, hingeJoint.getMinAngle());
    }

    @Test
    void testGetMaxAngle() {
        HingeJoint hingeJoint = new HingeJoint();
        assertEquals(360.0, hingeJoint.getMaxAngle());
    }

    @Test
    void testGetSetVelocity() {
        HingeJoint hingeJoint = new HingeJoint();
        hingeJoint.setVelocity(10.0);
        assertEquals(10.0, hingeJoint.getVelocity());
    }

    @Test
    void testGetSetAcceleration() {
        HingeJoint hingeJoint = new HingeJoint();
        hingeJoint.setAcceleration(20.0);
        assertEquals(20.0, hingeJoint.getAcceleration());
    }

    @Test
    void testGetAxle() {
        HingeJoint hingeJoint = new HingeJoint();
        assertNull(hingeJoint.getAxle());
    }
}