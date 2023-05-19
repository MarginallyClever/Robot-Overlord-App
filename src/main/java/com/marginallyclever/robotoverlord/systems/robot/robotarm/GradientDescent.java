package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.robots.Robot;

import javax.vecmath.Matrix4d;

/**
 * Gradient descent is a method of finding a local minimum of a function.  This class uses Gradient Descent to
 * attempt to find the joint angles that will move the end effector closer to the target.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Deprecated
public class GradientDescent {
	private final RobotComponent myRobot;

	public GradientDescent(RobotComponent subject) {
		super();
		myRobot = subject;
	}

	private double getDistanceToTarget(RobotComponent robot,Matrix4d target) {
		Matrix4d endEffectorPose = (Matrix4d)robot.get(Robot.END_EFFECTOR);
		double [] distance = MatrixHelper.getCartesianBetweenTwoMatrices(endEffectorPose,target);
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
		
		double [] angles = myRobot.getAllJointValues();
		
		// seems to work better descending from the finger than ascending from the base.
		for(int i = myRobot.getNumBones()-1; i>=0; --i ) {  // descending mode
			//logger.info("\tA angles["+i+"]="+angles[i]);
			double gradient = partialGradient(target,angles,i,samplingDistance);
			//logger.info("\tB angles["+i+"]="+angles[i]+"\tlearningRate="+learningRate+"\tgradient="+gradient);
			angles[i] -= learningRate * gradient;
			//logger.info("\tC angles["+i+"]="+angles[i]);
			myRobot.setAllJointValues(angles);
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
		myRobot.setAllJointValues(angles);
		//double t1 = temp.getBone(i).getTheta();
		double FxPlusD = getDistanceToTarget(myRobot,target);
		double gradient = (FxPlusD - Fx) / samplingDistance;
		//logger.info("\t\tFx="+Fx+"\tt0="+t0+"\tt1="+t1+"\tFxPlusD="+FxPlusD+"\tsamplingDistance="+samplingDistance+"\tgradient="+gradient);
		
		// reset the old value
		angles[i] = oldValue;
		myRobot.setAllJointValues(angles);
		
		return gradient;
	}
}
