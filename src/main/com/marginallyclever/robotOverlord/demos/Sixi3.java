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
		setBaseShape(new Shape("Base","/Sixi3b/j0.obj"));
		
		// name d r a t max min file
		addBone(new RobotArmBone("X", 8.01,     0,270,  0,170,-170,"/Sixi3b/j1.obj"));
		addBone(new RobotArmBone("Y",9.131,17.889,  0,270,370, 170,"/Sixi3b/j2.obj"));
		addBone(new RobotArmBone("Z",    0,12.435,  0,  0,150,-150,"/Sixi3b/j3.obj"));
		addBone(new RobotArmBone("U",    0,     0,270,270,440, 100,"/Sixi3b/j4.obj"));
		addBone(new RobotArmBone("V", 5.12,     0,  0,180,360,   0,"/Sixi3b/j5.obj"));
		//addBone(new RobotArmBone("W",    0,  5.12,  0,  0,350    ,10     ,"/Sixi3b/j6.obj"));

		setTextureFilename("/Sixi3b/SIXI3_BASE.png");
		adjustModelOriginsToDHLinks();		
	}
}
