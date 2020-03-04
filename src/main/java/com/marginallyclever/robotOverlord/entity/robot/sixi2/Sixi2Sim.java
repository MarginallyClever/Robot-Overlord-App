package com.marginallyclever.robotOverlord.entity.robot.sixi2;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHKeyframe;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;

public class Sixi2Sim extends Sixi2Model {
	public enum InterpolationStyle {
		LINEAR_FK("LINEAR_FK"),
		LINEAR_IK("LINEAR_IK"),
		JACOBIAN("JACOBIAN");
		
		private String styleName;
		private InterpolationStyle(String s) {
			styleName=s;
		}
		public String toString() {
			return styleName;
		}
	};
	
	protected long arrivalTime;
	protected long startTime;
	
	// fk interpolation
	protected double [] poseTarget;
	protected double [] poseFrom;
	protected double [] poseLive;
	
	// ik interpolation
	protected Matrix4d mTarget;
	protected Matrix4d mFrom;
	protected Matrix4d mLive;
	

	protected DHKeyframe homeKey;
	  
	public Sixi2Sim() {
		super();
		setName("Sim");
		
		poseTarget = new double[6];
		poseFrom = new double[6];
		poseLive = new double[6];
		
	    readyForCommands=true;
	    
	    // set blue
	    for( DHLink link : links ) {
	    	link.getMaterial().setDiffuseColor(113f/255f, 211f/255f, 226f/255f,0.75f);
	    }

	    // the home position
		homeKey = getIKSolver().createDHKeyframe();
		homeKey.fkValues[0]=0;
		homeKey.fkValues[1]=0;
		homeKey.fkValues[2]=0;
		homeKey.fkValues[3]=0;
		homeKey.fkValues[4]=20;
		homeKey.fkValues[5]=0;
		setPoseFK(homeKey);
	}

	@Override 
	public void update(double dt) {
		/*
		if(connection!=null && connection.isOpen()) {
			// do not simulate movement when connected to a live robot.
		} else {
			if(interpolator.isPlaying()) 
			{
				interpolator.update(dt,live.getEndEffectorMatrix());
				InterpolationStyle style = InterpolationStyle.LINEAR;
				switch (style) {
				case Sixi2.LINEAR_FK:	interpolateLinearFK(dt);	break;
				case Sixi2.LINEAR_IK:	interpolateLinearIK(dt);	break;
				case Sixi2.JACOBIAN:	interpolateJacobian(dt);	break;
				}
				if (live.dhTool != null) {
					live.dhTool.interpolate(dt);
				}
				
				if(sixi2Panel!=null && !sixi2Panel.scrubberLock.isLocked()) {
					sixi2Panel.setScrubHead(10*(int)interpolator.getPlayHead());
				}
				if(!interpolator.isPlaying()) {
					// must have just ended this frame
					if(sixi2Panel!=null) {
						sixi2Panel.stop();
					}
				} else {
					
				}
			}
		}
		 */
	    long tNow=System.currentTimeMillis();
	    long dtms = arrivalTime - tNow;

	    if(dtms>=0) {
	    	// linear interpolation of movement
	    	float tTotal = arrivalTime - startTime;
	    	float tPassed = tNow - startTime;
	    	float tFraction = tPassed / tTotal;

	    	int i=0;
	    	for( DHLink n : links ) {
	    		if( n.getName()==null ) continue;
	    		n.setAdjustableValue((poseTarget[i] - poseFrom[i]) * tFraction + poseFrom[i]);
	    		++i;
	    	}
	    } else {
	    	// nothing happening
	    	// all stop
	    	/*
	    	int i=0;
	    	for( DHLink n : links ) {
	    		if( n.getName()==null ) continue;
	    		poseLive[i] = poseTarget[i];
	    		n.setAdjustableValue(poseTarget[i]);
	    	}*/
	    	// ready for new command
	    	readyForCommands=true;
	    }
	}

