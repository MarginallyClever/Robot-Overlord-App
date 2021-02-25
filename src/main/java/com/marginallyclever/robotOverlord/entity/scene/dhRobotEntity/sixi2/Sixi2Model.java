package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.convenience.memento.MementoOriginator;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.solvers.DHIKSolver_GradientDescent;
import com.marginallyclever.robotOverlord.entity.scene.shapeEntity.ShapeEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Contains the setup of the DHLinks for a DHRobot.
 * TODO Could read these values from a text file.
 * TODO Could have an interactive setup option - dh parameter design app?
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class Sixi2Model extends DHRobotModel implements MementoOriginator {	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5074982680400300334L;

	public static final double SENSOR_RESOLUTION = 360.0 / Math.pow(2,14); 
	
	public static final double MAX_FEEDRATE = 80.0;  // cm/s
	public static final double DEFAULT_FEEDRATE = 25.0;  // cm/s
	public static final double MAX_ACCELERATION = 202.5;  // cm/s
	public static final double DEFAULT_ACCELERATION = 25.25;  // cm/s/s
	
	public static final double MOTOR_STEPS_PER_TURN  =200.0; // motor full steps * microstepping setting
	
	public static final double NEMA17_CYCLOID_GEARBOX_RATIO        = 20.0;
	public static final double NEMA23_CYCLOID_GEARBOX_RATIO_ELBOW  = 35.0;
	public static final double NEMA23_CYCLOID_GEARBOX_RATIO_ANCHOR = 30.0;
	public static final double NEMA24_CYCLOID_GEARBOX_RATIO        = 40.0;
	
	public static final double DM322T_MICROSTEP = 2.0;
	
	public static final double ELBOW_DOWNGEAR_RATIO = 30.0/20.0;
	public static final double NEMA17_RATIO         = DM322T_MICROSTEP*NEMA17_CYCLOID_GEARBOX_RATIO*ELBOW_DOWNGEAR_RATIO;
	public static final double NEMA23_RATIO_ELBOW   = NEMA23_CYCLOID_GEARBOX_RATIO_ELBOW;
	public static final double NEMA23_RATIO_ANCHOR  = NEMA23_CYCLOID_GEARBOX_RATIO_ANCHOR;
	public static final double NEMA24_RATIO         = NEMA24_CYCLOID_GEARBOX_RATIO;
	
	// Motors are numbered 0 (base) to 5 (hand)
	public static final double MOTOR_0_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA23_RATIO_ANCHOR;  // anchor
	public static final double MOTOR_1_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA24_RATIO;  // shoulder
	public static final double MOTOR_2_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA23_RATIO_ELBOW;  // elbow
	public static final double MOTOR_3_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA17_RATIO;  // ulna
	public static final double MOTOR_4_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA17_RATIO;  // wrist
	public static final double MOTOR_5_STEPS_PER_TURN = MOTOR_STEPS_PER_TURN*NEMA17_RATIO;  // hand
	
	public static final double DEGREES_PER_STEP_0 = 360.0/MOTOR_0_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_1 = 360.0/MOTOR_1_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_2 = 360.0/MOTOR_2_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_3 = 360.0/MOTOR_3_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_4 = 360.0/MOTOR_4_STEPS_PER_TURN;
	public static final double DEGREES_PER_STEP_5 = 360.0/MOTOR_5_STEPS_PER_TURN;
	
	public static final int MAX_SEGMENTS = 16;
	public static final double MIN_SEGMENT_TIME = 0.025;  // seconds

	public static final double MAX_JOINT_ACCELERATION = 101.5;  // deg/s
	public static final double MAX_JOINT_FEEDRATE = 50.0;  // deg/s

	public static final double [] MAX_JERK = { 3,3,3,4,4,5,5 };
	
	// the joint angles that lead to a given finger tip position.  one set of joint angles creates one finger tip position.
	protected PoseFK poseFK;
	// the finger tip position.  one finger tip position is creates more than one set of joint angles.
	protected Matrix4d poseIK = new Matrix4d();
	

	public Sixi2Model() {
		this(true);
	}
	
	public Sixi2Model(boolean attachModels) {
		super(new DHIKSolver_GradientDescent());
		
		setName("Sixi2Model");

		ShapeEntity base = new ShapeEntity();
		addChild(base);
		base.setName("Base");
		base.setShapeFilename("/Sixi2/anchor.obj");
		base.getMaterial().setTextureFilename("/Sixi2/sixi.png");
		base.setShapeOrigin(0, 0, 0.9);

		// setup children
		this.setNumLinks(7);

		if(!attachModels) {
			ShapeEntity part0 = new ShapeEntity();	addChild(part0);	part0.setShapeFilename("/Sixi2/shoulder.obj");
			ShapeEntity part1 = new ShapeEntity();	addChild(part1);	part1.setShapeFilename("/Sixi2/bicep.obj");
			ShapeEntity part2 = new ShapeEntity();	addChild(part2);	part2.setShapeFilename("/Sixi2/forearm.obj");
			ShapeEntity part3 = new ShapeEntity();	addChild(part3);	part3.setShapeFilename("/Sixi2/tuningFork.obj");
			ShapeEntity part4 = new ShapeEntity();	addChild(part4);	part4.setShapeFilename("/Sixi2/picassoBox.obj");
			ShapeEntity part5 = new ShapeEntity();	addChild(part5);	part5.setShapeFilename("/Sixi2/hand.obj");
		} else {
			links.get(0).setShapeFilename("/Sixi2/shoulder.obj");
			links.get(1).setShapeFilename("/Sixi2/bicep.obj");
			links.get(2).setShapeFilename("/Sixi2/forearm.obj");
			links.get(3).setShapeFilename("/Sixi2/tuningFork.obj");
			links.get(4).setShapeFilename("/Sixi2/picassoBox.obj");
			links.get(5).setShapeFilename("/Sixi2/hand.obj");
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
		links.get(5).setD(3.795);
		links.get(5).setR(0);
		links.get(5).setAlpha(0);
		links.get(5).setRange(-170, 170);
		links.get(5).maxTorque.set(2.5); //Nm

		links.get(6).setLetter("E");
		links.get(6).setName("End Effector");
		links.get(6).setD(2.75);
		links.get(6).flags = LinkAdjust.NONE;
		
		// update this world pose and all my children's poses all the way down.
		refreshPose();
		
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

		// set to default position
		goHome();
		// make room to store the current position and get a copy of the default.
		poseFK = getPoseFK();
		// ...so that we can get the IK pose of the finger tip.
		poseIK.set(links.get(getNumNonToolLinks()-1).getPoseWorld());
	}

	/**
	 * Override this to set your robot at home position.  Called on creation.
	 */
	@Override
	public void goHome() {
	    // the home position
		PoseFK homeKey = createPoseFK();
		homeKey.fkValues[0]=0;
		homeKey.fkValues[1]=-90;
		homeKey.fkValues[2]=0;
		homeKey.fkValues[3]=0;
		homeKey.fkValues[4]=20;
		homeKey.fkValues[5]=0;
		setPoseFK(homeKey);
	}

	public void goRest() {
	    // set rest position
		PoseFK restKey = createPoseFK();
		restKey.fkValues[0]=0;
		restKey.fkValues[1]=-60-90;
		restKey.fkValues[2]=85+90;
		restKey.fkValues[3]=0;
		restKey.fkValues[4]=20;
		restKey.fkValues[5]=0;
		setPoseFK(restKey);
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sm", "Sixi Model");
		view.popStack();
		super.getView(view);
	}

	@Override
	public Memento getState() {
		Sixi2ModelState s = new Sixi2ModelState();
		s.poseFK = getPoseFK();
		s.currentTool = this.toolIndex;
		if(toolIndex>=0 && toolIndex < allTools.size()) {
			s.toolMemento = allTools.get(toolIndex).getState();
		}
		return s;
	}

	@Override
	public void setState(Memento arg0) {
		if(!(arg0 instanceof Sixi2ModelState)) return;
		Sixi2ModelState s = (Sixi2ModelState)arg0;
		this.setPoseFK(s.poseFK);
		this.toolIndex = s.currentTool;
		if(toolIndex>=0 && toolIndex < allTools.size()) {
			allTools.get(toolIndex).setState(s.toolMemento);
		}
	}
	
	/**
	 * Return the Jacobian matrix for Sixi at a given FK pose.
	 * Courtesy of https://github.com/MichaelRyanGreer/Instructional/blob/main/inverse_kinematics/inverse_kinematics_DH_parameters.ipynb
	 * @param keyframe joint angles
	 * @return 6x6 jacobian matrix 
	 */
	public double[][] exactJacobian(PoseFK keyframe) {
		double s1=Math.sin(keyframe.fkValues[0]);
		double s2=Math.sin(keyframe.fkValues[1]);
		//double s3=Math.sin(keyframe.fkValues[2]);
		double s4=Math.sin(keyframe.fkValues[3]);
		double s5=Math.sin(keyframe.fkValues[4]);
		double s23 = Math.sin(keyframe.fkValues[1]+keyframe.fkValues[2]);

		double c1=Math.cos(keyframe.fkValues[0]);
		double c2=Math.cos(keyframe.fkValues[1]);
		//double c3=Math.cos(keyframe.fkValues[2]);
		double c4=Math.cos(keyframe.fkValues[3]);
		double c5=Math.cos(keyframe.fkValues[4]);
		double c23 = Math.cos(keyframe.fkValues[1]+keyframe.fkValues[2]);

		double r1 = links.get(1).getR();
		double r2 = links.get(2).getR();
		double d3 = links.get(3).getD();
		double d5d6 = links.get(5).getD() + links.get(6).getD();
		
		double [][] jacobian = {
			{
				d5d6*s1*s5*c4*c23 + d5d6*s1*s23*c5 + d3*s1*s23 - r1*s1*c2-r2*s1*c23 - d5d6*s4*s5*c1,
				-d5d6*s1*s4*s5 - d5d6*s5*c1*c4*c23 - d5d6*s23*c1*c5 - d3*s23*c1 + r1*c1*c2 + r2*c1*c23,
				0,
				0,
				0,
				1,
			},
			{ 
				-(r1*s2 - d5d6*s5*s23*c4 + r2*s23 + d5d6*c5*c23 + d3*c23)*c1,
				-(r1*s2 - d5d6*s5*s23*c4 + r2*s23 + d5d6*c5*c23 + d3*c23)*s1,
				d5d6*s5*c4*c23 + d5d6*s23*c5 + d3*s23 - r1*c2 - r2*c23,
				-s1,
				c1,
				0
			},
			{
				(d5d6*s5*s23*c4 - r2*s23 - d5d6*c5*c23 - d3*c23)*c1,
				(d5d6*s5*s23*c4 - r2*s23 - d5d6*c5*c23 - d3*c23)*s1,
				d5d6*s5*c4*c23 + d5d6*s23*c5 + d3*s23 - r2*c23,
				-s1,
				c1,
				0
			},
			{
				d5d6*(-s1*c4 + s4*c1*c23)*s5,
				d5d6*(s1*s4*c23 + c1*c4)*s5,
				-d5d6*s4*s5*s23,
				-s23*c1,
				-s1*s23,
				-c23
			},
			{
				-d5d6*s1*s4*c5+d5d6*s5*s23*c1-d5d6*c1*c4*c5*c23,
				d5d6*s1*s5*s23-d5d6*s1*c4*c5*c23+d5d6*s4*c1*c5,
				d5d6*s5*c23+d5d6*s23*c4*c5,
				-s1*c4+s4*c1*c23,
				s1*s4*c23+c1*c4,
				-s4*s23
			},
			{
				0,
				0,
				0,
				-(s1*s4+c1*c4*c23)*s5-s23*c1*c5,
				(-s1*c4*c23+s4*c1)*s5-s1*s23*c5,
				s5*s23*c4-c5*c23
			}
		};
		
		return jacobian;
	}
	
	
	/**
	 * Use Forward Kinematics to approximate the Jacobian matrix for Sixi.
	 * See also https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
	 * @param keyframe joint angles
	 * @return 6x6 jacobian matrix 
	 */
	public double [][] approximateJacobian(PoseFK keyframe) {
		double [][] jacobian = new double[6][6];
		
		double ANGLE_STEP_SIZE_DEGREES=0.5;  // degrees
		
		PoseFK oldPoseFK = getPoseFK();
		
		setPoseFK(keyframe);
		Matrix4d T = new Matrix4d(poseIK);
		
		PoseFK newPoseFK = createPoseFK();
		int i=0;
		// for all adjustable joints
		for( DHLink link : links ) {
			if(link.flags == LinkAdjust.NONE) continue;
			
			// use anglesB to get the hand matrix after a tiny adjustment on one joint.
			newPoseFK.set(keyframe);
			newPoseFK.fkValues[i]+=ANGLE_STEP_SIZE_DEGREES;
			setPoseFK(newPoseFK);
			// Tnew will be different from T because of the changes in setPoseFK().
			Matrix4d Tnew = new Matrix4d(poseIK);
			
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
	
	
	/**
	 * Convert joint velocity to cartesian velocity.
	 * @param model a robot model set to the current pose.
	 * @param jointVelocity from which to calculate the cartesian force.
	 * @return cartesian force calculated
	 */
	public double [] getCartesianVelocityFromJointVelocity(final double [] jointVelocity) {
		double [] cf = new double[6];  // cartesian force calculated
		double[][] jacobian = exactJacobian(getPoseFK());

		for( int k=0;k<6;++k ) {
			for( int j=0;j<6;++j ) {
				cf[j] += jacobian[k][j] * jointVelocity[k];
			}
		}
		return cf;
	}
	
	
	// return the Sixi2Command that represents the current model state. 
	public Sixi2Command createCommand() {
		Sixi2Command c = new Sixi2Command(getPoseFK(),
				Sixi2Model.DEFAULT_FEEDRATE,
				Sixi2Model.DEFAULT_ACCELERATION,
				1,0);
		c.poseIK.set(getPoseIK());
		return c;
	}
	
	
	public void setCommand(Sixi2Command c) {
		setPoseFK(c.getPoseFK());
	}
}
