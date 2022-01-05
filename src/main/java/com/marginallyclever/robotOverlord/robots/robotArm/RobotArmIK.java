package com.marginallyclever.robotOverlord.robots.robotArm;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.RobotArmInterface;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * {@link RobotArmIK} is a {@link RobotArmFK} with added Inverse Kinematics.  
 * @see <a href='https://en.wikipedia.org/wiki/Inverse_kinematics'>Inverse Kinematics</a>
 * @author Dan Royer
 * @since 2021-02-24
 */
public class RobotArmIK extends RobotArmFK {
	private static final long serialVersionUID = -7778520191789995554L;

	private PoseEntity eeTarget = new PoseEntity("Target");
	
	public RobotArmIK(String name) {
		super(name);
		addChild(eeTarget);
		setEndEffectorTarget(getEndEffector());
	}
		
	public RobotArmIK() {
		this(RobotArmIK.class.getSimpleName());
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		RobotArmIK b = (RobotArmIK)super.clone();
		b.eeTarget = (PoseEntity)eeTarget.clone();
		
		return b;
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);

		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, getPose());
		drawEndEffectorPathToTarget(gl2);
		gl2.glPopMatrix();
	}
	
	private void drawEndEffectorPathToTarget(GL2 gl2) {
		Matrix4d start = this.getEndEffector();
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
		view.pushStack("IK","Inverse Kinematics");

		ViewElementButton bOpen = view.addButton("Open control panel");
		bOpen.addActionEventListener((evt)-> onOpenAction() );

		ViewElementButton bResetGoto = view.addButton("Reset GoTo");
		bResetGoto.addActionEventListener((evt)-> onResetGotoAction() );

		//ViewElementButton bRunTest = view.addButton("Run test");
		//bRunTest.addPropertyChangeListener((evt) -> onRunTest() );
		view.popStack();
		
		super.getView(view);
	}

	@Deprecated
	@SuppressWarnings("unused")
	private void onRunTest() {
		//testPathCalculation(100,true);
		//testPathCalculation(100,false);
		//testTime(true);
		testTime(false);
	}

	private void onResetGotoAction() {
		setEndEffectorTarget(this.getEndEffector());
	}

	private void onOpenAction() {
		JFrame parent = null;
		
		Entity e = this.getRoot();
		if(e instanceof RobotOverlord) {
			parent = ((RobotOverlord)e).getMainFrame();
		}
		
		final RobotArmIK me = this;
		final JFrame parentFrame = parent;
		
        new Thread(new Runnable() {
            @Override
			public void run() {
            	JDialog frame = new JDialog(parentFrame,getName());
        		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        		frame.add(new RobotArmInterface(me));
        		frame.pack();
        		frame.setVisible(true);
            }
        }).start();
	}

	@Deprecated
	@SuppressWarnings("unused")
	private void testPathCalculation(double STEPS,boolean useExact) {
		double [] jOriginal = this.getAngles();
		Matrix4d start = this.getEndEffector();
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
	
				double [] jBefore = this.getAngles();

				// move arm towards result to get future pose
				try {
					JacobianNewtonRaphson.step(this);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				double [] jAfter = this.getAngles();

				double [] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(old, interpolated);
				old.set(interpolated);
	
				ApproximateJacobian aj = new ApproximateJacobian(this);
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
		
		this.setAngles(jOriginal);
		
		Matrix4d startCompare = this.getEndEffector();
		if(!startCompare.equals(start)) {
			Log.message("Change!\nS"+start.toString()+"E"+startCompare.toString());
		}

		//updateSliders();
	}
	
	@Deprecated
	private void testTime(boolean useExact) {
		long start = System.nanoTime();

		for(int i=0;i<1000;++i) {
			if(useExact) {
				//getExactJacobian(jacobian);
			} else {
				new ApproximateJacobian(this);
			}
		}
		
		long end = System.nanoTime();
		System.out.println("diff="+((double)(end-start)/1000.0)+(useExact?"exact":"approx"));
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

	public ApproximateJacobian getApproximateJacobian() {
		return new ApproximateJacobian(this);
	}
}
