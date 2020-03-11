package com.marginallyclever.robotOverlord.entity.robot.sixi2;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink.LinkAdjust;
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

		this.setModelFilename("/Sixi2/anchor.stl");
		this.setModelOrigin(0, 0, 0.9);
		this.setModelRotation(90,-90,0);
		
		// setup children
		this.setNumLinks(6);
		
		// pan shoulder
		links.get(0).setLetter("X");
		links.get(0).setModelFilename("/Sixi2/shoulder.stl");
		links.get(0).setD(18.8452+0.9);
		links.get(0).setR(0);
		links.get(0).setAlpha(-90);
		links.get(0).setModelRotation(0,-90,180);
		links.get(0).setModelOrigin(0,18.84520,0);
		links.get(1).setRange(-120,120);
	
		// tilt shoulder
		links.get(1).setLetter("Y");
		links.get(1).setModelFilename("/Sixi2/bicep.stl");
		links.get(1).setD(0);
		links.get(1).setR(35.796);
		links.get(1).setAlpha(0);
		links.get(1).setModelRotation(90,0,-90);
		links.get(1).setModelOrigin(-18.84520-35.796,0,0);
		links.get(1).setRange(-180,0);
		links.get(1).setTheta(-90);
	
		// tilt elbow
		links.get(2).setLetter("Z");
		links.get(2).setModelFilename("/Sixi2/forearm.stl");
		links.get(2).setD(0);
		links.get(2).setR(6.4259);
		links.get(2).setAlpha(-90);
		links.get(2).setModelRotation(180,0,-90);
		links.get(2).setModelOrigin(-18.84520-35.796-6.4259,0,0);
		links.get(2).setRange(-83.369, 86);
	
		// roll ulna
		links.get(3).setLetter("U");
		links.get(3).setModelFilename("/Sixi2/tuningFork.stl");
		links.get(3).setD(29.355+9.350);
		links.get(3).setR(0);
		links.get(3).setAlpha(90);
		links.get(3).setModelRotation(90,0,-90);
		links.get(3).setModelOrigin(-18.84520-35.796-6.4259,-(29.355+9.350),0);
		links.get(3).setRange(-175, 175);
	
		// tilt picassobox
		links.get(4).setLetter("V");
		links.get(4).setModelFilename("/Sixi2/picassoBox.stl");
		links.get(4).setD(0);
		links.get(4).setR(0);
		links.get(4).setAlpha(-90);
		links.get(4).setModelRotation(180,0,-90);
		links.get(4).setModelOrigin((-18.84520-35.796-6.4259),0,-(29.355+9.350));
		links.get(4).setRange(-120, 120);
		links.get(4).setTheta(20);
	
		// roll hand
		links.get(5).setLetter("W");
		links.get(5).setModelFilename("/Sixi2/hand.stl");
		links.get(5).setD(5.795);
		links.get(5).setR(0);
		links.get(5).setAlpha(0);
		links.get(5).setModelRotation(0,180,0);
		links.get(5).setModelOrigin(0,-18.84520-35.796-6.4259,-(29.355+9.350+5.795));
		links.get(5).setRange(-170, 170);
		

		setModelScale(0.1f);
		for( DHLink link : links ) {
			link.setModelScale(0.1f);
			link.flags = LinkAdjust.THETA;
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
