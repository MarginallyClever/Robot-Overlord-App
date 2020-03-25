package com.marginallyclever.robotOverlord;

import javax.vecmath.Matrix4d;

import org.junit.Test;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.Sixi2;

public class Sixi2Tester {
	/**
	 * Test that IK(FK(A))==A for many random fkpose A. 
	 */
	@Test
	public void TestIK() {
		Sixi2 robot = new Sixi2();
		int numLinks = robot.sim.links.size();
		DHKeyframe key = robot.sim.getIKSolver().createDHKeyframe();
		
		for( int j = 0; j < 1000; ++j ) {
			// find a random pose for the whole arm
			for( int i = 0; i < numLinks; ++i ) {
				DHLink link = robot.sim.links.get(i);
				// find a random pose for this bone.
				double top = link.getRangeMax();
				double bot = link.getRangeMin();
				double range = top-bot;
				double v = range * Math.random() + bot;
				key.fkValues[i]=v;
			}
			// set the pose
			robot.sim.setPoseFK(key);
			// get the end effector world pose for this key
			Matrix4d ee = robot.sim.endEffector.getPoseWorld();
			// use the key to solve IK
			if(robot.sim.setPoseIK(ee)) {
				System.out.print(j + ": "+key.fkValues+" OK");
			} else {
				System.out.print(j + ": "+key.fkValues+" BAD");
			}
		}
	}
}
