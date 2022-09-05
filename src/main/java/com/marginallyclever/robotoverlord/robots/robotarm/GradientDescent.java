package com.marginallyclever.robotoverlord.robots.robotarm;

import javax.vecmath.Matrix4d;

public class GradientDescent {
	private RobotArmFK sixi3;

	GradientDescent(RobotArmFK subject) {
		super();
		sixi3=subject;
	}
	
	/**
	 * Use gradient descent to move the end effector closer to the target.  The process is iterative, might not reach the target,
	 * and changes depending on the position when gradient descent began. 
	 * @return distance to target
	 * @param learningRate in a given iteration the stepSize is x.  on the next iteration it should be x * refinementRate. 
	 * @param threshold When error term is within threshold then stop. 
	 * @param samplingDistance how big should the first step be?
	 */
	public boolean run(Matrix4d target,double learningRate, double threshold, double samplingDistance) {
		if(sixi3.getDistanceToTarget(target)<threshold) return true;
		
		double [] angles = sixi3.getAngles();
		
		// seems to work better descending from the finger than ascending from the base.
		for( int i=sixi3.getNumBones()-1; i>=0; --i ) {  // descending mode
			//Log.message("\tA angles["+i+"]="+angles[i]);
			double gradient = partialGradient(target,angles,i,samplingDistance);
			//Log.message("\tB angles["+i+"]="+angles[i]+"\tlearningRate="+learningRate+"\tgradient="+gradient);
			angles[i] -= learningRate * gradient;
			//Log.message("\tC angles["+i+"]="+angles[i]);
			sixi3.setAngles(angles);
			if(sixi3.getDistanceToTarget(target)<threshold) {
				return true;
			}
		}

		// if you get here the robot did not reach its target.
		// try tweaking your input parameters for better results.
		return false;
	}
	
	private double partialGradient(Matrix4d target, double [] angles, int i, double samplingDistance) {
		// get the current error term F.
		double oldValue = angles[i];
		double Fx = sixi3.getDistanceToTarget(target);

		// move F+D, measure again.
		angles[i] += samplingDistance;
		//double t0 = temp.getBone(i).getTheta();
		sixi3.setAngles(angles);
		//double t1 = temp.getBone(i).getTheta();
		double FxPlusD = sixi3.getDistanceToTarget(target);
		double gradient = (FxPlusD - Fx) / samplingDistance;
		//Log.message("\t\tFx="+Fx+"\tt0="+t0+"\tt1="+t1+"\tFxPlusD="+FxPlusD+"\tsamplingDistance="+samplingDistance+"\tgradient="+gradient);
		
		// reset the old value
		angles[i] = oldValue;
		sixi3.setAngles(angles);
		
		return gradient;
	}
}
