package com.marginallyclever.robotOverlord;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.vecmath.Matrix4d;

import org.junit.Test;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.Sixi2;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.Sixi2Model;

public class Sixi2Tester {
	/**
	 * Test that IK(FK(A))==A for many random fk pose A. 
	 */
	@Test
	public void TestIK() {
		System.out.println("TestIK() start");
		Sixi2Model model = new Sixi2Model();
		int numLinks = model.getNumLinks();
		PoseFK key0 = model.createPoseFK();
		PoseFK key1 = model.createPoseFK();
		
		final int TOTAL_TESTS = 50;
		int testsOK=0;
		int testsNoMatch=0;
		int testsNoIK=0;
		
		for( int j = 0; j < TOTAL_TESTS; ++j ) {
			// find a random pose for the whole arm
			System.out.print(j + ": ");
			
			for( int i = 0; i < numLinks; ++i ) {
				DHLink link = model.getLink(i);
				// find a random pose for this bone.
				double top = link.getRangeMax();
				double bot = link.getRangeMin();
				double range = top-bot;
				double v = range * Math.random() + bot;
				key0.fkValues[i]=v;
				System.out.print(StringHelper.formatDouble(v)+"\t");
			}
			
			// set the pose
			model.setPoseFK(key0);
			// get the end effector world pose for this key
			Matrix4d ee = model.getPoseIK();
			// use the end effector world pose to solve IK
			if(model.setPoseIK(ee)) {
				key1.set(model.getPoseFK());
				if(key1.equals(key0)) {
					testsOK++;
					System.out.println(" OK");
				} else {
					testsNoMatch++;
					System.out.println(" NO MATCH");
				}
			} else {
				testsNoIK++;
				System.out.println(" NO IK");
			}
		}
		System.out.println("Finished! "+testsOK+" OK, "+testsNoMatch+" no match, "+testsNoIK+" no IK.");
		System.out.println("TestIK() end");
	}
	

	/**
	 * Test that approximate jacobian at the home position does not generate any NaN values. 
	 */
	@Test
	public void TestApproximateJacobian() {
		System.out.println("TestApproximateJacobian() start");
		Sixi2Model robot = new Sixi2Model();
		//robot.goHome();
		PoseFK keyframe = robot.getPoseFK();
		
		//was double [][] aj = robot.approximateJacobian(keyframe);
		double [][] aj = robot.exactJacobian(keyframe);
		
		for( int y=0;y<aj.length;++y ) {
			for( int x=0;x<aj[y].length;++x ) {
				assert(!Double.isNaN(aj[y][x]));
			}
		}
		System.out.println("TestApproximateJacobian() end");
	}


	/**
	 * Compare approximate and exact Jacobian matrixes 
	 */
	@Test
	public void CompareJacobians() {
		System.out.println("CompareJacobians() start");
		Sixi2Model robot = new Sixi2Model();
		//robot.goHome();
		PoseFK keyframe = robot.getPoseFK();
		double [][] aj1 = robot.approximateJacobian(keyframe);
		double [][] aj2 = robot.exactJacobian(keyframe);
		double diff=0;
		for( int y=0;y<aj1.length;++y ) {
			for( int x=0;x<aj1[y].length;++x ) {
				diff += Math.abs(aj1[y][x]-aj2[y][x]);
			}
		}
		System.out.println("CompareJacobians() end.  Diff = "+diff);
	}
	
	
	@Test
	public void TestSerialize() throws Exception {
		Sixi2 a = new Sixi2();

        final ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(a);
        oos.flush();
        oos.close();

        final ByteArrayInputStream inTest = new ByteArrayInputStream(streamReal.toByteArray());
        final ObjectInputStream is = new ObjectInputStream(inTest);
        final Object bObj = is.readObject();
        
        assertTrue(bObj instanceof Sixi2);
        Sixi2 b = (Sixi2)bObj;
        assertTrue(b.getName().equals("Sixi2"));

        assertTrue(b.getChildren().size() == a.getChildren().size());
	}
	
	@Test
	public void TestReadWrite() throws Exception {
		Sixi2 a = new Sixi2();
		Sixi2 b = new Sixi2();
		/*
        final ByteArrayOutputStream streamReal = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(streamReal);
        oos.writeObject(a);
        oos.flush();
        oos.close();

        final ByteArrayInputStream inTest = new ByteArrayInputStream(streamReal.toByteArray());
        final ObjectInputStream is = new ObjectInputStream(inTest);
        final Object bObj = is.readObject();
        
        assertTrue(bObj instanceof Sixi2);
        Sixi2 b = (Sixi2)bObj;
        assertTrue(b.getName().equals("Sixi2"));*/
		try {
		a.save("test.sixi2");
		b.load("test.sixi2");
		} catch( Exception e) {
			e.printStackTrace();
		}
		
        assertTrue(b.getChildren().size() == a.getChildren().size());
	}
}
