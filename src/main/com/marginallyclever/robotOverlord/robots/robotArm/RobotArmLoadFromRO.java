package com.marginallyclever.robotOverlord.robots.robotArm;

import java.io.File;
import java.io.FileReader;

public class RobotArmLoadFromRO {
	RobotArmFK arm;
	
	public RobotArmFK load(String filePath) throws Exception {
		RobotArmFK arm = new RobotArmFK();
		
		FileReader f = new FileReader(new File(filePath));
		
		
		return arm;
	}
}
