package com.marginallyclever.robotOverlord.entity.robot.sixi2;

import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHLink;
import com.marginallyclever.robotOverlord.engine.dhRobot.DHRobot;
import com.marginallyclever.robotOverlord.engine.dhRobot.solvers.DHIKSolver_RTTRTR;

public class Sixi2Model extends DHRobot {	
	double ELBOW_TO_ULNA_Y = -28.805;
	double ELBOW_TO_ULNA_Z = 4.7201;
	double ULNA_TO_WRIST_Y = -11.800;
	double ULNA_TO_WRIST_Z = 0;
	double ELBOW_TO_WRIST_Y = ELBOW_TO_ULNA_Y + ULNA_TO_WRIST_Y;  //-40.605
	double ELBOW_TO_WRIST_Z = ELBOW_TO_ULNA_Z + ULNA_TO_WRIST_Z;  // 4.7201
	//double WRIST_TO_HAND = 8.9527;

	// last known state
	
	protected boolean readyForCommands=false;
	protected boolean relativeMode=false;
	protected int gMode=0;
	protected double feedRate;
	protected double acceleration;

	
	public Sixi2Model() {
		super();
		setName("Sixi2Model");
		
		feedRate=5;
		acceleration=20;
		
		this.setIKSolver(new DHIKSolver_RTTRTR());

		this.setFilename("/Sixi2/anchor2.stl");
		this.setModelOrigin(new Vector3d(0, 0, 5.15));
		this.setModelRotation(new Vector3d(90,180,0));
		
		// setup children
		this.setNumLinks(8);

		// roll anchor
		links.get(0).setD(13.44);
		links.get(0).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(0).setRange(-120, 120);
		links.get(0).setName("X");
		links.get(0).setFilename("/Sixi2/shoulder2.stl");
		links.get(0).setModelOrigin(new Vector3d(0, 0, -5.3));
		links.get(0).setModelRotation(new Vector3d(90,-90,0));
	
		// tilt shoulder
		links.get(1).setTheta(90);
		links.get(1).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(1).setRangeMin(-72);
		links.get(1).setName("Y");
		links.get(1).setFilename("/Sixi2/bicep2.stl");
		links.get(1).setModelOrigin(new Vector3d(-1.82, 0, 9));
		links.get(1).setModelRotation(new Vector3d(90,0,0));
	
		// tilt elbow
		links.get(2).setD(44.55);
		links.get(2).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(2).setRange(-83.369, 86);
		links.get(2).setName("Z");
		links.get(2).setFilename("/Sixi2/forearm2.stl");
		links.get(2).setModelOrigin(new Vector3d(0,ELBOW_TO_WRIST_Y,ELBOW_TO_WRIST_Z));
		links.get(2).setModelRotation(new Vector3d(-90,0,180));
	
		// interim point
		links.get(3).setD(4.7201);
		links.get(3).setAlpha(90);
		links.get(3).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		
		// roll ulna
		links.get(4).setD(28.805);
		links.get(4).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(4).setRange(-175, 175);
		links.get(4).setName("U");
		links.get(4).setFilename("/Sixi2/tuningFork2.stl");
		links.get(4).setModelOrigin(new Vector3d(0, 0, -ULNA_TO_WRIST_Y));
		links.get(4).setModelRotation(new Vector3d(0,180,0));
	
		// tilt picassobox
		links.get(5).setD(11.8);
		links.get(5).setAlpha(25);
		links.get(5).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R;
		links.get(5).setRange(-120, 125);
		links.get(5).setName("Z");
		links.get(5).setFilename("/Sixi2/picassoBox2.stl");
		links.get(5).setModelRotation(new Vector3d(180,0,180));
	
		// roll hand
		links.get(6).setD(3.9527);
		links.get(6).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;
		links.get(6).setRange(-180, 180);
		links.get(6).setName("W");
		links.get(6).setFilename("/Sixi2/hand2.stl");
		links.get(6).setModelOrigin(new Vector3d(0,0,-3.9527));
		links.get(6).setModelRotation(new Vector3d(180,0,180));
	
		
		links.get(7).flags = DHLink.READ_ONLY_D | DHLink.READ_ONLY_THETA | DHLink.READ_ONLY_R | DHLink.READ_ONLY_ALPHA;

		setModelScale(0.1f);
		for( DHLink link : links ) {
			link.setModelScale(0.1f);
		}
		
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
			gcode += " " + links.get(i).getName() + links.get(i).getAdjustableValue();
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
