package com.marginallyclever.ro3.node.nodes.marlinrobot.marlinrobotarm;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.marlinrobot.MarlinListener;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarlinRobotArmTest {
    @Test
    void testGetTarget() {
        Limb limb = new Limb();
        MarlinRobotArm marlinRobotArm = new MarlinRobotArm();
        Pose target = new Pose();
        limb.addChild(marlinRobotArm);
        limb.addChild(target);
        limb.setTarget(target);
        marlinRobotArm.setLimb(limb);
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
        limb.addChild(marlinRobotArm);
        marlinRobotArm.setLimb(limb);
        assertEquals(limb, marlinRobotArm.getLimb().getSubject());
    }

    @Test
    public void toFromJSON() {
        Registry.start();

        var limb = new Limb("a");

        MarlinRobotArm mra = new MarlinRobotArm();
        mra.addChild(limb);
        mra.setLimb(limb);

        var json = mra.toJSON();
        MarlinRobotArm mra2 = new MarlinRobotArm();
        mra2.fromJSON(json);

        assertEquals(mra.getLimb().getSubject().getName(),mra2.getLimb().getSubject().getName());
    }

    @Test
    public void numberFormatDE() {
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
        try {
            assertEquals("1.234", StringHelper.formatDouble(1.234));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void numberFormatDE2() {
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(1.234);
            assertEquals("1.234", sb.toString());
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}