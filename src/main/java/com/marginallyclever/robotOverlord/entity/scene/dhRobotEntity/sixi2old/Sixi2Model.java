package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2old;

import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.convenience.memento.MementoOriginator;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHRobotModel;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.PoseFK;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink.LinkAdjust;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool.Sixi2ChuckGripper;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.JacobianHelper;
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
@Deprecated
public class Sixi2Model extends DHRobotModel implements MementoOriginator {	
	// last known state
	protected boolean readyForCommands=false;
	protected boolean relativeMode=false;
	protected int gMode=0;
	
	public DoubleEntity feedRate = new DoubleEntity("Feedrate",25.0);
	public DoubleEntity acceleration = new DoubleEntity("Acceleration",5.0);
	public DHLink endEffector;
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
	protected Matrix4d poseIKTarget = new Matrix4d();
	protected Matrix4d poseIKStart = new Matrix4d();
	protected Matrix4d poseIKNow = new Matrix4d();

	protected double[] cartesianForceDesired = {0,0,0,0,0,0};
	protected double[] jointVelocityDesired;

	public Sixi2Model() {
		this(true);
	}
	
	public Sixi2Model(boolean attachModels) {
		super();
		setName("Sixi2Model");
		addChild(feedRate);
		addChild(acceleration);
		
		this.setIKSolver(new DHIKSolver_GradientDescent());

		ModelEntity base = new ModelEntity();
		addChild(base);
		base.setName("Base");
		base.setModelFilename("/Sixi2/anchor.obj");
		base.getMaterial().setTextureFilename("/Sixi2/sixi.png");
		base.setModelOrigin(0, 0, 0.9);

		// setup children
		this.setNumLinks(6);

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
		
		endEffector = new DHLink();
		endEffector.setPosition(new Vector3d(0,0,0));
		endEffector.setName("End Effector");
		links.get(links.size()-1).addChild(endEffector);
		
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
		
		goHome();

		endEffectorTarget.setName("End Effector Target");
		addChild(endEffectorTarget);
		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
		
		// interpolation stuff
		int numAdjustableLinks = links.size();
		poseFKTarget = new double[numAdjustableLinks];
		poseFKStart = new double[numAdjustableLinks];
		poseFKNow = new double[numAdjustableLinks];

		poseIKNow.set(endEffector.getPoseWorld());
		poseIKTarget.set(poseIKNow);
		poseIKStart.set(poseIKNow);
		
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
		
		setTool(new Sixi2ChuckGripper());
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
						poseFKTarget[i] = StringHelper.parseNumber(t.substring(1));
					}
				}
				++i;
			}
			
			for( String t : tok ) {
				String letter = t.substring(0,1); 
				if(letter.equalsIgnoreCase("F")) {
					feedRate.set(StringHelper.parseNumber(t.substring(1)));
				} else if(letter.equalsIgnoreCase("A")) {
					acceleration.set(StringHelper.parseNumber(t.substring(1)));
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
	        poseIKNow.set(endEffector.getPoseWorld());
	        poseIKStart.set(poseIKNow);
	        
	        // get the target matrix
	        PoseFK oldPose = getPoseFK();
	        PoseFK newPose = ikSolver.createPoseFK();
	        newPose.set(poseFKTarget);
	        setPoseFK(newPose);
	        poseIKTarget.set(endEffector.getPoseWorld());
	        setPoseFK(oldPose);

	        double travelS = dMax/(double)feedRate.get();
	        
	        MatrixHelper.normalize3(poseIKTarget);

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
					poseIKStart, 
					poseIKTarget, 
					tFraction, 
					poseIKNow);
			MatrixHelper.normalize3(poseIKNow);
			setPoseIK(poseIKNow);
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
		MatrixHelper.interpolate(poseIKStart,poseIKTarget, ratioFuture, interpolatedMatrixFuture);
		
		JacobianHelper.getCartesianForceBetweenTwoPoses(interpolatedMatrixNow, interpolatedMatrixFuture, dt, cartesianForceDesired);

		PoseFK keyframe = getPoseFK();
		
		if(!JacobianHelper.getJointVelocityFromCartesianForce(this,cartesianForceDesired,jointVelocityDesired)) return;
		
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
			poseIKNow.set(endEffector.getPoseWorld());
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
		endEffectorTarget.setPoseWorld(endEffector.getPoseWorld());
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

	/**
	 * @return a list of cuboids, or null.
	 */
	@Override
	public ArrayList<Cuboid> getCuboidList() {
		ArrayList<Cuboid> cuboidList = new ArrayList<Cuboid>();

		refreshPose();

		for( DHLink link : links ) {
			if(link.getCuboid() != null ) {
				cuboidList.addAll(link.getCuboidList());
			}
		}
		if(dhTool != null) {
			cuboidList.addAll(dhTool.getCuboidList());
		}

		return cuboidList;
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
}
