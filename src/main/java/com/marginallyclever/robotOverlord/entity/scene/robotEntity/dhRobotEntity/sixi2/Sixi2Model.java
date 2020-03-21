package com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.sixi2;

import javax.vecmath.Matrix4d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.DHRobotEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.dhRobotEntity.solvers.DHIKSolver_RTTRTR;

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

	
	public Sixi2Model() {
		super();
		setName("Sixi2Model");
		addChild(feedRate);
		addChild(acceleration);
		addChild(endEffector);
		
		this.setIKSolver(new DHIKSolver_RTTRTR());
		
		// setup children
		this.setNumLinks(6);

		ModelEntity anchor = new ModelEntity();
		addChild(anchor);
		anchor.setName("Base");
		anchor.setModelFilename("/Sixi2/anchor.stl");
		anchor.setModelOrigin(0, 0, 0.9);
		
		// pan shoulder
		links.get(0).setLetter("X");
		links.get(0).setModelFilename("/Sixi2/shoulder.stl");
		links.get(0).setD(18.8452);
		links.get(0).setR(0);
		links.get(0).setAlpha(-90);
		links.get(0).setRange(-120,120);
	
		// tilt shoulder
		links.get(1).setLetter("Y");
		links.get(1).setModelFilename("/Sixi2/bicep.stl");
		links.get(1).setD(0);
		links.get(1).setR(35.796);
		links.get(1).setAlpha(0);
		links.get(1).setRange(-180,0);
		links.get(1).setTheta(-90);
	
		// tilt elbow
		links.get(2).setLetter("Z");
		links.get(2).setModelFilename("/Sixi2/forearm.stl");
		links.get(2).setD(0);
		links.get(2).setR(6.4259);
		links.get(2).setAlpha(-90);
		links.get(2).setRange(-83.369, 86);
	
		// roll ulna
		links.get(3).setLetter("U");
		links.get(3).setModelFilename("/Sixi2/tuningFork.stl");
		links.get(3).setD(29.355+9.350);
		links.get(3).setR(0);
		links.get(3).setAlpha(90);
		links.get(3).setRange(-175, 175);
	
		// tilt picassobox
		links.get(4).setLetter("V");
		links.get(4).setModelFilename("/Sixi2/picassoBox.stl");
		links.get(4).setD(0);
		links.get(4).setR(0);
		links.get(4).setAlpha(-90);
		links.get(4).setRange(-120, 120);
	
		// roll hand
		links.get(5).setLetter("W");
		links.get(5).setModelFilename("/Sixi2/hand.stl");
		links.get(5).setD(5.795);
		links.get(5).setR(0);
		links.get(5).setAlpha(0);
		links.get(5).setRange(-170, 170);

		links.get(5).addChild(endEffector);
		
		this.refreshPose();

		// Now I have the poseWorld for each DHLink, I can use that to adjust the model values.
		// I need the world pose of every 'bone' DHLink.  I'll keep a running total in 'worldPose'.
		// The bone.Z axis is the axis of rotation for that kinematic link.
		Matrix4d worldPose = new Matrix4d();
		worldPose.setIdentity();
		Matrix4d iWP = new Matrix4d();
		Matrix4d m2 = new Matrix4d();
		
		for(int i=0;i<links.size();++i) {
			DHLink bone=links.get(i);
			Matrix4d m = bone.getPose();
			worldPose.mul(m);
			m2.sub(m,bone.getPoseWorld());
			iWP.set(worldPose);
			iWP.invert();
			bone.getModel().adjustMatrix(iWP);
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
