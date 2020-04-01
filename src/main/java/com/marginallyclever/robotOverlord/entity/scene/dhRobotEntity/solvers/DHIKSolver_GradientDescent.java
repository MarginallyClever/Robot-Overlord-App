package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;

/**
 * See https://www.alanzucconi.com/2017/04/10/gradient-descent/
 * and https://www.alanzucconi.com/2017/04/10/robotic-arms/
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DHIKSolver_GradientDescent extends DHIKSolver {
	protected double learningRate=0.0625;
	protected double samplingDistance=0.5;
	
	protected DHRobotEntity robot;
	protected Matrix4d targetMatrix;
	protected DHLink tip;
	/**
	 * @return the number of double values needed to store a valid solution from this DHIKSolver.
	 */
	public int getSolutionSize() {
		return 6;
	}

	public double distanceToTarget() {
		// TODO this is a shitty, breakable way of finding the end effector.
		Matrix4d tpw = tip.getPoseWorld();
		tpw.sub(targetMatrix);
		double dp =
				Math.abs(tpw.m00) + Math.abs(tpw.m01) + Math.abs(tpw.m02) + Math.abs(tpw.m03) +
				Math.abs(tpw.m10) + Math.abs(tpw.m11) + Math.abs(tpw.m12) + Math.abs(tpw.m13) +
				Math.abs(tpw.m20) + Math.abs(tpw.m21) + Math.abs(tpw.m22) + Math.abs(tpw.m23) +
				Math.abs(tpw.m30) + Math.abs(tpw.m31) + Math.abs(tpw.m32) + Math.abs(tpw.m33);
		return dp;
	}

	protected double partialDescent(DHLink link) {
		double oldValue = link.getAdjustableValue();
		double newValue = oldValue + samplingDistance;
		double Fx = distanceToTarget();
		link.setAdjustableValue(newValue);
		link.refreshPoseMatrix();
		double FxPlusD = distanceToTarget();
		link.setAdjustableValue(oldValue);
		link.refreshPoseMatrix();

		double gradient = ( FxPlusD - Fx ) / samplingDistance;
		return gradient;
	}

	/**
	 * We're going to jiggle the arm very slightly and see which jiggle gets us closer to the target.
	 * Eventually we get close enough and quit.
	 * We might not actually reach the target by the time we've done interating.
	 */
	@Override
	public SolutionType solveWithSuggestion(DHRobotEntity robot,Matrix4d targetMatrix,DHKeyframe keyframe,DHKeyframe suggestion) {
		this.robot = robot;
		this.targetMatrix = targetMatrix;
		this.tip = (DHLink)robot.findByPath("./X/Y/Z/U/V/W/End Effector");
		assert(tip.isAnEndEffector()==true);

		learningRate=0.125;
		
		// robot sensor spec is 12 bits, or 2^12 steps per rotation.
		final double s = 360.0/Math.pow(2,12);
		double [] samplingDistances = { s*0.125, s, s, s*5, s*5, s*5 };

		double dtt=0;
		
		for(int iter=0;iter<20;++iter) {
			for( int i=0; i<robot.getNumLinks(); ++i ) {
				DHLink link = robot.links.get(i);
				samplingDistance = samplingDistances[i];
				
				double oldValue = link.getAdjustableValue();
				double gradient = partialDescent( link );
				double newValue = oldValue - gradient * learningRate; 
				link.setAdjustableValue(newValue);
		
				dtt=distanceToTarget();
				if(dtt<1) break;
			}
			if(dtt<1) break;
		}
		
		for( int i=0; i<robot.getNumLinks(); ++i ) {
			keyframe.fkValues[i] = robot.links.get(i).getAdjustableValue();
		}
		
		return SolutionType.ONE_SOLUTION;
	}
}
