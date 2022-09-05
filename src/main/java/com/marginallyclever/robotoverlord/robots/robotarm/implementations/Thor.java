package com.marginallyclever.robotoverlord.robots.robotarm.implementations;

import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmIK;
import com.marginallyclever.robotoverlord.mesh.ShapeEntity;

public class Thor extends RobotArmIK {
	private static final long serialVersionUID = 1L;

	public Thor() {
		super();
		setName("Thor");
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base","/Thor/Thor0.obj"));
		
		// name d r a t max min file
		addBone(new RobotArmBone("X",20.2,0 ,-90,  0,120,-120,"/Thor/Thor1.obj"));
		addBone(new RobotArmBone("Y",0   ,16,  0,-90,170,-170,"/Thor/Thor2.obj"));
		addBone(new RobotArmBone("Z",0   ,0 ,-90,-90, 86, -91,"/Thor/Thor3.obj"));
		addBone(new RobotArmBone("U",19.5,0 , 90,  0, 90, -90,"/Thor/Thor4.obj"));
		addBone(new RobotArmBone("V",0   ,0 ,-90,  0, 90, -90,"/Thor/Thor5.obj"));
		addBone(new RobotArmBone("W",5.35,0 ,  0,  0,170,-170,"/Thor/Thor6.obj"));

		setTextureFilename("/Thor/THOR_BASE.png");
		adjustModelOriginsToDHLinks();
	}
}
