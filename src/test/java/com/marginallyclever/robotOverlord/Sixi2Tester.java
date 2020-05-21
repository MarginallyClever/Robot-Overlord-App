package com.marginallyclever.robotOverlord;

import javax.vecmath.Matrix4d;

import org.junit.Test;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.Sixi2;

public class Sixi2Tester {
	/**
	 * Test that IK(FK(A))==A for many random fk pose A. 
	 */
	@Test
	public void TestIK() {
		Sixi2 robot = new Sixi2();
		int numLinks = robot.sim.links.size();
		DHKeyframe key0 = robot.sim.getIKSolver().createDHKeyframe();
		DHKeyframe key1 = robot.sim.getIKSolver().createDHKeyframe();
		
		final int totalTests = 1000;
		int testsOK=0;
		int testsNoMatch=0;
		int testsNoIK=0;
		
		for( int j = 0; j < totalTests; ++j ) {
			// find a random pose for the whole arm
			System.out.print(j + ": ");
			
			for( int i = 0; i < numLinks; ++i ) {
				DHLink link = robot.sim.links.get(i);
				// find a random pose for this bone.
				double top = link.getRangeMax();
				double bot = link.getRangeMin();
				double range = top-bot;
				double v = range * Math.random() + bot;
				key0.fkValues[i]=v;
				System.out.print(StringHelper.formatDouble(v)+"\t");
			}
			
			// set the pose
			robot.sim.setPoseFK(key0);
			// get the end effector world pose for this key
			Matrix4d ee = robot.sim.endEffector.getPoseWorld();
			// use the end effector world pose to solve IK
			if(robot.sim.setPoseIK(ee)) {
				robot.sim.getPoseFK(key1);
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
	}
	

	/**
	 * Test that approximate jacobian at the home position does not generate any NaN values. 
	 */
	@Test
	public void TestApproximateJacobian() {
		System.out.println("TestApproximateJacobian start");
		Sixi2 robot = new Sixi2();
		//robot.goHome();
		DHKeyframe keyframe = robot.sim.getIKSolver().createDHKeyframe();
		robot.sim.getPoseFK(keyframe);
		double [][] aj = robot.sim.approximateJacobian(keyframe);
		for( int y=0;y<aj.length;++y ) {
			for( int x=0;x<aj[y].length;++x ) {
				assert(!Double.isNaN(aj[y][x]));
			}
		}
		System.out.println("TestApproximateJacobian end");
	}
}
