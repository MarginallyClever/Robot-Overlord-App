package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.log.Log;

import com.marginallyclever.robotoverlord.components.RobotComponent;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix4d;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Deprecated
public class RobotArmFKTests {
    /**
     * Set random positions for the robot, save, load, and compare that they match.
     * Repeat many times.
     */
    //@Test
    public void testSerialization() {
    }

    @Test
    public void testInverseDynamics() {
        RobotComponent robot = new RobotComponent();
        //double[] t = robot.getTorques();
        //System.out.println("Torque=" + t);
    }
}
