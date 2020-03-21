package com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.sixi2;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.solvers.DHIKSolver_RTTRTR;

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
	public PoseEntity endEffector = new PoseEntity("End Effector");

	// set this to false before running the app and the model will not attach to the DHLinks.
	// this is convenient for setting up the DHLinks with less visual confusion.
	static final boolean ATTACH_MODELS=true;
	
	public Sixi2Model() {
		super();
		setName("Sixi2Model");
		addChild(feedRate);
		addChild(acceleration);
		addChild(endEffector);

		this.setIKSolver(new DHIKSolver_RTTRTR());
		
		ModelEntity anchor = new ModelEntity();
		addChild(anchor);
		anchor.setName("Base");
		anchor.setModelFilename("/Sixi2/anchor.stl");
		anchor.setModelOrigin(0, 0, 0.9);

		// setup children
		this.setNumLinks(6);

		if(!ATTACH_MODELS) {
			ModelEntity anchor1 = new ModelEntity();	addChild(anchor1);	anchor1.setModelFilename("/Sixi2/shoulder.stl");
			ModelEntity anchor2 = new ModelEntity();	addChild(anchor2);	anchor2.setModelFilename("/Sixi2/bicep.stl");
			ModelEntity anchor3 = new ModelEntity();	addChild(anchor3);	anchor3.setModelFilename("/Sixi2/forearm.stl");
			ModelEntity anchor4 = new ModelEntity();	addChild(anchor4);	anchor4.setModelFilename("/Sixi2/tuningFork.stl");
			ModelEntity anchor5 = new ModelEntity();	addChild(anchor5);	anchor5.setModelFilename("/Sixi2/picassoBox.stl");
			ModelEntity anchor6 = new ModelEntity();	addChild(anchor6);	anchor6.setModelFilename("/Sixi2/hand.stl");
		}
		
		// pan shoulder
		links.get(0).setLetter("X");
		if(ATTACH_MODELS) links.get(0).setModelFilename("/Sixi2/shoulder.stl");
		links.get(0).setD(18.8452);
		links.get(0).setTheta(0);
		links.get(0).setR(0);
		links.get(0).setAlpha(0);
		links.get(0).setRange(-120,120);
		
		// tilt shoulder
		links.get(1).setLetter("Y");
		if(ATTACH_MODELS) links.get(1).setModelFilename("/Sixi2/bicep.stl");
		links.get(1).setD(0);
		links.get(1).setTheta(0);
		links.get(1).setR(0);
		links.get(1).setAlpha(-90);
		links.get(1).setRange(-90,90);

		// tilt elbow
		links.get(2).setLetter("Z");
		if(ATTACH_MODELS) links.get(2).setModelFilename("/Sixi2/forearm.stl");
		links.get(2).setD(0);
		links.get(2).setTheta(-90);
		links.get(2).setR(35.796);
		links.get(2).setAlpha(0);
		links.get(2).setRange(-83.369-90, 86-90);
	
		// roll ulna
		links.get(3).setLetter("U");
		if(ATTACH_MODELS) links.get(3).setModelFilename("/Sixi2/tuningFork.stl");
		links.get(3).setD(0);
		links.get(3).setTheta(0);
		links.get(3).setR(6.4259);
		links.get(3).setAlpha(-90);
		links.get(3).setRange(-175, 175);
	
		// tilt picassoBox
		links.get(4).setLetter("V");
		if(ATTACH_MODELS) links.get(4).setModelFilename("/Sixi2/picassoBox.stl");
		links.get(4).setD(29.355+9.350);
		links.get(4).setTheta(0);
		links.get(4).setR(0);
		links.get(4).setAlpha(-90);
		links.get(4).setRange(-120, 120);
	
		// roll hand
		links.get(5).setLetter("W");
		if(ATTACH_MODELS) links.get(5).setModelFilename("/Sixi2/hand.stl");
		links.get(5).setTheta(0);
		links.get(5).setD(0);
		links.get(5).setR(0);
		links.get(5).setAlpha(90);
		links.get(5).setRange(-170, 170);
		
		endEffector.setPosition(new Vector3d(0,0,5.795));
		
		links.get(links.size()-1).addChild(endEffector);
		
		// update this world pose and all my children's poses all the way down.
		this.updatePoseWorld();
		
		// Use the poseWorld for each DHLink to adjust the model origins.
		for(int i=0;i<links.size();++i) {
			DHLink bone=links.get(i);
			if(bone.getModel()!=null) {
				Matrix4d iWP = bone.getPoseWorld();
				iWP.invert();
				bone.getModel().adjustMatrix(iWP);
			}
		}
	}
	
	abstract public void update(double dt);

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
		gcode += " F"+feedRate;
		gcode += " A"+acceleration;
		
		return gcode;
	}

	public double getFeedrate() {
		return feedRate.get();
	}

	public void setFeedRate(double feedrate) {
		this.feedRate.set(feedrate);
	}

	public double getAcceleration() {
		return acceleration.get();
	}

	public void setAcceleration(double acceleration) {
		this.acceleration.set(acceleration);
	}
}
