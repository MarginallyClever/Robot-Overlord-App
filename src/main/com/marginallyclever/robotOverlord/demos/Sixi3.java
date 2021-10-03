package com.marginallyclever.robotOverlord.demos;

import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmBone;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmFK;
import com.marginallyclever.robotOverlord.shape.Shape;

public class Sixi3 extends RobotArmFK {
	private static final long serialVersionUID = 1L;

	public Sixi3() {
		super();
		setName("Sixi3");
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new Shape("Base","/Sixi3b/base.3mf"));
		
		// name d r a t max min file
		addBone(new RobotArmBone("X", 8.01,     0,270,  0,170    ,-170   ,"/Sixi3b/j0.3mf"));
		addBone(new RobotArmBone("Y",9.131,17.889,  0,270,270+100,270-100,"/Sixi3b/j1.3mf"));
		addBone(new RobotArmBone("Z",    0,12.435,  0,  0,0+150  ,0-150  ,"/Sixi3b/j2.3mf"));
		addBone(new RobotArmBone("U",    0,     0,270,270,270+170,270-170,"/Sixi3b/j3.3mf"));
		addBone(new RobotArmBone("V", 5.12,     0,  0,180,360    ,0      ,"/Sixi3b/j4.3mf"));
		//addBone(new RobotArmBone("W",    0,  5.12,  0,  0,350    ,10     ,"/Sixi3b/j5.3mf"));
		
		adjustModelOriginsToDHLinks();		
	}
}
