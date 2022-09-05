package com.marginallyclever.robotoverlord.robots.robotarm;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.robots.robotarm.implementations.Sixi3_6axis;

public class RobotArmTests {
	@Test
	public void saveAndLoad() throws Exception {
		Log.start();
		Sixi3_6axis arm = new Sixi3_6axis();
		File f = File.createTempFile("test", ".urdf");
		Log.message(f.getAbsolutePath());
		RobotArmSaveToURDF saver = new RobotArmSaveToURDF();
		saver.save(f.getAbsolutePath(), arm);
		
		Log.end();
	}
}
