package com.marginallyclever.robotOverlord.arm3.uArm;

import java.text.DecimalFormat;

import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.HTMLDialogBox;
import com.marginallyclever.robotOverlord.arm3.Arm3Dimensions;
import com.marginallyclever.robotOverlord.arm3.Arm3MotionState;

public class UArmDimensions extends Arm3Dimensions {
	// name
	final protected String ROBOT_NAME = "uArm / Lite Arm i2";
	// expected firmware starting message
	final protected static String HELLO = "HELLO WORLD! I AM UARM #";
	// expected firmware version
	final protected static int firmwareVersion = 1;
		
	// Machine dimensions
	final protected float BASE_TO_SHOULDER_X   =(1.985f);
	final protected float BASE_TO_SHOULDER_Z   =(5.864f);
	final protected float SHOULDER_TO_ELBOW    =(14.843125f);
	final protected float ELBOW_TO_WRIST       =(15.940625f);
	final protected float WRIST_TO_FINGER      =(5.735f);
	final protected float BASE_TO_SHOULDER_MINIMUM_LIMIT = 7.5f;
	
	// When the robot is homed, what are the XYZ coordinates of the finger tip?
	final protected float HOME_X = 19.0f+BASE_TO_SHOULDER_X+WRIST_TO_FINGER;
	final protected float HOME_Y = 0;
	final protected float HOME_Z = 5.864f;

	protected DecimalFormat df;
	
	// Dangerous!
	final boolean HOME_AUTOMATICALLY_ON_STARTUP = true;
	
	public UArmDimensions() {
		df = new DecimalFormat("0.00");
		df.setGroupingUsed(false);
	}
	
	@Override
	public String getName() {		return ROBOT_NAME;	}
	@Override
	public String getHello() {		return HELLO;	}
	@Override
	public boolean getHomeAutomaticallyOnStartup() {	return HOME_AUTOMATICALLY_ON_STARTUP;	}
	@Override
	public Vector3f getHomePosition() {		return new Vector3f(HOME_X,HOME_Y,HOME_Z);	}
	@Override
	public float getBaseToShoulderX() {	return BASE_TO_SHOULDER_X;	}
	@Override
	public float getBaseToShoulderZ() { return BASE_TO_SHOULDER_Z;	}
	@Override
	public float getShoulderToElbow() { return SHOULDER_TO_ELBOW;	}
	@Override
	public float getElbowToWrist() { return ELBOW_TO_WRIST;	}
	@Override
	public float getWristToFinger() { return WRIST_TO_FINGER;	}
	@Override
	public float getBaseToShoulderMinimumLimit() { return BASE_TO_SHOULDER_MINIMUM_LIMIT;	}
	
	@Override
	public void doAbout() {
		HTMLDialogBox box = new HTMLDialogBox();
		box.display(null, "<html><body>"
				+"<h1>uArm</h1>"
				+"<p>Created by uFactory.</p><br>"
				+"<p>Code for Robot Overlord by Dan Royer (dan@marginallyclever.com).</p><br>"
				+"<p>A three axis robot arm, modelled after the ABB model IRB 460.</p><br>"
				+"<p><a href='https://www.marginallyclever.com/product/uarm'>Click here for more details</a>.</p>"
				+"</body></html>", "About "+this.getName());
	}

	@Override
	public String reportMove(Arm3MotionState arg0) {
		float x = arg0.angleBase    +(         90.0f);
		float y = (-arg0.angleShoulder)+(54.54f-30);
		float z = 180.0f-(arg0.angleElbow-(130.673f-90.0f));

		System.out.println("Y="+y+" : "+df.format(y));
		if(y<0) y=0;
		if(y>180) y=180;
				
		return "G0 X"+df.format(x)+" Y"+df.format(y)+" Z"+df.format(z)+"\n";
	}
}
