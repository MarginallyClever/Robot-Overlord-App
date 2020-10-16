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
public class Sixi2Model extends DHRobotModel implements MementoOriginator {	
	protected PoseFK poseFK;
	protected Matrix4d poseIK = new Matrix4d();

	public Sixi2Model() {
		this(true);
	}
	
	public Sixi2Model(boolean attachModels) {
		super(new DHIKSolver_GradientDescent());
		setName("Sixi2Model");

		ModelEntity base = new ModelEntity();
		addChild(base);
		base.setName("Base");
		base.setModelFilename("/Sixi2/anchor.obj");
		base.getMaterial().setTextureFilename("/Sixi2/sixi.png");
		base.setModelOrigin(0, 0, 0.9);

		// setup children
		this.setNumLinks(7);

		if(!attachModels) {
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

		links.get(6).setLetter("E");
		links.get(6).setName("End Effector");
		links.get(6).setD(3);
		links.get(6).flags = LinkAdjust.NONE;
		
		// update this world pose and all my children's poses all the way down.
		refreshPose();
		
		// Use the poseWorld for each DHLink to adjust the model origins.
		for(int i=0;i<getNumLinks();++i) {
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
		// make sure the matrixes in the model match the default position...
		refreshPose();
		// ...so that we can get the IK pose of the finger tip.
		poseIK.set(links.get(getNumLinks()-1).getPoseWorld());
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
		return this.getPoseFK();
	}

	@Override
	public void setState(Memento arg0) {
		if(arg0 instanceof PoseFK) {
			this.setPoseFK((PoseFK)arg0);
		}
	}
	
	/**
	 * Use Forward Kinematics to approximate the Jacobian matrix for Sixi.
	 * See also https://robotacademy.net.au/masterclass/velocity-kinematics-in-3d/?lesson=346
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
}