	@Override 
	public void sendCommand(String command) {
		if(command==null) return;  // no more commands.

		// parse the command and update the model immediately.
		String [] tok = command.split("\\s+");
		for( String t : tok ) {
			if( t.startsWith("G")) {
				int newGMode = Integer.parseInt(t.substring(1));
				switch(newGMode) {
				case 0: gMode=0;	break;  // move
				case 1: gMode=1;	break;  // rapid
				case 2: gMode=2;	break;  // arc cw
				case 3: gMode=3;	break;  // arc ccw
				case 4: gMode=4;	break;  // dwell
				case 90: relativeMode=false;					break;
				case 91: relativeMode=true;					    break;
				default:  break;
				}
			}			
		}
		
		if(gMode==0) {
			// linear move
			Vector3d t1 = new Vector3d();
			Matrix3d m1 = new Matrix3d();
			getEndEffectorMatrix().get(m1,t1);
			Vector3d e1 = MatrixHelper.matrixToEuler(m1);
			boolean isDirty=false;

			for( String t : tok ) {
				switch(t.charAt(0)) {
				case 'X':  isDirty=true;  t1.x = Double.parseDouble(t.substring(1));  break;
				case 'Y':  isDirty=true;  t1.y = Double.parseDouble(t.substring(1));  break;
				case 'Z':  isDirty=true;  t1.z = Double.parseDouble(t.substring(1));  break;
				//case 'I':  isDirty=true;  e1.x = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				//case 'J':  isDirty=true;  e1.y = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				//case 'K':  isDirty=true;  e1.z = Math.toRadians(Double.parseDouble(token.substring(1)));  break;
				case 'F':  isDirty=true;  feedRate = Double.parseDouble(t.substring(1));  break;
				case 'A':  isDirty=true;  acceleration = Double.parseDouble(t.substring(1));  break;
				default:  break;
				}
			}

			
			if(dhTool!=null) {
				dhTool.parseGCode(command);
			}
			
			if(isDirty) {
				// changing the target pose of the ghost
				m1 = MatrixHelper.eulerToMatrix(e1);
				Matrix4d m=new Matrix4d();
				m.set(m1);
				m.setTranslation(t1);
				
				if(setPoseIK(m)) {
					//addInterpolation(feedRate);
				}
			}
		
			double dMax=0;
	        double dp=0;
			for(int i=0; i<poseLive.length; ++i ) {
				poseFrom[i] = poseLive[i];
				double dAbs = Math.abs(poseTarget[i] - poseFrom[i]);
				dp+=dAbs;
				if(dMax<dAbs) dMax=dAbs;
			}
	        if(dp==0) return;
	        
	        double travelS = dMax/feedRate;
	        long travelMs = (long)Math.ceil(travelS*1000.0);
	        startTime=System.currentTimeMillis();
	        arrivalTime=startTime+travelMs;
	        
	        // wait for reply
	        readyForCommands=false;
		}
		if(gMode==4) {
			// dwell
			float dwellTimeMs=0;
			for( String t : tok ) {
				if(t.startsWith("P")) {
					dwellTimeMs+=Double.parseDouble(t.substring(1));
				}
				if(t.startsWith("S")) {
					dwellTimeMs+=Double.parseDouble(t.substring(1))*1000;
				}
			}
	        startTime=System.currentTimeMillis();
	        long travelMs = (long)Math.ceil(dwellTimeMs);
	        arrivalTime=startTime+travelMs;
		}
	}
	

	/**
	 * Use Forward Kinematics to approximate the Jacobian matrix for Sixi.
	 * See also https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
	 */
	public double [][] approximateJacobian(DHKeyframe keyframe) {
		double [][] jacobian = new double[6][6];
		
		double ANGLE_STEP_SIZE_DEGREES=0.5;  // degrees
		
		DHKeyframe keyframe2 = getIKSolver().createDHKeyframe();

		// use anglesA to get the hand matrix
		DHRobot clone = new DHRobot(this);
		clone.setPoseFK(keyframe);
		Matrix4d T = new Matrix4d(clone.getEndEffectorMatrix());
		
		// for all joints
		int i,j;
		for(i=0;i<6;++i) {
			// use anglesB to get the hand matrix after a tiiiiny adjustment on one joint.
			for(j=0;j<6;++j) {
				keyframe2.fkValues[j]=keyframe.fkValues[j];
			}
			keyframe2.fkValues[i]+=ANGLE_STEP_SIZE_DEGREES;

			clone.setPoseFK(keyframe2);
			Matrix4d Tnew = new Matrix4d(clone.getEndEffectorMatrix());
			
			// use the finite difference in the two matrixes
			// aka the approximate the rate of change (aka the integral, aka the velocity)
			// in one column of the jacobian matrix at this position.
			Matrix4d dT = new Matrix4d();
			dT.sub(Tnew,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));
			
			jacobian[i][0]=dT.m03;
			jacobian[i][1]=dT.m13;
			jacobian[i][2]=dT.m23;


			// find the rotation part
			// these initialT and initialTd were found in the comments on
			// https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
			// and used to confirm that our skew-symmetric matrix match theirs.
			/*
			double[] initialT = {
					 0,  0   , 1   ,  0.5963,
					 0,  1   , 0   , -0.1501,
					-1,  0   , 0   , -0.01435,
					 0,  0   , 0   ,  1 };
			double[] initialTd = {
					 0, -0.01, 1   ,  0.5978,
					 0,  1   , 0.01, -0.1441,
					-1,  0   , 0   , -0.01435,
					 0,  0   , 0   ,  1 };
			T.set(initialT);
			Td.set(initialTd);
			dT.sub(Td,T);
			dT.mul(1.0/Math.toRadians(ANGLE_STEP_SIZE_DEGREES));//*/
			
			//System.out.println("T="+T);
			//System.out.println("Td="+Td);
			//System.out.println("dT="+dT);
			Matrix3d T3 = new Matrix3d(
					T.m00,T.m01,T.m02,
					T.m10,T.m11,T.m12,
					T.m20,T.m21,T.m22);
			//System.out.println("R="+R);
			Matrix3d dT3 = new Matrix3d(
					dT.m00,dT.m01,dT.m02,
					dT.m10,dT.m11,dT.m12,
					dT.m20,dT.m21,dT.m22);
			//System.out.println("dT3="+dT3);
			Matrix3d skewSymmetric = new Matrix3d();
			
			T3.transpose();  // inverse of a rotation matrix is its transpose
			skewSymmetric.mul(dT3,T3);
			
			//System.out.println("SS="+skewSymmetric);
			//[  0 -Wz  Wy]
			//[ Wz   0 -Wx]
			//[-Wy  Wx   0]
			
			jacobian[i][3]=skewSymmetric.m12;
			jacobian[i][4]=skewSymmetric.m20;
			jacobian[i][5]=skewSymmetric.m01;
		}
		
