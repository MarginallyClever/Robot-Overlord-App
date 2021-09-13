package com.marginallyclever.robotOverlord.robots.sixi3;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.IntEntity;

/**
 * {@link Sixi3IK} is a {@link Sixi3FK} with added Inverse Kinematics.  
 * Registered in {@code com.marginallyclever.robotOverlord.entity.Entity}
 * @see <a href='https://en.wikipedia.org/wiki/Inverse_kinematics'>Inverse Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class Sixi3IK extends Sixi3FK implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7778520191789995554L;

	// target end effector pose
	private PoseEntity eeTarget = new PoseEntity("Target");

	private DoubleEntity threshold = new DoubleEntity("Threshold",0.001);
	private DoubleEntity stepSize = new DoubleEntity("Step size",10.0);
	private IntEntity iterations = new IntEntity("Iterations",40);
	private DoubleEntity refinementRate = new DoubleEntity("Refinement rate (0...1)",0.85); 
	
	public Sixi3IK() {
		super();
		setName("Sixi3IK");

		addChild(eeTarget);
		
		Matrix4d m = new Matrix4d();
		getEndEffector(m);
		eeTarget.setPose(m);
		
		this.addPropertyChangeListener(this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		Matrix4d m = (Matrix4d)evt.getNewValue();
		eeTarget.setPose(m);
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
		
		// move arm towards result to get future pose
		gradientDescent(eeTarget.getPose(),iterations.get(),refinementRate.get(), threshold.get(), stepSize.get());
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, getPose());
		drawPathToTarget(gl2);
		gl2.glPopMatrix();
	}
	
	private void drawPathToTarget(GL2 gl2) {
		Matrix4d start = new Matrix4d();
		getEndEffector(start);
		
		Matrix4d end = eeTarget.getPose();
		Matrix4d interpolated = new Matrix4d();
		
		Vector3d a = new Vector3d();
		Vector3d b = new Vector3d();
		start.get(a);
		end.get(b);
		a.sub(b);
		a.length();
		
		double STEPS=a.length();
		for(double d=1;d<STEPS;d++) {
			MatrixHelper.interpolate(start,end,d/STEPS,interpolated);
			MatrixHelper.drawMatrix(gl2, interpolated, 1.0);
		}
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
		double dC = c1.length();
		
		// linear difference in X handles
		Vector3d x0 = MatrixHelper.getXAxis(target);
		Vector3d x1 = MatrixHelper.getXAxis(m);
		x1.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		x0.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		x1.sub(x0);
		double dX = x1.length();
		
		// linear difference in Y handles
		Vector3d y0 = MatrixHelper.getYAxis(target);
		Vector3d y1 = MatrixHelper.getYAxis(m);
		y1.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		y0.scale(GRADIENT_DESCENT_ERROR_TERM_ROTATION_SCALE);
		y1.sub(y0);
		double dY = y1.length();		

	    // now sum these to get the error term.
		return dC+dX+dY;
	}
	
	/**
	 * 
	 * @param target
	 * @param fk
	 * @param jointIndex
	 * @param stepSize
	 * @return the gradient
	 */
	private double partialGradient(final Matrix4d target, double [] fk, int jointIndex, double stepSize) {
		// get the current error term F.
		double oldValue = fk[jointIndex];
		double Fx = distanceToTarget(target);

		// move F+D, measure again.
		fk[jointIndex] = oldValue + stepSize;
		setFKValues(fk);
		double FxPlusD = distanceToTarget(target);

		// set the old value
		fk[jointIndex] = oldValue;
		setFKValues(fk);
		
		return (FxPlusD - Fx) / stepSize;
	}

	/**
	 * Use gradient descent to move the end effector closer to the target.  The process is iterative, might not reach the target,
	 * and changes depending on the position when gradient descent began. 
	 * @return distance to target
	 * @param attempts How many times should I try to get closer?
	 * @param learningRate in a given iteration the stepSize is x.  on the next iteration it should be x * refinementRate. 
	 * @param threshold When error term is within threshold then stop. 
	 * @param initialStepSize how big should the first step be?
	 */
	private boolean gradientDescent(final Matrix4d target, final double attempts, double learningRate, final double threshold, double initialStepSize) {
		if(distanceToTarget(target)<threshold) return true;
		
		double [] angles = getFKValues();
		double stepSize = initialStepSize;
		
		for(int tries=0;tries<attempts;++tries) {
			// seems to work better descending from the finger than ascending from the base.

			for( int i=getNumBones()-1; i>=0; --i ) {  // descending mode
				double gradient = partialGradient(target,angles,i,stepSize);
				angles[i] -= learningRate * gradient;
				setFKValues(angles);
				if(distanceToTarget(target)<threshold) return true;
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
		b.addPropertyChangeListener((evt) -> {
			Matrix4d m = new Matrix4d();
			getEndEffector(m);
			eeTarget.setPose(m);
		});

		ViewElementButton b2 = view.addButton("Run test");
		b2.addPropertyChangeListener((evt) -> {
			//testPathCalculation(100,true);
			//testPathCalculation(100,false);
			//testTime(true);
			testTime(false);
		});
		
		// add gradient descent parameters here
		view.add(threshold);
		view.add(stepSize);
		view.add(iterations);
		view.add(refinementRate);
		
		view.popStack();
		
		super.getView(view);
	}
	
	@SuppressWarnings("unused")
	private void testPathCalculation(double STEPS,boolean useExact) {
		double [] jOriginal = getFKValues();
		Matrix4d start = new Matrix4d();
		getEndEffector(start);
		
		Matrix4d end = eeTarget.getPose();
		
		try {
			PrintWriter pw = new PrintWriter(new File("test"+((int)STEPS)+"-"+(useExact?"e":"a")+".csv"));

			Matrix4d interpolated = new Matrix4d();
			Matrix4d old = new Matrix4d(start);
			double [][] jacobian = new double[6][6];
			double [] cartesianDistance = new double[6];
			//double [] cartesianDistanceCompare = new double[6];
			double [] jointDistance = new double[getNumBones()];

			//pw.print("S"+start.toString()+"E"+end.toString());

			for(double alpha=1;alpha<=STEPS;++alpha) {
				MatrixHelper.interpolate(start,end,alpha/STEPS,interpolated);
	
				double [] jBefore = getFKValues();

				// move arm towards result to get future pose
				gradientDescent(interpolated,20,0.8, threshold.get(), stepSize.get());
				double [] jAfter = getFKValues();

				if(useExact) {
					//getExactJacobian(jacobian);
				} else {
					getApproximateJacobian(jacobian);
				}
				
				getCartesianBetweenTwoMatrixes(old, interpolated, cartesianDistance);
				old.set(interpolated);
	
				boolean ok=getJointFromCartesian(jacobian, cartesianDistance, jointDistance);
				//getCartesianFromJoint(jacobian, jointDistance, cartesianDistanceCompare);
				// cartesianDistance and cartesianDistanceCompare should always match
				// jointDistance[n] should match jAfter[n]-jBefore[n]
	
				pw.print((int)alpha+"\t");
				if(ok) {
					/*
					for(int i=0;i<6;++i) {
						String add="";
						for(int j=0;j<6;++j) {
							pw.print(add+jacobian[i][j]);
							add="\t";
						}
						pw.println();
					}*/
					pw.println(
							+jointDistance[0]+"\t"
							+jointDistance[1]+"\t"
							+jointDistance[2]+"\t"
							+jointDistance[3]+"\t"
							+jointDistance[4]+"\t"
							+jointDistance[5]+"\t"
							+(jAfter[0]-jBefore[0])+"\t"
							+(jAfter[1]-jBefore[1])+"\t"
							+(jAfter[2]-jBefore[2])+"\t"
							+(jAfter[3]-jBefore[3])+"\t"
							+(jAfter[4]-jBefore[4])+"\t"
							+(jAfter[5]-jBefore[5])+"\t");
				} else pw.println(" not ok");
			}

			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		setFKValues(jOriginal);
		
		Matrix4d startCompare = new Matrix4d();
		getEndEffector(startCompare);
		if(!startCompare.equals(start)) {
			System.out.println("Change!\nS"+start.toString()+"E"+startCompare.toString());
		}

		//updateSliders();
	}
	
	private void testTime(boolean useExact) {
		double [][] jacobian = new double[6][6];
		long start = System.nanoTime();

		for(int i=0;i<1000;++i) {
			if(useExact) {
				//getExactJacobian(jacobian);
			} else {
				getApproximateJacobian(jacobian);
			}
		}
		
		long end = System.nanoTime();
		System.out.println("diff="+((double)(end-start)/1000.0)+(useExact?"exact":"approx"));
	}
}
