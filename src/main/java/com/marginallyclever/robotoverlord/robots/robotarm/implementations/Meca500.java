package com.marginallyclever.robotoverlord.robots.robotarm.implementations;

import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmIK;
import com.marginallyclever.robotoverlord.entities.ShapeEntity;

public class Meca500 extends RobotArmIK {
	private static final long serialVersionUID = 1L;

	public Meca500() {
		super();
		setName(Meca500.class.getSimpleName());
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base","/meca500/j0.obj"));
		// base 
		// Mass	201.686 g
		// Center of Mass	-1.113, -0.011, 14.664

		// name d r alpha theta thetaMax thetaMin modelFile
		addBone(new RobotArmBone("X",  13.5,     0,-90,  0,175,-175,"/meca500/j1.obj"));
		addBone(new RobotArmBone("Y",     0,  13.5,  0,-90,90-90,-90-70,"/meca500/j2.obj"));
		addBone(new RobotArmBone("Z",     0,   3.8, 90,  0,55+70,55-135,"/meca500/j3.obj"));
		addBone(new RobotArmBone("U",  12.0,     0, 90,  0,170, -170,"/meca500/j4.obj"));
		addBone(new RobotArmBone("V",     0,     0,-90,  0,115,-115,"/meca500/j5.obj"));
		addBone(new RobotArmBone("W",   7.0,     0,  0,180,720,-720,"/meca500/j6.obj"));
		
		adjustModelOriginsToDHLinks();
		adjustCenterOfMassToDHLinks();
	}
}
