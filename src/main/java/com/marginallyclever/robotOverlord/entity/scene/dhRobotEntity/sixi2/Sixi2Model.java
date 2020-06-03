package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_GradientDescent;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Contains the setup of the DHLinks for a DHRobot.
 * TODO Could read these values from a text file.
 * TODO Could have an interactive setup option - dh parameter design app?
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public abstract class Sixi2Model extends DHRobotEntity {	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7341226486087376506L;

	// set this to false before running the app and the model will not attach to the DHLinks.
	// this is convenient for setting up the DHLinks with less visual confusion.
	// TODO phase out when dhRobotBuilder matures?
	static final boolean ATTACH_MODELS=true;

	// last known state
	protected boolean readyForCommands=false;
	protected boolean relativeMode=false;
	protected int gMode=0;
	
	public DoubleEntity feedRate = new DoubleEntity("Feedrate",25.0);
	public DoubleEntity acceleration = new DoubleEntity("Acceleration",5.0);
	public DHLink endEffector = new Sixi2LinearGripper();
	public PoseEntity endEffectorTarget = new PoseEntity();

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

	protected double[] cartesianForceDesired = {0,0,0,0,0,0};
	protected double[] jointVelocityDesired;
	
	public Sixi2Model() {
		super();
		setName("Sixi2Model");
		addChild(feedRate);
		addChild(acceleration);

		//this.setIKSolver(new DHIKSolver_RTTRTR());
		this.setIKSolver(new DHIKSolver_GradientDescent());

		ModelEntity base = new ModelEntity();
		addChild(base);
		base.setName("Base");
		base.setModelFilename("/Sixi2/anchor.obj");
		base.getMaterial().setTextureFilename("/Sixi2/sixi.png");
		base.setModelOrigin(0, 0, 0.9);

		// setup children
		this.setNumLinks(6);

		if(!ATTACH_MODELS) {
			ModelEntity part1 = new ModelEntity();	addChild(part1);	part1.setModelFilename("/Sixi2/shoulder.obj");
			ModelEntity part2 = new ModelEntity();	addChild(part2);	part2.setModelFilename("/Sixi2/bicep.obj");
			ModelEntity part3 = new ModelEntity();	addChild(part3);	part3.setModelFilename("/Sixi2/forearm.obj");
			ModelEntity part4 = new ModelEntity();	addChild(part4);	part4.setModelFilename("/Sixi2/tuningFork.obj");
			ModelEntity part5 = new ModelEntity();	addChild(part5);	part5.setModelFilename("/Sixi2/picassoBox.obj");
			ModelEntity part6 = new ModelEntity();	addChild(part6);	part6.setModelFilename("/Sixi2/hand.obj");
		} else {
			links.get(0).setModelFilename("/Sixi2/shoulder.obj");
			links.get(1).setModelFilename("/Sixi2/bicep.obj");
			links.get(2).setModelFilename("/Sixi2/forearm.obj");
			links.get(3).setModelFilename("/Sixi2/tuningFork.obj");
			links.get(4).setModelFilename("/Sixi2/picassoBox.obj");
			links.get(5).setModelFilename("/Sixi2/hand.obj");
		}
		
		// pan shoulder
		links.get(0).setLetter("X");
		links.get(0).setD(18.8452+0.9);
		links.get(0).setTheta(0);
		links.get(0).setR(0);
		links.get(0).setAlpha(-90);
		links.get(0).setRange(-120,120);
		links.get(0).maxTorque.set(14.0); //Nm
		
		// tilt shoulder
		links.get(1).setLetter("Y");
		links.get(1).setD(0);
		links.get(1).setTheta(-90);
		links.get(1).setR(35.796);
		links.get(1).setAlpha(0);
		links.get(1).setRange(-170,0);
		links.get(1).maxTorque.set(40.0); //Nm

		// tilt elbow
		links.get(2).setLetter("Z");
		links.get(2).setD(0);
		links.get(2).setTheta(0);
		links.get(2).setR(6.4259);
		links.get(2).setAlpha(-90);
		links.get(2).setRange(-83.369, 86);
		links.get(2).maxTorque.set(14.0); //Nm
	
		// roll ulna
		links.get(3).setLetter("U");
		links.get(3).setD(29.355+9.35);
		links.get(3).setTheta(0);
		links.get(3).setR(0);
		links.get(3).setAlpha(90);
		links.get(3).setRange(-175, 175);
		links.get(3).maxTorque.set(3.0); //Nm
	
		// tilt picassoBox
		links.get(4).setLetter("V");
		links.get(4).setD(0);
		links.get(4).setTheta(0);
		links.get(4).setR(0);
		links.get(4).setAlpha(-90);
		links.get(4).setRange(-120, 120);
		links.get(4).maxTorque.set(2.5); //Nm
	
		// roll hand
		links.get(5).setLetter("W");
		links.get(5).setTheta(0);
		links.get(5).setD(5.795);
		links.get(5).setR(0);
		links.get(5).setAlpha(0);
		links.get(5).setRange(-170, 170);
		links.get(5).maxTorque.set(2.5); //Nm
		
		endEffector.setPosition(new Vector3d(0,0,0));
		endEffector.setName("End Effector");
		links.get(links.size()-1).addChild(endEffector);
		
		// update this world pose and all my children's poses all the way down.
		this.updatePoseWorld();
		
		// Use the poseWorld for each DHLink to adjust the model origins.
		for(int i=0;i<links.size();++i) {
			DHLink bone=links.get(i);
			if(bone.getModel()!=null) {
				Matrix4d iWP = bone.getPoseWorld();
				iWP.m23 -= 0.9;
				iWP.invert();
				bone.getModel().adjustMatrix(iWP);
				bone.getMaterial().setTextureFilename("/Sixi2/sixi.png");
			}
		}
		
		goHome();

		endEffectorTarget.setName("End Effector Target");
		addChild(endEffectorTarget);
		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
		
		// interpolation stuff
		int numAdjustableLinks = links.size();
		poseFKTarget = new double[numAdjustableLinks];
		poseFKStart = new double[numAdjustableLinks];
		poseFKNow = new double[numAdjustableLinks];

		jointVelocityDesired = new double [links.size()];

		int i=0;
		for( DHLink link : links ) {
			if(link.flags == LinkAdjust.NONE) continue;
			
			poseFKNow[i] = link.getAdjustableValue();
			poseFKStart[i] = poseFKNow[i];
			poseFKTarget[i] = poseFKNow[i];
			jointVelocityDesired[i]=0;
			++i;
		}
	}
	
	/**
	 * send a command to this model
	 * @param command
	 */
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
						//Log.message("link "+link.getLetter()+" matches "+letter);
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
	        
	        MatrixHelper.normalize3(mTarget);

	        /*String msg="";
	        Vector3d v3 = new Vector3d();
	        mFrom.get(v3);
	        msg+="from="+v3;
	        mTarget.get(v3);
	        msg+="\ttarget="+v3;
	        Log.message(msg);//*/
	        
	        timeNow=timeStart=0;
	        timeTarget=timeStart+travelS;
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

	/**
	 * get the command for this model
	 * @return
	 */
	public String getCommand() {
		String gcode = "G0";
		for( DHLink link : links ) {
			if(!link.getLetter().isEmpty()) {
				gcode += " " + link.getLetter() + StringHelper.formatDouble(link.getAdjustableValue());
			}
		}
		gcode += " F"+getFeedrate();
		gcode += " A"+getAcceleration();
		
		return gcode;
	}

	public void update(double dt) {
		int style = (int)interpolationStyle.get(); 
		     if(InterpolationStyle.LINEAR_FK.toInt()==style) interpolateLinearFK(dt);
		else if(InterpolationStyle.LINEAR_IK.toInt()==style) interpolateLinearIK(dt);
		else if(InterpolationStyle.JACOBIAN .toInt()==style) interpolateJacobian(dt);
		
		super.update(dt);
	}
	
	public double getFeedrate() {
		return (double)feedRate.get();
	}

	public void setFeedRate(double feedrate) {
		this.feedRate.set(feedrate);
	}

	public double getAcceleration() {
		return (double)acceleration.get();
	}

	public void setAcceleration(double acceleration) {
		this.acceleration.set(acceleration);
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
			MatrixHelper.normalize3(mLive);
			setPoseIK(mLive);
	    } else {
	    	// nothing happening
	    	readyForCommands=true;
	    }
	}
	
	/**
	 * Interpolate between two matrixes using approximate jacobians and update forward kinematics while you're at it.
	 * 
 	 * caution: assumes FK pose at start of interpolation is sane.
	 * 
	 * @param dt size of step this, in seconds.
	 */
	protected void interpolateJacobian(double dt) {
		readyForCommands=true;
		
		if(timeTarget == timeStart) {
	    	// nothing happening
			return;
		}
		
		// get interpolated future pose
    	double tTotal = timeTarget - timeStart;
		timeNow += dt;
	    double t = timeNow-timeStart;
	    
		double ratioNow    = (t   ) / tTotal;
		double ratioFuture = (t+dt) / tTotal;
		if(ratioNow   >1) ratioNow   =1;
		if(ratioFuture>1) ratioFuture=1;
		
		Matrix4d interpolatedMatrixNow = new Matrix4d(endEffector.getPoseWorld());
		//MatrixHelper.interpolate(mFrom,mTarget, ratioNow   , interpolatedMatrixNow);
		Matrix4d interpolatedMatrixFuture = new Matrix4d();
		MatrixHelper.interpolate(mFrom,mTarget, ratioFuture, interpolatedMatrixFuture);
		
		getCartesianForceBetweenTwoPoses(interpolatedMatrixNow, interpolatedMatrixFuture, dt, cartesianForceDesired);

		DHKeyframe keyframe = getIKSolver().createDHKeyframe();
		getPoseFK(keyframe);
		
		if(!getJointVelocityFromCartesianForce(keyframe,cartesianForceDesired,jointVelocityDesired)) return;
		
		capJointVelocity(jointVelocityDesired);

		//String msg="Jacobian ";
		for(int j = 0; j < keyframe.fkValues.length; ++j) {
			// simulate a change in the joint velocities
			double v = keyframe.fkValues[j] + Math.toDegrees(jointVelocityDesired[j]) * dt;
			
			keyframe.fkValues[j]=v;
			//msg+=StringHelper.formatDouble(Math.toDegrees(jointVelocityDesired[j]))+"\t";
		}

		if (sanityCheck(keyframe)) {
			setPoseFK(keyframe);
			mLive.set(endEffector.getPoseWorld());
			//msg+="ok";
		} else {
			//msg+="insane";
		}
		//Log.message(msg);
	}
	
	
	/**
	 * Scale jointVelocity to within torqueMax of every joint
	 * @param jointVelocity Values will be changed.  Must be same length as DHKeyframe.fkValues.length().
	 */
	protected void capJointVelocity(double[] jointVelocity) {
		double scale=1;
		for(int j = 0; j < jointVelocity.length; ++j) {
			double maxT = links.get(j).maxTorque.get();
			double ajvot = Math.abs(jointVelocityDesired[j]); 
			if( scale > maxT/ajvot ) {
				scale = maxT/ajvot;
			}
		}
		for(int j = 0; j < jointVelocity.length; ++j) {
			jointVelocityDesired[j] *= scale;
		}
	}
	
	
	/**
	 * 
	 * @param mStart matrix of start pose
	 * @param mEnd matrix of end pose
	 * @param dt time scale, seconds
	 * @param cartesianForce 6 doubles that will be filled with the XYZ translation and UVW rotation.
	 * @return true if successful
	 */
	protected boolean getCartesianForceBetweenTwoPoses(Matrix4d mStart,Matrix4d mEnd,double dt,double[] cartesianForce) {
		Vector3d p0 = new Vector3d();
		Vector3d p1 = new Vector3d();
		Vector3d dp = new Vector3d();
		mStart.get(p0);
		mEnd.get(p1);
		dp.sub(p1,p0);
		dp.scale(1.0/dt);

		mStart.setTranslation(new Vector3d(0,0,0));
		mEnd.setTranslation(new Vector3d(0,0,0));
		// get the rotation force
		Quat4d q0 = new Quat4d();
		Quat4d q1 = new Quat4d();
		Quat4d dq = new Quat4d();
		q0.set(mStart);
		q1.set(mEnd);
		dq.sub(q1,q0);
		dq.scale(2/dt);
		Quat4d w = new Quat4d();
		w.mulInverse(dq,q0);
		
		cartesianForce[0]=dp.x;
		cartesianForce[1]=dp.y;
		cartesianForce[2]=dp.z;
		cartesianForce[3]=-w.x;
		cartesianForce[4]=-w.y;
		cartesianForce[5]=-w.z;
		
		return true;
	}
	
	/**
	 * 
	 * @param keyframe the current pose at which to calculate
	 * @param cartesianForce the XYZ translation and UVW rotation forces on the end effector
	 * @param jvot joint velocity over time.  Will be filled with the new velocity
	 * @return false if joint velocities have NaN values
	 */
	protected boolean getJointVelocityFromCartesianForce(DHKeyframe keyframe,double[] cartesianForce,double [] jvot) {
		// jvot = joint velocity over time
		
		double[][] jacobian = approximateJacobian(keyframe);
		double[][] inverseJacobian = MatrixHelper.invert(jacobian);

		int j,k;
		for(j = 0; j < keyframe.fkValues.length; ++j) {
			double sum=0;
			for(k = 0; k < 6; ++k) {
				sum += inverseJacobian[k][j] * cartesianForce[k];
			}
			if(Double.isNaN(jvot[j])) {
				for(k = 0; k < 6; ++k) jvot[k]=0;
				return false;
			}
			sum=MathHelper.wrapRadians(sum);
			jvot[j]=Math.toDegrees(sum);
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param jointVelocity
	 * @return cartesian force calculated
	 */
	public double [] getCartesianForceFromJointVelocity(DHKeyframe keyframe,double [] jointVelocity) {
		double [] cf = new double[6];  // cartesian force calculated
		double[][] jacobian = approximateJacobian(keyframe);

		for( int k=0;k<keyframe.fkValues.length;++k ) {
			for( int j=0;j<6;++j ) {
				cf[j] += jacobian[k][j] * jointVelocity[k];
			}
		}
		return cf;
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
			// Tnew will be different from T because of the changes in setPoseFK().
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
			
			//Log.message("T="+T);
			//Log.message("Td="+Td);
			//Log.message("dT="+dT);
			Matrix3d T3 = new Matrix3d(
					T.m00,T.m01,T.m02,
					T.m10,T.m11,T.m12,
					T.m20,T.m21,T.m22);
			//Log.message("R="+R);
			Matrix3d dT3 = new Matrix3d(
					dT.m00,dT.m01,dT.m02,
					dT.m10,dT.m11,dT.m12,
					dT.m20,dT.m21,dT.m22);
			//Log.message("dT3="+dT3);
			Matrix3d skewSymmetric = new Matrix3d();
			
			T3.transpose();  // inverse of a rotation matrix is its transpose
			skewSymmetric.mul(dT3,T3);
			
			//Log.message("SS="+skewSymmetric);
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

	public void goHome() {
	    // the home position
		DHKeyframe homeKey = getIKSolver().createDHKeyframe();
		homeKey.fkValues[0]=0;
		homeKey.fkValues[1]=-90;
		homeKey.fkValues[2]=0;
		homeKey.fkValues[3]=0;
		homeKey.fkValues[4]=20;
		homeKey.fkValues[5]=0;
		setPoseFK(homeKey);
		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
	}

	public void goRest() {
	    // set rest position
		DHKeyframe restKey = getIKSolver().createDHKeyframe();
		restKey.fkValues[0]=0;
		restKey.fkValues[1]=-60-90;
		restKey.fkValues[2]=85+90;
		restKey.fkValues[3]=0;
		restKey.fkValues[4]=20;
		restKey.fkValues[5]=0;
		setPoseFK(restKey);
		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sm", "Sixi Model");
		view.addComboBox(interpolationStyle, InterpolationStyle.getAll());
		view.addRange(feedRate, 50, 0);
		view.addRange(acceleration,50,0);
		view.popStack();
		super.getView(view);
	}
}
