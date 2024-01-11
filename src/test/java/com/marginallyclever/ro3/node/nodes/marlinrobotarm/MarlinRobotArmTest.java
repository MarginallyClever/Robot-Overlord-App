package com.marginallyclever.ro3.node.nodes.marlinrobotarm;

import com.marginallyclever.ro3.node.nodes.Pose;
import com.marginallyclever.ro3.node.nodes.pose.Limb;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class MarlinRobotArmTest {
    @Test
    void testGetTarget() {
        Limb limb = new Limb();
        LimbSolver limbSolver = new LimbSolver();
        MarlinRobotArm marlinRobotArm = new MarlinRobotArm();
        Pose target = new Pose();
        limb.addChild(marlinRobotArm);
        limb.addChild(limbSolver);
        limb.addChild(target);
        limbSolver.setTarget(target);
        marlinRobotArm.setSolver(limbSolver);
        assertEquals(target, marlinRobotArm.getTarget());
    }

    @Test
    void testSendGCode() {
        MarlinRobotArm marlinRobotArm = new MarlinRobotArm();
        MarlinListener mockListener = Mockito.mock(MarlinListener.class);
        marlinRobotArm.addMarlinListener(mockListener);
        marlinRobotArm.sendGCode("M114");

        Mockito.verify(mockListener).messageFromMarlin(Mockito.startsWith("Ok: M114"));

    }

    @Test
    void testSetLimbAndSolver() {
        Limb limb = new Limb();
        MarlinRobotArm marlinRobotArm = new MarlinRobotArm();
        LimbSolver limbSolver = new LimbSolver();
        limb.addChild(marlinRobotArm);
        limb.addChild(limbSolver);
        marlinRobotArm.setLimb(limb);
        marlinRobotArm.setSolver(limbSolver);
        assertEquals(limb, marlinRobotArm.getLimb());
        assertEquals(limbSolver, marlinRobotArm.getSolver());
    }
}