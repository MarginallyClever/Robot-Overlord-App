package com.marginallyclever.robotoverlord.components.robot.robotarm;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotoverlord.components.robot.RobotComponent;

import javax.vecmath.Matrix4d;

@Deprecated
public class GradientDescent {
	private final RobotComponent myRobot;

	public GradientDescent(RobotComponent subject) {
		super();
		myRobot = subject;
	}

	private double getDistanceToTarget(RobotComponent robot,Matrix4d target) {
		double [] distance = MatrixHelper.getCartesianBetweenTwoMatrices(myRobot.getEndEffectorPose(),target);
		double sum=0;
		for( double d : distance ) sum += Math.abs(d);
		return sum;
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
		if(getDistanceToTarget(myRobot,target)<threshold) return true;
		
		double [] angles = myRobot.getAngles();
		
		// seems to work better descending from the finger than ascending from the base.
		for(int i = myRobot.getNumBones()-1; i>=0; --i ) {  // descending mode
			//Log.message("\tA angles["+i+"]="+angles[i]);
			double gradient = partialGradient(target,angles,i,samplingDistance);
			//Log.message("\tB angles["+i+"]="+angles[i]+"\tlearningRate="+learningRate+"\tgradient="+gradient);
			angles[i] -= learningRate * gradient;
			//Log.message("\tC angles["+i+"]="+angles[i]);
			myRobot.setAngles(angles);
			if(getDistanceToTarget(myRobot,target)<threshold) {
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
		double Fx = getDistanceToTarget(myRobot,target);

		// move F+D, measure again.
		angles[i] += samplingDistance;
		//double t0 = temp.getBone(i).getTheta();
		myRobot.setAngles(angles);
		//double t1 = temp.getBone(i).getTheta();
		double FxPlusD = getDistanceToTarget(myRobot,target);
		double gradient = (FxPlusD - Fx) / samplingDistance;
		//Log.message("\t\tFx="+Fx+"\tt0="+t0+"\tt1="+t1+"\tFxPlusD="+FxPlusD+"\tsamplingDistance="+samplingDistance+"\tgradient="+gradient);
		
		// reset the old value
		angles[i] = oldValue;
		myRobot.setAngles(angles);
		
		return gradient;
	}
}
