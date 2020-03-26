package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import java.util.Observable;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Sixi2Sim extends Sixi2Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6216095894080620268L;

	public enum InterpolationStyle {
		LINEAR_FK(0,"Linear FK"),
		LINEAR_IK(1,"Linear IK"),
		JACOBIAN(2,"Jacobian IK");
		
		private int number;
		private String name;
		private InterpolationStyle(int n,String s) {
			number=n;
			name=s;
		}
		final public int toInt() {
			return number;
		}
		final public String toString() {
			return name;
		}
		static public String [] getAll() {
			InterpolationStyle[] allModes = InterpolationStyle.values();
			String[] labels = new String[allModes.length];
			for(int i=0;i<labels.length;++i) {
				labels[i] = allModes[i].toString();
			}
			return labels;
		}
	};

	protected IntEntity interpolationStyle = new IntEntity("Interpolation",InterpolationStyle.JACOBIAN.toInt());
	
	protected double timeTarget;
	protected double timeStart;
	protected double timeNow;
	
	// fk interpolation
	protected double [] poseFKTarget;
	protected double [] poseFKStart;
	protected double [] poseFKNow;
	
	// ik interpolation
	protected Matrix4d mLive = new Matrix4d();
	protected Matrix4d mFrom = new Matrix4d();
	protected Matrix4d mTarget = new Matrix4d();

	protected DHRobotEntity robot;
	
	
	public Sixi2Sim() {
		super();
		setName("Sim");
		
		for(DHLink link : links ) {
			link.setDHRobot(this);
		}
		endEffector.setDHRobot(this);
		
		int numAdjustableLinks = links.size();
		poseFKTarget = new double[numAdjustableLinks];
		poseFKStart = new double[numAdjustableLinks];
		poseFKNow = new double[numAdjustableLinks];
		
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
				case 90: relativeMode=false;	break;
				case 91: relativeMode=true;    break;
				default:  break;
				}
			}			
		}
		
		if(gMode==0) {
			// linear move

			int i=0;
			for( DHLink link : links ) {
				if(link.flags == LinkAdjust.NONE) continue;
				
				poseFKNow[i] = link.getAdjustableValue();
				poseFKTarget[i] = poseFKNow[i];
				
				for( String t : tok ) {
					String letter = t.substring(0,1); 
					if(link.getLetter().equalsIgnoreCase(letter)) {
						//System.out.println("link "+link.getLetter()+" matches "+letter);
						poseFKTarget[i] = Double.parseDouble(t.substring(1));
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
			for(i=0; i<poseFKNow.length; ++i) {
				poseFKStart[i] = poseFKNow[i];
				double dAbs = Math.abs(poseFKTarget[i] - poseFKStart[i]);
				dp+=dAbs;
				if(dMax<dAbs) dMax=dAbs;
			}
	        if(dp==0) return;
	        
	        // set the live and from matrixes
	        mLive.set(endEffector.getPoseWorld());
	        mFrom.set(mLive);
	        
	        // get the target matrix
	        DHKeyframe oldPose = solver.createDHKeyframe();
	        getPoseFK(oldPose);
		        DHKeyframe newPose = solver.createDHKeyframe();
		        newPose.set(poseFKTarget);
		        setPoseFK(newPose);
		        mTarget.set(endEffector.getPoseWorld());
	        setPoseFK(oldPose);


	        double travelS = dMax/(double)feedRate.get();
	        
	        timeNow=timeStart=0;
	        timeTarget=timeStart+travelS;
	        
	        // wait for reply
	        readyForCommands=false;
		} else if(gMode==4) {
			// dwell
			double dwellTimeS=0;
			for( String t : tok ) {
				if(t.startsWith("P")) {
					dwellTimeS+=Double.parseDouble(t.substring(1))*0.001;
				}
				if(t.startsWith("S")) {
					dwellTimeS+=Double.parseDouble(t.substring(1));
				}
			}
	        timeStart=0;
	        timeTarget=timeStart+dwellTimeS;
		}
	}

	@Override 
	public void update(double dt) {
		int style = (int)interpolationStyle.get(); 
		     if(InterpolationStyle.LINEAR_FK.toInt()==style) interpolateLinearFK(dt);
		else if(InterpolationStyle.LINEAR_IK.toInt()==style) interpolateLinearIK(dt);
		else if(InterpolationStyle.JACOBIAN .toInt()==style) interpolateJacobian(dt);
		
		super.update(dt);
	}
	
	@Override
	public void update(Observable obs, Object obj) {
		if(obs == endEffector.poseWorld) {
			setPoseIK(endEffector.getPoseWorld());
		}
	}

	protected void interpolateLinearFK(double dt) {
		double tTotalS = timeTarget - timeStart;
		timeNow += dt;
	    double t = timeNow-timeStart;

	    if(t>=0 && t<=tTotalS) {
	    	// linear interpolation of movement
	    	double tFraction = t/tTotalS;

	    	int i=0;
	    	for( DHLink n : links ) {
	    		if( n.getName()==null ) continue;
	    		n.setAdjustableValue((poseFKTarget[i] - poseFKStart[i]) * tFraction + poseFKStart[i]);
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
		double tTotalS = timeTarget - timeStart;
		timeNow += dt;
	    double t = timeNow-timeStart;

	    if(t>=0 && t<=tTotalS) {
	    	// linear interpolation of movement
	    	double tFraction = t/tTotalS;
	    	
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
		Matrix4d T = endEffector.getPoseWorld();
		
		DHKeyframe newPoseFK = getIKSolver().createDHKeyframe();
		int i=0;
		// for all adjustable joints
		for( DHLink link : links ) {
			if(link.flags == LinkAdjust.NONE) continue;
			
			// use anglesB to get the hand matrix after a tiny adjustment on one joint.
			newPoseFK.set(keyframe);
			newPoseFK.fkValues[i]+=ANGLE_STEP_SIZE_DEGREES;
			setPoseFK(newPoseFK);
			Matrix4d Tnew = endEffector.getPoseWorld();
			
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
	 * Interpolate between two matrixes using approximate jacobians and update forward kinematics while you're at it.
	 * 
 	 * caution: assumes FK pose at start of interpolation is sane.
	 * 
	 * @param dt size of step this, in seconds.
	 */
	protected void interpolateJacobian(double dt) {
		if(timeTarget == timeStart) {
	    	// nothing happening
			readyForCommands=true;
			return;
		}
		
    	double tTotal = timeTarget - timeStart;
		timeNow += dt;
	    double t = timeNow-timeStart;
	    
		double ratioNow    = (t   ) / tTotal;
		double ratioFuture = (t+dt) / tTotal;
		if(ratioNow   >1) ratioNow   =1;
		if(ratioFuture>1) ratioFuture=1;
		
		if(ratioFuture==1 && ratioNow==1) {
	    	// nothing happening
			readyForCommands=true;
			return;
		}
		
		// changing the end matrix will only move the simulated version of the "live"
		// robot.
		Matrix4d interpolatedMatrixNow = new Matrix4d();
		Matrix4d interpolatedMatrixFuture = new Matrix4d();
		MatrixHelper.interpolate(mFrom,mTarget, ratioNow   , interpolatedMatrixNow);
		MatrixHelper.interpolate(mFrom,mTarget, ratioFuture, interpolatedMatrixFuture);

		// get the translation force
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		Vector3d dp = new Vector3d();
		interpolatedMatrixNow.get(p0);
		interpolatedMatrixFuture.get(p1);
		dp.sub(p1,p0);
		dp.scale(1.0/dt);
		
		// get the rotation force
		Quat4d q0 = new Quat4d();
		Quat4d q1 = new Quat4d();
		Quat4d dq = new Quat4d();
		interpolatedMatrixNow.get(q0);
		interpolatedMatrixFuture.get(q1);
		dq.sub(q1,q0);
		dq.scale(2/dt);
		Quat4d w = new Quat4d();
		w.mulInverse(dq,q0);
		
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
				mLive.set(endEffector.getPoseWorld());
				System.out.println("ok");
			} else {
				System.out.println("bad");
			}
		}
	}

	@Override
	public void setPoseWorld(Matrix4d m) {}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Ss", "Sixi Sim");
		view.addComboBox(interpolationStyle, InterpolationStyle.getAll());
		view.popStack();
		super.getView(view);
	}
}
