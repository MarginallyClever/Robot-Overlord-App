package com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.sixi2;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.dhLink.DHLink;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.dhLink.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.sixi2.Sixi2.ControlMode;

public class Sixi2Sim extends Sixi2Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6216095894080620268L;

	public enum InterpolationStyle {
		LINEAR_FK(0,"LINEAR_FK"),
		LINEAR_IK(1,"LINEAR_IK"),
		JACOBIAN(2,"JACOBIAN");
		
		private int number;
		private String name;
		private InterpolationStyle(int n,String s) {
			number=n;
			name=s;
		}
		public int toInt() {
			return number;
		}
		public String toString() {
			return name;
		}
		static public String [] getAll() {
			ControlMode[] allModes = ControlMode.values();
			String[] labels = new String[allModes.length];
			for(int i=0;i<labels.length;++i) {
				labels[i] = allModes[i].toString();
			}
			return labels;
		}
	};

	protected InterpolationStyle interpolationStyle = InterpolationStyle.LINEAR_FK;
	protected long arrivalTime;
	protected long startTime;
	
	// fk interpolation
	protected double [] poseTargetFK;
	protected double [] poseFromFK;
	protected double [] poseLiveFK;
	
	// ik interpolation
	protected Matrix4d mLive = new Matrix4d();
	protected Matrix4d mFrom = new Matrix4d();
	protected Matrix4d mTarget = new Matrix4d();
	  
	public Sixi2Sim() {
		super();
		setName("Sim");

		int numAdjustableLinks = 0;
		for(DHLink link : links ) {
			if(link.flags != LinkAdjust.NONE) numAdjustableLinks++;
		}
		poseTargetFK = new double[numAdjustableLinks];
		poseFromFK = new double[numAdjustableLinks];
		poseLiveFK = new double[numAdjustableLinks];
		
	    readyForCommands=true;
	    
	    // set blue
	    for( DHLink link : links ) {
	    	link.getMaterial().setDiffuseColor(113f/255f, 211f/255f, 226f/255f,1.0f);
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

			int i=0;
			for( DHLink link : links ) {
				if(link.flags == LinkAdjust.NONE) continue;
				
				poseLiveFK[i] = link.getAdjustableValue();
				poseTargetFK[i] = poseLiveFK[i];
				
				for( String t : tok ) {
					String letter = t.substring(0,1); 
					if(link.getLetter().equalsIgnoreCase(letter)) {
						//System.out.println("link "+link.getLetter()+" matches "+letter);
						poseTargetFK[i] = Double.parseDouble(t.substring(1));
					}
				}
				++i;
			}
			
			for( String t : tok ) {
				String letter = t.substring(0,1); 
				if(letter.equalsIgnoreCase("F")) {
					feedRate.set(Double.parseDouble(t.substring(1)));
				} else if(letter.equalsIgnoreCase("A")) {
					acceleration.set(Double.parseDouble(t.substring(1)));
				}
			}

			
			if(dhTool!=null) {
				dhTool.sendCommand(command);
			}
		
			double dMax=0;
	        double dp=0;
			for(i=0; i<poseLiveFK.length; ++i) {
				poseFromFK[i] = poseLiveFK[i];
				double dAbs = Math.abs(poseTargetFK[i] - poseFromFK[i]);
				dp+=dAbs;
				if(dMax<dAbs) dMax=dAbs;
			}
	        if(dp==0) return;
	        
	        double travelS = dMax/feedRate.get();
	        long travelMs = (long)Math.ceil(travelS*1000.0);
	        
	        // set the live and from matrixes
	        mLive.set(this.getEndEffectorMatrix());
	        mFrom.set(mLive);
	        
	        // get the target matrix
	        DHKeyframe oldPose = solver.createDHKeyframe();
	        getPoseFK(oldPose);
		        DHKeyframe newPose = solver.createDHKeyframe();
		        newPose.set(poseTargetFK);
		        setPoseFK(newPose);
		        mTarget.set(getEndEffectorMatrix());
	        setPoseFK(oldPose);

	        // start the clock
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

	@Override 
	public void update(double dt) {
		// TODO handle this better
		interpolationStyle = InterpolationStyle.JACOBIAN;
		
		switch (interpolationStyle) {
		case LINEAR_FK:	interpolateLinearFK(dt);	break;
		case LINEAR_IK:	interpolateLinearIK(dt);	break;
		case JACOBIAN:	interpolateJacobian(dt);	break;
		}
	}

	protected void interpolateLinearFK(double dt) {
		double tTotal = arrivalTime - startTime;
    	double tNow = System.currentTimeMillis();
	    double t = tNow-startTime;

	    if(t>=0 && t<=tTotal) {
	    	// linear interpolation of movement
	    	double tFraction = t/tTotal;

	    	int i=0;
	    	for( DHLink n : links ) {
	    		if( n.getName()==null ) continue;
	    		n.setAdjustableValue((poseTargetFK[i] - poseFromFK[i]) * tFraction + poseFromFK[i]);
	    		++i;
	    	}
	    } else {
	    	// nothing happening
	    	readyForCommands=true;
	    }
	}
	
	/**
	 * interpolation between two matrixes linearly, and update kinematics.
	 * @param dt change in seconds.
	 */
	protected void interpolateLinearIK(double dt) {	
		double tTotal = arrivalTime - startTime;
    	double tNow = System.currentTimeMillis();
	    double t = tNow-startTime;

	    if(t>=0 && t<=tTotal) {
	    	// linear interpolation of movement
	    	double tFraction = t/tTotal;
	    	
			MatrixHelper.interpolate(
					mFrom, 
					mTarget, 
					tFraction, 
					mLive);
			setPoseIK(mLive);
	    } else {
	    	// nothing happening
	    	readyForCommands=true;
	    }
	}
	
	/**
	 * Use Forward Kinematics to approximate the Jacobian matrix for Sixi.
	 * See also https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
	 */
	public double [][] approximateJacobian(DHKeyframe keyframe) {
		double [][] jacobian = new double[6][6];
		
		double ANGLE_STEP_SIZE_DEGREES=0.5;  // degrees
		
		DHKeyframe oldPoseFK = getIKSolver().createDHKeyframe();
		getPoseFK(oldPoseFK);
		
		setPoseFK(keyframe);
		Matrix4d T = new Matrix4d(getEndEffectorMatrix());
		
		DHKeyframe newPoseFK = getIKSolver().createDHKeyframe();
		int i=0;
		// for all adjustable joints
		for( DHLink link : links ) {
			if(link.flags == LinkAdjust.NONE) continue;
			
			// use anglesB to get the hand matrix after a tiny adjustment on one joint.
			newPoseFK.set(keyframe);
			newPoseFK.fkValues[i]+=ANGLE_STEP_SIZE_DEGREES;
			setPoseFK(newPoseFK);
			Matrix4d Tnew = new Matrix4d(getEndEffectorMatrix());
			
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
			
			++i;
		}

		// undo our changes.
		setPoseFK(oldPoseFK);
		
		return jacobian;
	}

	/**
	 * interpolation between two matrixes using jacobians, and update kinematics
	 * while you're at it.
	 * 
	 * @param dt
	 */
	protected void interpolateJacobian(double dt) {
		if(arrivalTime == startTime) {
	    	// nothing happening
			readyForCommands=true;
			return;
		}
		
    	double tTotal = arrivalTime - startTime;
    	double tNow = System.currentTimeMillis();
	    double t = tNow-startTime;
	    
		double ratio0 = (t        ) / tTotal;
		double ratio1 = (t+dt*1000) / tTotal;
		if(ratio0>1) ratio0=1;
		if(ratio1>1) ratio1=1;
		
		if(ratio1==1 && ratio0==1) {
	    	// nothing happening
			readyForCommands=true;
			return;
		}
		
		// changing the end matrix will only move the simulated version of the "live"
		// robot.
		Matrix4d interpolatedMatrix0 = new Matrix4d();
		Matrix4d interpolatedMatrix1 = new Matrix4d();
		MatrixHelper.interpolate(mFrom,mTarget, ratio0, interpolatedMatrix0);
		MatrixHelper.interpolate(mFrom,mTarget, ratio1, interpolatedMatrix1);

		
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
		
		// caution: assumes current pose is sane.
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
					System.out.print(StringHelper.formatDouble(v)+"\t");
					
					v = MathHelper.capRotationDegrees(v,0);
					keyframe.fkValues[j]=v;
				}
			}
			if (sanityCheck(keyframe)) {
				setPoseFK(keyframe);
				mLive.set(getEndEffectorMatrix());
				System.out.println("ok");
			} else {
				System.out.println("bad");
			}
		}
	}
}
