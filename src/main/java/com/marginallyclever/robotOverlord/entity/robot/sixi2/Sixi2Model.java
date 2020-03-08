package com.marginallyclever.robotOverlord.entity.robot.sixi2;

import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.engine.dhRobot.solvers.DHIKSolver_RTTRTR;

public abstract class Sixi2Model extends DHRobot {	
	// last known state
	protected boolean readyForCommands=false;
	protected boolean relativeMode=false;
	protected int gMode=0;
	protected double feedRate;
	protected double acceleration;

	
	public Sixi2Model() {
		super();
		setName("Sixi2Model");
		
		feedRate=25;
		acceleration=20;
		
		this.setIKSolver(new DHIKSolver_RTTRTR());

		this.setFilename("/Sixi2/anchor.stl");
		this.setModelOrigin(0, 0, 0.9);
		this.setModelRotation(90,-90,0);
		
		// setup children
		this.setNumLinks(8);
		
		// pan shoulder
		links.get(0).setD(188.452/10+0.9);
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).setRange(-120, 120);
		links.get(0).setLetter("X");
		links.get(0).setFilename("/Sixi2/shoulder.stl");
		links.get(0).setModelOrigin(0, 0, -188.452/10);
		links.get(0).setModelRotation(new Vector3d(90,-90,0));
	
		// tilt shoulder
		links.get(1).setTheta(90);
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).setRangeMin(-72);
		links.get(1).setLetter("Y");
		links.get(1).setFilename("/Sixi2/bicep.stl");
		links.get(1).setModelOrigin(0, 0, -188.452/10);
		links.get(1).setModelRotation(new Vector3d(90,180,0));
	
		// tilt elbow
		links.get(2).setD(357.96/10);
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).setRange(-83.369, 86);
		links.get(2).setLetter("Z");
		links.get(2).setFilename("/Sixi2/forearm.stl");
		links.get(2).setModelOrigin(0,0,(-188.452-357.96)/10);
		links.get(2).setModelRotation(new Vector3d(-90,0,180));
	
		// interim point
		links.get(3).setD(64.259/10);
		links.get(3).setAlpha(90);
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		
		// roll ulna
		links.get(4).setD(293.55/10);
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).setRange(-175, 175);
		links.get(4).setLetter("U");
		links.get(4).setFilename("/Sixi2/tuningFork.stl");
		links.get(4).setModelOrigin(0,(-188.452-357.96-64.259)/10,(-293.55)/10);
		links.get(4).setModelRotation(new Vector3d(0,180,0));
	
		// tilt picassobox
		links.get(5).setD(93.50/10);
		links.get(5).setAlpha(25);
		links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(5).setRange(-120, 125);
		links.get(5).setLetter("V");
		links.get(5).setFilename("/Sixi2/picassoBox.stl");
		links.get(5).setModelOrigin(0,(-188.452-357.96-64.259)/10,(-293.55-93.50)/10);
		links.get(5).setModelRotation(new Vector3d(180,0,180));
	
		// roll hand
		links.get(6).setD(57.95/10);
		links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(6).setRange(-180, 180);
		links.get(6).setLetter("W");
		links.get(6).setFilename("/Sixi2/hand.stl");
		links.get(6).setModelOrigin(0,(-188.452-357.96-64.259)/10,(-293.55-93.50-57.95)/10);
		links.get(6).setModelRotation(new Vector3d(180,0,180));
		
		links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		setModelScale(0.1f);
		for( DHLink link : links ) {
			link.setModelScale(0.1f);
		}
		
		this.refreshPose();
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
		gcode += " F"+(StringHelper.formatDouble(feedRate));
		gcode += " A"+(StringHelper.formatDouble(acceleration));
		
		return gcode;
	}


	public double getFeedrate() {
		return feedRate;
	}


	public void setFeedRate(double feedrate) {
		this.feedRate = feedrate;
	}


	public double getAcceleration() {
		return acceleration;
	}


	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}
}
