package com.marginallyclever.robotOverlord;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.vecmath.Matrix4d;

import org.junit.Test;

import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3FK;

public class Sixi3FKTests {
	/**
	 * Set random positions for the the robot, save, load, and compare that they match.
	 * Repeat many times.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		Sixi3FK a = new Sixi3FK();

		Matrix4d m1 = new Matrix4d();
		Matrix4d m2 = new Matrix4d();
		Matrix4d m3 = new Matrix4d();
		Matrix4d am = new Matrix4d();
		Matrix4d bm = new Matrix4d();
		
		double [] av = new double[Sixi3FK.NUM_BONES];
		double [] bv = new double[Sixi3FK.NUM_BONES];
		
		for(int j=0;j<200;++j) {
			// old pose
			for(int i=0;i<Sixi3FK.NUM_BONES;++i) {
				av[i] = 10 + Math.random()*340;
			}
			
			// rotation
			m1.rotX(Math.random() * Math.PI*2);
			m2.rotZ(Math.random() * Math.PI*2);
			m3.mul(m1,m2);
			// translation
			m3.m03=Math.random()*100;
			m3.m13=Math.random()*100;
			m3.m23=Math.random()*100;
			a.setPose(m3);

			a.setFKValues(av);
			a.getEndEffector(am);
			
			File tempFile = File.createTempFile("test","txt",new File(System.getProperty("user.dir")));
			
			FileOutputStream fileOut = new FileOutputStream(tempFile);
			ObjectOutputStream objOut = new ObjectOutputStream (fileOut);
			objOut.writeObject(a);
			objOut.flush();
			objOut.close();
			
			FileInputStream fileIn = new FileInputStream(tempFile);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			Sixi3FK b = (Sixi3FK)objIn.readObject();
			objIn.close();
			
			assertTrue(b.getName().contentEquals(a.getName()));
			assertTrue(b.getPose().equals(a.getPose()));
			
			b.getFKValues(bv);
			for(int i=0;i<Sixi3FK.NUM_BONES;++i) {
				assertTrue(av[i]==bv[i]);
			}
			
			b.getEndEffector(bm);
			assertTrue(am.equals(bm));
			
			tempFile.deleteOnExit();
		}
	}
}