		return jacobian;
	}


	/**
	 * interpolation between two matrixes linearly, and update kinematics.
	 * @param dt change in seconds.
	 */
	protected void interpolateLinear(double dt) {			
		// changing the end matrix will only move the simulated version of the "live"
		// robot.

	    long tNow=System.currentTimeMillis();
	    
		double total = arrivalTime-startTime;
		double t = tNow-startTime;
		double ratio = total>0? t/total : 0;
		MatrixHelper.interpolate(
				mFrom, 
				mTarget, 
				ratio, 
				mLive);
		setPoseIK(mLive);
	}

	/**
	 * interpolation between two matrixes using jacobians, and update kinematics
	 * while you're at it.
	 * 
	 * @param dt
	 */
	protected void interpolateJacobian(double dt) {
		double total = arrivalTime-startTime;
		
		if(total==0) {
			return;
		}
	    long tNow=System.currentTimeMillis();
		double t = tNow-startTime;
		double ratio0 = (t   ) / total;
		double ratio1 = (t+dt) / total;
		if(ratio1>1) ratio1=1;
		
		// changing the end matrix will only move the simulated version of the "live"
		// robot.
		Matrix4d interpolatedMatrix0 = new Matrix4d();
		Matrix4d interpolatedMatrix1 = new Matrix4d();
		MatrixHelper.interpolate(mFrom,mTarget, ratio0, interpolatedMatrix0);
		MatrixHelper.interpolate(mFrom,mTarget, ratio1, interpolatedMatrix1);

		mLive.set(interpolatedMatrix1);
		
		// get the translation force
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		Vector3d dp = new Vector3d();
		interpolatedMatrix0.get(p0);
		interpolatedMatrix1.get(p1);
		dp.sub(p1,p0);
		dp.scale(1.0/dt);
		
		// get the rotation force
		Quat4d q0 = new Quat4d();
		Quat4d q1 = new Quat4d();
		Quat4d dq = new Quat4d();
		interpolatedMatrix0.get(q0);
		interpolatedMatrix1.get(q1);
		dq.sub(q1,q0);
		dq.scale(2/dt);
		Quat4d w = new Quat4d();
		w.mulInverse(dq,q0);
		
		// assumes live is at a sane solution.
		DHKeyframe keyframe = getIKSolver().createDHKeyframe();
		getPoseFK(keyframe);
		double[][] jacobian = approximateJacobian(keyframe);
		double[][] inverseJacobian = MatrixHelper.invert(jacobian);
		double[] force = { dp.x,dp.y,dp.z, -w.x,-w.y,-w.z };

		double df = Math.sqrt(
				force[0] * force[0] + 
				force[1] * force[1] + 
				force[2] * force[2] +
				force[3] * force[3] +
				force[4] * force[4] +
				force[5] * force[5]);
		if (df > 0.01) {
			double[] jvot = new double[6];
			int j, k;
			for (j = 0; j < 6; ++j) {
				for (k = 0; k < 6; ++k) {
					jvot[j] += inverseJacobian[k][j] * force[k];
				}
				if (!Double.isNaN(jvot[j])) {
					// simulate a change in the joint velocities
					double v = keyframe.fkValues[j] + Math.toDegrees(jvot[j]) * dt;
					v = MathHelper.capRotationDegrees(v,0);
					keyframe.fkValues[j]=v;
					System.out.print(StringHelper.formatDouble(v)+"\t");
				}
			}
			if (sanityCheck(keyframe)) {
				setPoseFK(keyframe);
				System.out.print("ok");
			} else {
				System.out.print("bad");
			}
			System.out.println();
		}
	}
	
	public void goHome() {
		setPoseFK(homeKey);
	}
}
