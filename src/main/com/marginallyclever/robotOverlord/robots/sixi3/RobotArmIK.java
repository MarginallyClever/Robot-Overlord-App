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

/**
 * {@link RobotArmIK} is a {@link RobotArmFK} with added Inverse Kinematics.  
 * Registered in {@code com.marginallyclever.robotOverlord.entity.Entity}
 * @see <a href='https://en.wikipedia.org/wiki/Inverse_kinematics'>Inverse Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class RobotArmIK extends PoseEntity {
	private static final long serialVersionUID = -7778520191789995554L;

	private RobotArmFK myArmFK = new RobotArmFK();
	private PoseEntity eeTarget = new PoseEntity("Target");
		
	public RobotArmIK() {
		super();
		setName("RobotArmIK");

		addChild(eeTarget);
		setEndEffectorTarget(getEndEffector());
	}
	
	public RobotArmIK(RobotArmFK armFK) {
		super();
		myArmFK = armFK;
		setName(myArmFK.getName());
		setEndEffectorTarget(getEndEffector());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		RobotArmIK b = (RobotArmIK)super.clone();
		
		b.myArmFK = (RobotArmFK)myArmFK.clone();
		b.eeTarget = (PoseEntity)(eeTarget.clone());
		
		return b;
	}

	@Override
	public void update(double dt) {
		super.update(dt);
		myArmFK.update(dt);
		
		// move arm towards result to get future pose
		try {
			//JacobianNewtonRaphson.step(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, getPose());
		myArmFK.render(gl2);
		drawPathToTarget(gl2);
		gl2.glPopMatrix();
		
		MatrixHelper.drawMatrix(gl2, getEndEffector(), 1);
	}
	
	private void drawPathToTarget(GL2 gl2) {
		Matrix4d start = myArmFK.getEndEffector();
		
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
	
	@Override
	public void getView(ViewPanel view) {
		myArmFK.getView(view);
		view.pushStack("IK","Inverse Kinematics");

		ViewElementButton b = view.addButton("Reset GoTo");
		b.addPropertyChangeListener((evt) -> {
			eeTarget.setPose(myArmFK.getEndEffector());
		});

		ViewElementButton b2 = view.addButton("Run test");
		b2.addPropertyChangeListener((evt) -> {
			//testPathCalculation(100,true);
			//testPathCalculation(100,false);
			//testTime(true);
			testTime(false);
		});
				
		view.popStack();
		
		super.getView(view);
	}
	
	@SuppressWarnings("unused")
	private void testPathCalculation(double STEPS,boolean useExact) {
		double [] jOriginal = myArmFK.getAngles();
		Matrix4d start = myArmFK.getEndEffector();
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
	
				double [] jBefore = myArmFK.getAngles();

				// move arm towards result to get future pose
				try {
					JacobianNewtonRaphson.step(this);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				double [] jAfter = myArmFK.getAngles();

				double [] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(old, interpolated);
				old.set(interpolated);
	
				ApproximateJacobian aj = new ApproximateJacobian(myArmFK);
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
		
		myArmFK.setAngles(jOriginal);
		
		Matrix4d startCompare = myArmFK.getEndEffector();
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
				new ApproximateJacobian(myArmFK);
			}
		}
		
		long end = System.nanoTime();
		System.out.println("diff="+((double)(end-start)/1000.0)+(useExact?"exact":"approx"));
	}
	
	public Matrix4d getEndEffector() {
		return myArmFK.getEndEffector(); 
	}

	public Matrix4d getEndEffectorTarget() {
		return eeTarget.getPose();
	}

	/**
	 * Update the target end effector and fire a {@link PropertyChangeEvent} notice.  
	 * The {@link PropertyChangeEvent.propertyName} will be "eeTarget".
	 * @param m1 the new end effector target.
	 */
	public void setEndEffectorTarget(Matrix4d m1) {
		Matrix4d m0 = eeTarget.getPoseWorld();
		eeTarget.setPose(m1);
		
		notifyPropertyChangeListeners(new PropertyChangeEvent(this,"eeTarget",m0,m1));
	}

	public double[] getAngles() {
		return myArmFK.getAngles();
	}

	public void setAngles(double[] list) {
		myArmFK.setAngles(list);
	}

	public int getNumBones() {
		return myArmFK.getNumBones();
	}

	public RobotArmBone getBone(int i) {
		return myArmFK.getBone(i);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener p) {
		super.addPropertyChangeListener(p);
		myArmFK.addPropertyChangeListener(p);
	}

	public ApproximateJacobian getApproximateJacobian() {
		return new ApproximateJacobian(myArmFK);
	}


	public double getDistanceToTarget(Matrix4d m4) {
		return myArmFK.getDistanceToTarget(m4);
	}	
}
