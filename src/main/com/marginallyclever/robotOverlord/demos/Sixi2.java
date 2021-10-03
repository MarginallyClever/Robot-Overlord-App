package com.marginallyclever.robotOverlord.demos;

import com.marginallyclever.robotOverlord.robots.sixi3.RobotArmBone;
import com.marginallyclever.robotOverlord.robots.sixi3.RobotArmFK;
import com.marginallyclever.robotOverlord.shape.Shape;

public class Sixi2 extends RobotArmFK {
	private static final long serialVersionUID = 1L;

	public Sixi2() {
		super();
		setName("Sixi2");
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new Shape("Base","/Sixi2/anchor.obj"));
		
		// name d r a t max min file
		addBone(new RobotArmBone("X",18.8452    ,0     ,-90,  0,120,-120,   "/Sixi2/shoulder.obj"));
		addBone(new RobotArmBone("Y",0          ,35.796,  0,-90,  0,-170,   "/Sixi2/bicep.obj"));
		addBone(new RobotArmBone("Z",0          ,6.4259,-90,  0, 86,-83.369,"/Sixi2/forearm.obj"));
		addBone(new RobotArmBone("U",29.355+9.35,0     , 90,  0,175,-175,   "/Sixi2/tuningFork.obj"));
		addBone(new RobotArmBone("V",0          ,0     ,-90,  0,120,-120,   "/Sixi2/picassoBox.obj"));
		addBone(new RobotArmBone("W",6.795      ,0     ,  0,  0,170,-170,   "/Sixi2/hand.obj"));
		
		adjustModelOriginsToDHLinks();		
		
		setTextureFilename("/Sixi2/sixi.png");
	}
}
