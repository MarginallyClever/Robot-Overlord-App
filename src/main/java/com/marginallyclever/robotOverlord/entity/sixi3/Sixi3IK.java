package com.marginallyclever.robotOverlord.entity.sixi3;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * {@link Sixi3IK} is a {@link Sixi3FK} with added Inverse Kinematics.  
 * Registered in {@code com.marginallyclever.robotOverlord.entity.Entity}
 * @see <a href='https://en.wikipedia.org/wiki/Inverse_kinematics'>Inverse Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class Sixi3IK extends Sixi3FK {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7778520191789995554L;

	// target end effector pose
	private PoseEntity eeTarget = new PoseEntity("Target");

	private DoubleEntity threshold = new DoubleEntity("Threshold",0.01); 
	private DoubleEntity stepSize = new DoubleEntity("Step size",0.125); 
	private DoubleEntity learningRate = new DoubleEntity("Leaning rate",0.005); 
	
	public Sixi3IK() {
		super();
		setName("Sixi3IK");

		addChild(eeTarget);
		eeTarget.addPropertyChangeListener(this);
		
		Matrix4d m = new Matrix4d();
		getEndEffector(m);
		eeTarget.setPose(m);
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
	}
	
	/**
	 * Measures the difference between the latest end effector matrix and the target matrix.
	 * It is a combination of the linear distance and the rotation distance (collectively known as the Twist)
	 * @return the error term.
	 */
	public double distanceToTarget(final Matrix4d target) {
		// Scale the "handles" used.  Bigger scale, greater rotation compensation.
		final double GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE = 100;
		
		Matrix4d m = new Matrix4d();
		getEndEffector(m);
		
		// linear difference in centers
		Vector3d c0 = new Vector3d();
		Vector3d c1 = new Vector3d();
		m.get(c0);
		target.get(c1);
		c1.sub(c0);
		double dC = c1.lengthSquared();
		
		// linear difference in X handles
		Vector3d x0 = MatrixHelper.getXAxis(target);
		Vector3d x1 = MatrixHelper.getXAxis(m);
		x1.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		x0.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		x1.sub(x0);
		double dX = x1.lengthSquared();
		
		// linear difference in Y handles
		Vector3d y0 = MatrixHelper.getYAxis(target);
		Vector3d y1 = MatrixHelper.getYAxis(m);
		y1.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		y0.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		y1.sub(y0);
		double dY = y1.lengthSquared();		

	    // now sum these to get the error term.
		return dC+dX+dY;
	}
	
	/**/
	private double partialGradientDescent(final Matrix4d target, double [] fk, double [] samplingDistances, int i) {
		// get the current error term F.
		double oldValue = fk[i];
		double Fx = distanceToTarget(target);

		// move F+D, measure again.
		fk[i] = oldValue + samplingDistances[i];
		setFKValues(fk);
		double FxPlusD = distanceToTarget(target);

		// move F-D, measure again.
		fk[i] = oldValue - samplingDistances[i];
		setFKValues(fk);
		double FxMinusD = distanceToTarget(target);

		// restore the old value
		fk[i] = oldValue;
		setFKValues(fk);

		// if F+D and F-D have more error than F, try smaller step size next time. 
		if( FxMinusD > Fx && FxPlusD > Fx ) {
			// If we somehow are *exactly* fit then Fx is zero and /0 is bad.
			if( Fx != 0 ) {
				samplingDistances[i] *= Math.min(FxMinusD, FxPlusD) / Fx;
			}
			return 0;
		}
		
		double gradient = ( FxPlusD - Fx ) / samplingDistances[i];
		return gradient;
	}

	/**
	 * Use gradient descent to move the end effector closer to the target.  The process is iterative, might not reach the target,
	 * and changes depending on the position when gradient descent began. 
	 * @return distance to target
	 * @param iterations How many times should I try to get closer?
	 * @param threshold When error term is within threshold then stop. 
	 * @param learningRate how much of that partial descent to actually apply each step?
	 * @param initialSampleSize How many times should I try to get closer?
	 */
	private boolean gradientDescent(final Matrix4d target,final double iterations, final double threshold, final double learningRate, final double initialSampleSize) {
		// pose before gradient descent starts
		double [] fk = new double [Sixi3FK.NUM_BONES];
		getFKValues(fk);

		// how big a step to take with each partial descent?
		double [] samplingDistances = new double[Sixi3FK.NUM_BONES];
		for(int i=0;i<Sixi3FK.NUM_BONES;++i) {
			samplingDistances[i]=initialSampleSize;
		}
		
		for(int j=0;j<iterations;++j) {
			// seems to work better descending from the finger than ascending from the base.
			//for( int i=0; i<Sixi3FK.NUM_BONES; ++i ) {  // ascending mode
			for( int i=Sixi3FK.NUM_BONES-1; i>=0; --i ) {  // descending mode
				double oldValue = fk[i];
				double gradient = partialGradientDescent(target,fk,samplingDistances,i);
				fk[i] = oldValue - gradient * learningRate; 
				setFKValues(fk);

				double dtt=distanceToTarget(target);
				if(dtt<=threshold) {
					// we hit the target, stop early.
					return true;
				}
			}
		}
		
		// if you get here the robot did not reach its target within 'iteration' steps.
		// try tweaking your input parameters for better results.
		return false;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("IK","Inverse Kinematics");

		ViewElementButton b = view.addButton("Reset GoTo");
		b.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Matrix4d m = new Matrix4d();
				getEndEffector(m);
				eeTarget.setPose(m);
			}
		});
		
		// add gradient descent parameters here
		view.add(threshold);
		view.add(stepSize);
		view.add(learningRate);
		
		view.popStack();
		
		super.getView(view);
	}
	
	// When GUI elements are changed they each cause a {@link PropertyChangeEvent}.
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Object src = evt.getSource();
		
		if(src == eeTarget && evt.getPropertyName().contentEquals("pose")) {
			double [] v = new double[Sixi3FK.NUM_BONES];
			double [] jBefore = new double[Sixi3FK.NUM_BONES];
			double [] jAfter = new double[Sixi3FK.NUM_BONES];
			getFKValues(v);
			Matrix4d start = new Matrix4d();
			getEndEffector(start);
			Matrix4d end = eeTarget.getPose();
			Matrix4d interpolated = new Matrix4d();
			Matrix4d old = new Matrix4d(start);
			double [][] jacobian = new double[6][6];
			double [] cartesianDistance = new double[6];
			double [] cartesianDistanceCompare = new double[6];
			double [] jointDistance = new double[Sixi3FK.NUM_BONES];
			final double STEPS=20;

			//System.out.print("S"+start.toString()+"E"+end.toString());
			
			for(double alpha=1;alpha<=STEPS;++alpha) {
				MatrixHelper.interpolate(start,end,alpha/STEPS,interpolated);

				getFKValues(jBefore);
				// move arm towards result to get future pose
				gradientDescent(interpolated,30, threshold.get(), stepSize.get(), learningRate.get());
				getFKValues(jAfter);

				getApproximateJacobian(jacobian);
				
				getCartesianBetweenTwoMatrixes(old, interpolated, cartesianDistance);
				old.set(interpolated);

				boolean ok=getJointFromCartesian(jacobian, cartesianDistance, jointDistance);
				getCartesianFromJoint(jacobian, jointDistance, cartesianDistanceCompare);
				// cartesianDistance and cartesianDistanceCompare should always match
				// jointDistance[n] should match jAfter[n]-jBefore[n]

				System.out.print(alpha
						+","
						//+"R"+result.toString()
						);
				if(ok) {
					/*
					for(int i=0;i<6;++i) {
						String add="";
						for(int j=0;j<6;++j) {
							System.out.print(add+jacobian[i][j]);
							add="\t";
						}
						System.out.println();
					}*/
					System.out.println(
							+jointDistance[0]+","
							+jointDistance[1]+","
							+jointDistance[2]+","
							+jointDistance[3]+","
							+jointDistance[4]+","
							+jointDistance[5]+","
							+(jAfter[0]-jBefore[0])+","
							+(jAfter[1]-jBefore[1])+","
							+(jAfter[2]-jBefore[2])+","
							+(jAfter[3]-jBefore[3])+","
							+(jAfter[4]-jBefore[4])+","
							+(jAfter[5]-jBefore[5])+",");
				} else System.out.println(" not ok");
			}
			System.out.println();
			
			setFKValues(v);
			//updateSliders();
		}
	}
}
