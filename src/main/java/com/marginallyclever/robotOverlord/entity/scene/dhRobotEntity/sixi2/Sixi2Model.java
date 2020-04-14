package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHKeyframe;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_GradientDescent;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;

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
	// last known state
	protected boolean readyForCommands=false;
	protected boolean relativeMode=false;
	protected int gMode=0;
	
	public DoubleEntity feedRate = new DoubleEntity("Feedrate",25.0);
	public DoubleEntity acceleration = new DoubleEntity("Acceleration",5.0);
	public DHLink endEffector = new Sixi2LinearGripper();

	// set this to false before running the app and the model will not attach to the DHLinks.
	// this is convenient for setting up the DHLinks with less visual confusion.
	static final boolean ATTACH_MODELS=true;
	
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
		}
		
		// pan shoulder
		links.get(0).setLetter("X");
		if(ATTACH_MODELS) links.get(0).setModelFilename("/Sixi2/shoulder.obj");
		links.get(0).setD(18.8452+0.9);
		links.get(0).setTheta(0);
		links.get(0).setR(0);
		links.get(0).setAlpha(-90);
		links.get(0).setRange(-120,120);
		links.get(0).maxTorque.set(14.0); //Nm
		
		// tilt shoulder
		links.get(1).setLetter("Y");
		if(ATTACH_MODELS) links.get(1).setModelFilename("/Sixi2/bicep.obj");
		links.get(1).setD(0);
		links.get(1).setTheta(-90);
		links.get(1).setR(35.796);
		links.get(1).setAlpha(0);
		links.get(1).setRange(-170,0);
		links.get(1).maxTorque.set(40.0); //Nm

		// tilt elbow
		links.get(2).setLetter("Z");
		if(ATTACH_MODELS) links.get(2).setModelFilename("/Sixi2/forearm.obj");
		links.get(2).setD(0);
		links.get(2).setTheta(0);
		links.get(2).setR(6.4259);
		links.get(2).setAlpha(-90);
		links.get(2).setRange(-83.369, 86);
		links.get(2).maxTorque.set(14.0); //Nm
	
		// roll ulna
		links.get(3).setLetter("U");
		if(ATTACH_MODELS) links.get(3).setModelFilename("/Sixi2/tuningFork.obj");
		links.get(3).setD(29.355+9.35);
		links.get(3).setTheta(0);
		links.get(3).setR(0);
		links.get(3).setAlpha(90);
		links.get(3).setRange(-175, 175);
		links.get(3).maxTorque.set(3.0); //Nm
	
		// tilt picassoBox
		links.get(4).setLetter("V");
		if(ATTACH_MODELS) links.get(4).setModelFilename("/Sixi2/picassoBox.obj");
		links.get(4).setD(0);
		links.get(4).setTheta(0);
		links.get(4).setR(0);
		links.get(4).setAlpha(-90);
		links.get(4).setRange(-120, 120);
		links.get(4).maxTorque.set(2.5); //Nm
	
		// roll hand
		links.get(5).setLetter("W");
		if(ATTACH_MODELS) links.get(5).setModelFilename("/Sixi2/hand.obj");
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
		links.get(4).setTheta(20);
	}
	
	/**
	 * send a command to this model
	 * @param command
	 */
	abstract public void sendCommand(String command);

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
		//gcode += " F"+getFeedrate();
		//gcode += " A"+getAcceleration();
		
		return gcode;
	}

	public void update(double dt) {
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
}
