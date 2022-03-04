package com.marginallyclever.robotOverlord;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmFK;
import com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi3_5axis;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import javax.vecmath.Matrix4d;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RobotArmFKTests {
	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	
    /**
     * Set random positions for the the robot, save, load, and compare that they match.
     * Repeat many times.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        RobotArmFK a = new RobotArmFK();
        int numBones = a.getNumBones();

        Matrix4d m1 = new Matrix4d();
        Matrix4d m2 = new Matrix4d();
        Matrix4d m3 = new Matrix4d();
        Matrix4d am, bm;

        double[] av = new double[numBones];

        for (int j = 0; j < 200; ++j) {
            // old pose
            for (int i = 0; i < numBones; ++i) {
                av[i] = 10 + Math.random() * 340;
            }

            // rotation
            m1.rotX(Math.random() * Math.PI * 2);
            m2.rotZ(Math.random() * Math.PI * 2);
            m3.mul(m1, m2);
            // translation
            m3.m03 = Math.random() * 100;
            m3.m13 = Math.random() * 100;
            m3.m23 = Math.random() * 100;
            a.setPose(m3);

            a.setAngles(av);
            am = a.getToolCenterPoint();

            File tempFile = File.createTempFile("test", "txt", new File(System.getProperty("user.dir")));

            FileOutputStream fileOut = new FileOutputStream(tempFile);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(a);
            objOut.flush();
            objOut.close();

            FileInputStream fileIn = new FileInputStream(tempFile);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            RobotArmFK b = (RobotArmFK) objIn.readObject();
            objIn.close();

            assertTrue(b.getName().contentEquals(a.getName()));
            assertTrue(b.getPose().equals(a.getPose()));

            double[] bv = b.getAngles();
            for (int i = 0; i < numBones; ++i) {
                assertTrue(av[i] == bv[i]);
            }

            bm = b.getToolCenterPoint();
            assertTrue(am.equals(bm));

            tempFile.deleteOnExit();
        }
    }

    @Test
    public void testInverseDynamics() {
        Sixi3_5axis robot = new Sixi3_5axis();
        double[] t = robot.getTorques();
        Log.message("Torque=" + t);
    }
}
