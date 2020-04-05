package com.marginallyclever.robotOverlord.entity.scene.robotEntity.olderModels.rotaryStewartPlatform;

public class RotaryStewartPlatform3Dimensions extends RotaryStewartPlatform2Dimensions {
	public final String ROBOT_NAME = "Stewart Platorm 3";
	public final String HELLO = "HELLO WORLD! I AM STEWART PLATFORM V4.2";

	// machine dimensions
	public final float BASE_TO_SHOULDER_X   =20.534f; //(21.5500f-1.016f);  // measured in fusion360, relative to base origin
	public final float BASE_TO_SHOULDER_Y   = 5.450f;
	public final float BASE_TO_SHOULDER_Z   = 4.925f;
	public final float BASE_ADJUST_Z        = 8.0952f;
	public final float STARTING_FINGER_Z    =43.7608f;
	public final float BICEP_LENGTH         =11.5f;
	public final float FOREARM_LENGTH       =36.83f;
	public final float WRIST_TO_FINGER_X    = 2.1f;
	public final float WRIST_TO_FINGER_Y    =15.561f;  //(13.9735f+3.175f/2);
	public final float WRIST_TO_FINGER_Z    = 3.4f;  // measured in fusion360, relative to finger origin
}
