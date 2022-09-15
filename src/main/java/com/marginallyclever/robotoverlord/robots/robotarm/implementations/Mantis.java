package com.marginallyclever.robotoverlord.robots.robotarm.implementations;

import com.marginallyclever.robotoverlord.entities.ShapeEntity;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmIK;

@Deprecated
public class Mantis extends RobotArmIK {
	private static final long serialVersionUID = 1L;

	public Mantis() {
		super();
		setName("Mantis");
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base", "/robots/AH/AH0.obj"));
		
		// name d r a t max min file
		addBone(new RobotArmBone("X",27.2   ,0      ,-90,  0,120,-120, "/robots/AH/AH1.obj"));
		addBone(new RobotArmBone("Y",0      ,22.5214,  0,-90,170,-170, "/robots/AH/AH2.obj"));
		addBone(new RobotArmBone("Z",0      ,0      ,-90,-90, 86, -91, "/robots/AH/AH3.obj"));
		addBone(new RobotArmBone("U",23.2325,0      , 90,  0, 90, -90, "/robots/AH/AH4.obj"));
		addBone(new RobotArmBone("V",0      ,0      ,-90,  0, 90, -90, "/robots/AH/AH5.obj"));
		addBone(new RobotArmBone("W",5.0    ,0      ,  0,  0,170,-170, "/robots/AH/AH6.obj"));

		setTextureFilename("/robots/AH/AH_BASE.png");
		adjustModelOriginsToDHLinks();
	}
}
