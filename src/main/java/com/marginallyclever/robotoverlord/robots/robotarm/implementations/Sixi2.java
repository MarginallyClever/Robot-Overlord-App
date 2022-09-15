package com.marginallyclever.robotoverlord.robots.robotarm.implementations;

import com.marginallyclever.robotoverlord.entities.ShapeEntity;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmIK;

public class Sixi2 extends RobotArmIK {
	private static final long serialVersionUID = 1L;

	public Sixi2() {
		super();
		setName("robots/Sixi2");
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base", "/robots/Sixi2/anchor.obj"));
		
		// name d r a t max min file
		addBone(new RobotArmBone("X",18.8452,0     ,-90,  0,120,-120, "/robots/Sixi2/shoulder.obj"));
		addBone(new RobotArmBone("Y",0      ,35.796,  0,-90,  0,-170, "/robots/Sixi2/bicep.obj"));
		addBone(new RobotArmBone("Z",0      ,6.4259,-90,  0, 86,-83.369, "/robots/Sixi2/forearm.obj"));
		addBone(new RobotArmBone("U",38.705 ,0     , 90,  0,175,-175, "/robots/Sixi2/tuningFork.obj"));
		addBone(new RobotArmBone("V",0      ,0     ,-90,  0,120,-120, "/robots/Sixi2/picassoBox.obj"));
		addBone(new RobotArmBone("W",6.795  ,0     ,  0,  0,170,-170, "/robots/Sixi2/hand.obj"));
		
		adjustModelOriginsToDHLinks();		
		
		setTextureFilename("/robots/Sixi2/sixi.png");
	}
}
