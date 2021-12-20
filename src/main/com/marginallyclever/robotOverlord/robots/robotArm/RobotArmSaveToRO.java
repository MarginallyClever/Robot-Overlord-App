package com.marginallyclever.robotOverlord.robots.robotArm;

import com.marginallyclever.convenience.log.Log;

/**
 * Export the {@code RobotArm} as a Robot Overlord test file
 * @author Dan Royer
 *
 */
public class RobotArmSaveToRO {	
	public void save(String filePath,RobotArmFK arm) throws Exception {
        Log.message(RobotArmSaveToRO.class.getSimpleName()+".save() start");

        //FileWriter f = new FileWriter(new File(filePath));
        //f.write(arm.toString());
        Log.message(arm.toString());
        Log.message(RobotArmSaveToRO.class.getSimpleName()+".save() done");
	}
}
