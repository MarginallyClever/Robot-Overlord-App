package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;

/**
 * TODO unfinished!
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class DHIKSolver_GradientDescent extends DHIKSolver {
	public double costFunction(DHKeyframe a,DHKeyframe b) {
		double sum=0;
		assert(a.fkValues.length==b.fkValues.length);
		for( int i=0; i<b.fkValues.length; ++i) {
			double delta = Math.abs( a.fkValues[i] - b.fkValues[i] );
			sum+=delta;
		}
		return sum;
	}
	
	@Override
	public SolutionType solveWithSuggestion(DHRobotEntity robot,Matrix4d targetMatrix,DHKeyframe keyframe,DHKeyframe suggestion) {
		// We're going to jiggle the arm very slightly and see which jiggle gets us closer to the target.  Eventually we get close enough and quit.
		// We might not actually reach the target by the time we've done interating.
		
		return SolutionType.NO_SOLUTIONS;
	}
}
