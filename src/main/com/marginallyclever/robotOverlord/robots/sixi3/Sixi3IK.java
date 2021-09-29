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

/**
 * {@link Sixi3IK} is a {@link Sixi3FK} with added Inverse Kinematics.  
 * Registered in {@code com.marginallyclever.robotOverlord.entity.Entity}
 * @see <a href='https://en.wikipedia.org/wiki/Inverse_kinematics'>Inverse Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class Sixi3IK extends PoseEntity {
	private static final long serialVersionUID = -7778520191789995554L;

	private Sixi3FK sixi3fk = new Sixi3FK();
	private Sixi3FK gradientFK = new Sixi3FK();
	private PoseEntity eeTarget = new PoseEntity("Target");
	
	private DoubleEntity threshold = new DoubleEntity("Threshold",0.5);
	private DoubleEntity samplingDistance = new DoubleEntity("Sampling distance (>0)",0.05);
	private DoubleEntity learningRate = new DoubleEntity("Learning rate (0...1)",2.0);
	private double learningRateNow=learningRate.get();
	
	public Sixi3IK() {
		super();
		setName("Sixi3IK");

		addChild(eeTarget);
		setEndEffectorTarget(getEndEffector());
	}
	
	@Override
	public void update(double dt) {
		super.update(dt);
		sixi3fk.update(dt);
		
		// move arm towards result to get future pose
		gradientDescent(eeTarget.getPose(),threshold.get());
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, getPose());
		sixi3fk.render(gl2);
		drawPathToTarget(gl2);
		gl2.glPopMatrix();

		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, getPose());
		gl2.glColor4d(0,0,1,0.8);
		gradientFK.render(gl2);
		gl2.glPopMatrix();
	}
	
	private void drawPathToTarget(GL2 gl2) {
		Matrix4d start = sixi3fk.getEndEffector();
		
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
	 * Use gradient descent to move the end effector closer to the target.  The process is iterative, might not reach the target,
	 * and changes depending on the position when gradient descent began. 
	 * @return distance to target
	 * @param learningRate in a given iteration the stepSize is x.  on the next iteration it should be x * refinementRate. 
	 * @param threshold When error term is within threshold then stop. 
	 * @param samplingDistance how big should the first step be?
	 */
	private void gradientDescent(final Matrix4d target, final double threshold) {
		double d0 = gradientFK.getDistanceToTarget(target);
		if(d0<threshold) {
			// target reached!
			sixi3fk.setAngles(gradientFK.getAngles());
		}
		
		GradientDescent gd = new GradientDescent(gradientFK);
		gd.run(target,learningRateNow,threshold,samplingDistance.get());

		double d1 = gradientFK.getDistanceToTarget(target);
		if(d1>d0) {
			learningRateNow*=0.95;
		} else {
			learningRateNow*=1.01;
		}
		
		if(d1<threshold) {
			// target reached!
			sixi3fk.setAngles(gradientFK.getAngles());
		} else {
			System.out.println("gradient Descent="+d1+" learningRateNow="+learningRateNow);
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		sixi3fk.getView(view);
		view.pushStack("IK","Inverse Kinematics");

		ViewElementButton b = view.addButton("Reset GoTo");
		b.addPropertyChangeListener((evt) -> {
			eeTarget.setPose(sixi3fk.getEndEffector());
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
		view.add(samplingDistance);
		view.add(learningRate);
		
		view.popStack();
		
		super.getView(view);
	}
	
	@SuppressWarnings("unused")
	private void testPathCalculation(double STEPS,boolean useExact) {
		double [] jOriginal = sixi3fk.getAngles();
		Matrix4d start = sixi3fk.getEndEffector();
		Matrix4d end = eeTarget.getPose();
		
		try {
			PrintWriter pw = new PrintWriter(new File("test"+((int)STEPS)+"-"+(useExact?"e":"a")+".csv"));

			Matrix4d interpolated = new Matrix4d();
			Matrix4d old = new Matrix4d(start);
			//double [] cartesianDistanceCompare = new double[6];

			//pw.print("S"+start.toString()+"E"+end.toString());

			for(double alpha=1;alpha<=STEPS;++alpha) {
				pw.print((int)alpha+"\t");

				MatrixHelper.interpolate(start,end,alpha/STEPS,interpolated);
	
				double [] jBefore = sixi3fk.getAngles();

				// move arm towards result to get future pose
				learningRateNow=learningRate.get();
				gradientDescent(interpolated,threshold.get());
				double [] jAfter = sixi3fk.getAngles();

				double [] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(old, interpolated);
				old.set(interpolated);
	
				ApproximateJacobian aj = new ApproximateJacobian(sixi3fk);
				try {
					double [] jointDistance = aj.getJointFromCartesian(cartesianDistance);
					//getCartesianFromJoint(jacobian, jointDistance, cartesianDistanceCompare);
					// cartesianDistance and cartesianDistanceCompare should always match
					// jointDistance[n] should match jAfter[n]-jBefore[n]
	
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
				} catch(Exception e) {
					pw.println(" not ok");
				}
			}

			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		sixi3fk.setAngles(jOriginal);
		
		Matrix4d startCompare = sixi3fk.getEndEffector();
		if(!startCompare.equals(start)) {
			System.out.println("Change!\nS"+start.toString()+"E"+startCompare.toString());
		}

		//updateSliders();
	}
	
	private void testTime(boolean useExact) {
		long start = System.nanoTime();

		for(int i=0;i<1000;++i) {
			if(useExact) {
				//getExactJacobian(jacobian);
			} else {
				new ApproximateJacobian(sixi3fk);
			}
		}
		
		long end = System.nanoTime();
		System.out.println("diff="+((double)(end-start)/1000.0)+(useExact?"exact":"approx"));
	}
	
	public Matrix4d getEndEffector() {
		Matrix4d m = getPoseWorld();
		Matrix4d ee = sixi3fk.getEndEffector(); 
		m.mul(ee);
		return m;
	}

	public Matrix4d getEndEffectorTarget() {
		return eeTarget.getPoseWorld();
	}

	public void setEndEffectorTarget(Matrix4d m1) {
		gradientFK.setAngles(sixi3fk.getAngles());
		learningRateNow=0.001;
		Matrix4d m0 = eeTarget.getPoseWorld();
		eeTarget.setPoseWorld(m1);
		
		notifyPropertyChangeListeners(new PropertyChangeEvent(this,"eeTarget",m0,m1));
	}

	public double[] getAngles() {
		return sixi3fk.getAngles();
	}

	public boolean setAngles(double[] list) {
		return sixi3fk.setAngles(list);
	}

	public int getNumBones() {
		return sixi3fk.getNumBones();
	}

	public Sixi3Bone getBone(int i) {
		return sixi3fk.getBone(i);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener p) {
		super.addPropertyChangeListener(p);
		sixi3fk.addPropertyChangeListener(p);
	}

	public ApproximateJacobian getApproximateJacobian() {
		return new ApproximateJacobian(sixi3fk);
	}


	public double getDistanceToTarget(Matrix4d m4) {
		return sixi3fk.getDistanceToTarget(m4);
	}	
}
