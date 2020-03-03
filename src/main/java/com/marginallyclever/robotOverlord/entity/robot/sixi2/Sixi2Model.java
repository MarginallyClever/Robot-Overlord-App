package com.marginallyclever.robotOverlord.entity.robot.sixi2;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.engine.dhRobot.solvers.DHIKSolver_RTTRTR;

public class Sixi2Model extends DHRobot {	
	// last known state
	
	protected boolean readyForCommands=false;
	protected boolean relativeMode=false;
	protected int gMode=0;
	protected double feedRate;
	protected double acceleration;

	
	public Sixi2Model() {
		super();
		
		feedRate=5;
		acceleration=20;
		
		this.setIKSolver(new DHIKSolver_RTTRTR());
		
		this.setNumLinks(8);
		// roll anchor
		links.get(0).setD(13.44);
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).setRange(-120, 120);
		links.get(0).setLetter("X");
	
		// tilt shoulder
		links.get(1).setTheta(90);
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).setRangeMin(-72);
		links.get(1).setLetter("Y");
	
		// tilt elbow
		links.get(2).setD(44.55);
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).setRange(-83.369, 86);
		links.get(2).setLetter("Z");
	
		// interim point
		links.get(3).setD(4.7201);
		links.get(3).setAlpha(90);
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		// roll ulna
		links.get(4).setD(28.805);
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).setRange(-175, 175);
		links.get(4).setLetter("U");
	
		// tilt picassobox
		links.get(5).setD(11.8);
		links.get(5).setAlpha(25);
		links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(5).setRange(-120, 125);
		links.get(5).setLetter("Z");
	
		// roll hand
		links.get(6).setD(3.9527);
		links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(6).setRange(-180, 180);
		links.get(6).setLetter("W");
	
		links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
	
		this.refreshPose();
	}

	public void update(double dt) {}

	/**
	 * send a command to this model
	 * @param command
	 */
	public void sendCommand(String command) {}

	/**
	 * get the command for this model
	 * @return
	 */
	public String getCommand() {
		String gcode = "G0";
		for (int i = 0; i < links.size(); ++i) {
			gcode += " " + links.get(i).getLetter() + links.get(i).getAdjustableValue();
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
