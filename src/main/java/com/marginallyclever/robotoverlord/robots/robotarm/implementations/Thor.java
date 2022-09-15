package com.marginallyclever.robotoverlord.robots.robotarm.implementations;

import com.marginallyclever.robotoverlord.entities.ShapeEntity;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmIK;

@Deprecated
public class Thor extends RobotArmIK {
	private static final long serialVersionUID = 1L;

	public Thor() {
		super();
		setName("robots/Thor");
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base", "/robots/Thor/Thor0.obj"));
		
		// name d r a t max min file
		addBone(new RobotArmBone("X",20.2,0 ,-90,  0,120,-120, "/robots/Thor/Thor1.obj"));
		addBone(new RobotArmBone("Y",0   ,16,  0,-90,170,-170, "/robots/Thor/Thor2.obj"));
		addBone(new RobotArmBone("Z",0   ,0 ,-90,-90, 86, -91, "/robots/Thor/Thor3.obj"));
		addBone(new RobotArmBone("U",19.5,0 , 90,  0, 90, -90, "/robots/Thor/Thor4.obj"));
		addBone(new RobotArmBone("V",0   ,0 ,-90,  0, 90, -90, "/robots/Thor/Thor5.obj"));
		addBone(new RobotArmBone("W",5.35,0 ,  0,  0,170,-170, "/robots/Thor/Thor6.obj"));

		setTextureFilename("/robots/Thor/THOR_BASE.png");
		adjustModelOriginsToDHLinks();
	}
}
