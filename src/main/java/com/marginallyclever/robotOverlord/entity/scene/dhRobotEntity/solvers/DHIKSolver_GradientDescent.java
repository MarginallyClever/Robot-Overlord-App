package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
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
	protected final double SENSOR_RESOLUTION = 360.0/Math.pow(2,12);
	protected double [] samplingDistances = { 
		SENSOR_RESOLUTION, 
		SENSOR_RESOLUTION, 
		SENSOR_RESOLUTION, 
		SENSOR_RESOLUTION*4, 
		SENSOR_RESOLUTION*4,
		SENSOR_RESOLUTION*4
	};
	
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
		//tpw.sub(targetMatrix);

		double correctiveFactorMagicNumber = 65;
		
		// linear difference in centers
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		tpw.get(p0);
		targetMatrix.get(p1);
		p1.sub(p0);
		double dC = p1.lengthSquared();
		
		// linear difference in X handles
		Vector3d x0 = MatrixHelper.getXAxis(targetMatrix);
		Vector3d x1 = MatrixHelper.getXAxis(tpw);
		x1.scale(correctiveFactorMagicNumber);
		x0.scale(correctiveFactorMagicNumber);
		x1.sub(x0);
		double dX = x1.lengthSquared();
		
		// linear difference in Y handles
		Vector3d y0 = MatrixHelper.getYAxis(targetMatrix);
		Vector3d y1 = MatrixHelper.getYAxis(tpw);
		y1.scale(correctiveFactorMagicNumber);
		y0.scale(correctiveFactorMagicNumber);
		y1.sub(y0);
		double dY = y1.lengthSquared();		
		
		//System.out.println("C"+dC+"\tX"+dX+"\tY"+dY);
		return dC+dX+dY;
	}

	protected double partialDescent(DHLink link,int i) {
		double oldValue = link.getAdjustableValue();
		double Fx = distanceToTarget();

		link.setAdjustableValue(oldValue + samplingDistances[i]);
		link.refreshPoseMatrix();
		double FxPlusD = distanceToTarget();

		link.setAdjustableValue(oldValue - samplingDistances[i]);
		link.refreshPoseMatrix();
		double FxMinusD = distanceToTarget();

		link.setAdjustableValue(oldValue);
		link.refreshPoseMatrix();

		if( FxMinusD > Fx && FxPlusD > Fx ) {
			samplingDistances[i]/=2;
			return 0;
		}
		
		double gradient = ( FxPlusD - Fx ) / samplingDistances[i];
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

		samplingDistances[0]=SENSOR_RESOLUTION; 
		samplingDistances[1]=SENSOR_RESOLUTION;
		samplingDistances[2]=SENSOR_RESOLUTION;
		samplingDistances[3]=SENSOR_RESOLUTION*4;
		samplingDistances[4]=SENSOR_RESOLUTION*4;
		samplingDistances[5]=SENSOR_RESOLUTION*4;
		
		// robot sensor spec is 12 bits, or 2^12 steps per rotation.

		double dtt=10;
		
		for(int iter=0;iter<20;++iter) {
			// seems to work better ascending than descending
			//for( int i=0; i<robot.getNumLinks(); ++i ) {
			for( int i=robot.getNumLinks()-1; i>=0; --i ) {
				DHLink link = robot.links.get(i);
				
				double oldValue = link.getAdjustableValue();
				double gradient = partialDescent( link, i );
				double newValue = oldValue - gradient * learningRate; 
				link.setAdjustableValue(newValue);
				link.refreshPoseMatrix();
		
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
