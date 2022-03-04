package com.marginallyclever.robotOverlord.robots.robotArm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.robotArm.implementations.Sixi3_6axis;

public class RobotArmIKTests {
	@Deprecated
	@SuppressWarnings("unused")
	private void testTime(boolean useExact) {
		RobotArmIK arm = new Sixi3_6axis();
		long start = System.nanoTime();

		for(int i=0;i<1000;++i) {
			if(useExact) {
				//getExactJacobian(jacobian);
			} else {
				new ApproximateJacobian(arm);
			}
		}
		
		long end = System.nanoTime();
		System.out.println("diff="+((double)(end-start)/1000.0)+(useExact?"exact":"approx"));
	}

	@Deprecated
	@SuppressWarnings("unused")
	private void testPathCalculation(double STEPS,boolean useExact) {
		RobotArmIK arm = new Sixi3_6axis();
		double [] jOriginal = arm.getAngles();
		Matrix4d start = arm.getEndEffector();
		
		double [] jStart = arm.getAngles();
		double [] jRandom = new double[jStart.length];
		for(int i=0;i<jStart.length;++i) {
			RobotArmBone b = arm.getBone(i); 
			jRandom[i] = (Math.random() * (b.getAngleMax()-b.getAngleMin())) + b.getAngleMin();
		}
		arm.setAngles(jRandom);
		Matrix4d end = arm.getEndEffector();
		arm.setAngles(jStart);
		
		try(PrintWriter pw = new PrintWriter(new File("test"+((int)STEPS)+"-"+(useExact?"e":"a")+".csv"))) {
			Matrix4d interpolated = new Matrix4d();
			Matrix4d old = new Matrix4d(start);
			//double [] cartesianDistanceCompare = new double[6];

			//pw.print("S"+start.toString()+"E"+end.toString());

			for(double alpha=1;alpha<=STEPS;++alpha) {
				pw.print((int)alpha+"\t");

				MatrixHelper.interpolate(start,end,alpha/STEPS,interpolated);
	
				double [] jBefore = arm.getAngles();

				// move arm towards result to get future pose
				try {
					JacobianNewtonRaphson.step(arm,end);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				double [] jAfter = arm.getAngles();

				double [] cartesianDistance = MatrixHelper.getCartesianBetweenTwoMatrixes(old, interpolated);
				old.set(interpolated);
	
				ApproximateJacobian aj = new ApproximateJacobian(arm);
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		arm.setAngles(jOriginal);
		
		Matrix4d startCompare = arm.getToolCenterPoint();
		if(!startCompare.equals(start)) {
			Log.message("Change!\nS"+start.toString()+"E"+startCompare.toString());
		}
	}
}
