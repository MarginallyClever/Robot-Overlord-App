package com.marginallyclever.robotOverlord.arm3;

import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.HTMLDialogBox;

public class Arm3Dimensions {
	// name
	final protected String ROBOT_NAME = "Arm3";
	// expected firmware starting message
	final protected String HELLO = "HELLO WORLD! I AM ARM3 #";
	// expected firmware version
	final protected int firmwareVersion = 1;
		
	// Machine dimensions, in cm
	final protected float BASE_TO_SHOULDER_X   =(5.37f);  // measured in solidworks
	final protected float BASE_TO_SHOULDER_Z   =(9.55f);  // measured in solidworks
	final protected float SHOULDER_TO_ELBOW    =(25.0f);
	final protected float ELBOW_TO_WRIST       =(25.0f);
	final protected float WRIST_TO_FINGER      =(4.0f);
	final protected float BASE_TO_SHOULDER_MINIMUM_LIMIT = 7.5f;
		
	// When the robot is homed, what are the XYZ coordinates of the finger tip?
	final protected float HOME_X = 13.05f;
	final protected float HOME_Y = 0;
	final protected float HOME_Z = 22.2f;
	
	final protected float HOME_RIGHT_X = 0;
	final protected float HOME_RIGHT_Y = 0;
	final protected float HOME_RIGHT_Z = -1;
	
	final protected float HOME_FORWARD_X = 1;
	final protected float HOME_FORWARD_Y = 0;
	final protected float HOME_FORWARD_Z = 0;
	
	// Dangerous!
	final boolean HOME_AUTOMATICALLY_ON_STARTUP = true;
	

	public String getName() {
		return ROBOT_NAME;
	}
	
	public String getHello() {
		return HELLO;
	}

	public boolean getHomeAutomaticallyOnStartup() {	return HOME_AUTOMATICALLY_ON_STARTUP;	}
	public Vector3f getHomePosition() {
		return new Vector3f(HOME_X,HOME_Y,HOME_Z);
	}
	
	public Vector3f getHomeForward() {
		return new Vector3f(HOME_FORWARD_X,HOME_FORWARD_Y,HOME_FORWARD_Z);
	}
	
	public Vector3f getHomeRight() {
		return new Vector3f(HOME_RIGHT_X,HOME_RIGHT_Y,HOME_RIGHT_Z);
	}

	public float getBaseToShoulderX() {	return BASE_TO_SHOULDER_X;	}
	public float getBaseToShoulderZ() { return BASE_TO_SHOULDER_Z;	}
	public float getShoulderToElbow() { return SHOULDER_TO_ELBOW;	}
	public float getElbowToWrist() { return ELBOW_TO_WRIST;	}
	public float getWristToFinger() { return WRIST_TO_FINGER;	}
	public float getBaseToShoulderMinimumLimit() { return BASE_TO_SHOULDER_MINIMUM_LIMIT;	}
	
	public void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(null, "<html><body>"
				+"<h1>Arm3</h1>"
				+"<p>Created by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A three axis robot arm, modelled after the ABB model IRB 460.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/arm3'>Click here for more details</a>.</p>"
				+"</body></html>", "About "+this.getName());
	}
	
	public String reportMove(Arm3MotionState arg0) {
		// TODO finish me, either with FK or IK values.
		// GO Xx Yy Zz
		return "";
	}
}
