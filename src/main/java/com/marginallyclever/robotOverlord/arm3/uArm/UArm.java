package com.marginallyclever.robotOverlord.arm3.uArm;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.arm3.Arm3;
import com.marginallyclever.robotOverlord.arm3.Arm3ControlPanel;

public class UArm extends Arm3 {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static public String ROBOT_NAME = "uArm";
	protected final static String hello = "HELLO WORLD! I AM UARM #";
	
	// Machine dimensions
	final static public float BASE_TO_SHOULDER_X   =(5.37f);  // measured in solidworks
	final static public float BASE_TO_SHOULDER_Z   =(9.55f);  // measured in solidworks
	final static public float SHOULDER_TO_ELBOW    =(25.0f);
	final static public float ELBOW_TO_WRIST       =(25.0f);
	final static public float WRIST_TO_FINGER      =(4.0f);
	final static public float BASE_TO_SHOULDER_MINIMUM_LIMIT = 7.5f;


	public UArm() {
		super();

		setDisplayName(ROBOT_NAME);
	}


	protected Arm3ControlPanel createArm3ControlPanel(RobotOverlord gui) {
		return new UArmControlPanel(gui,this);
	}
}
