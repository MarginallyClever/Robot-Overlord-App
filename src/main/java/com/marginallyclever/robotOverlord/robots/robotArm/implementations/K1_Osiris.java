package com.marginallyclever.robotOverlord.robots.robotArm.implementations;

import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmBone;
import com.marginallyclever.robotOverlord.robots.robotArm.RobotArmIK;
import com.marginallyclever.robotOverlord.shape.ShapeEntity;

public class K1_Osiris extends RobotArmIK {
	private static final long serialVersionUID = 1L;

	public K1_Osiris() {
		super();
		setName(K1_Osiris.class.getSimpleName());
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base","/K1/Base v1.obj"));
		// base 
		// Mass	201.686 g
		// Center of Mass	-1.113, -0.011, 14.664

		// name d r alpha theta thetaMax thetaMin modelFile
		addBone(new RobotArmBone("X",25.667,     1.187,-90,180,270.0,  10.0,"/K1/A1 v1.obj"));
		addBone(new RobotArmBone("Y",   0.0,  17.458,  0,-90,180.0,-180.0,"/K1/A2 v1.obj"));
		addBone(new RobotArmBone("Z",   0.0,   0.0, 90,  0,180.0,-180.0,"/K1/A3 v1.obj"));
		addBone(new RobotArmBone("U", 21.60,   0.398, -90,  0,180.0,-180.0,"/K1/A4 v1.obj"));
		addBone(new RobotArmBone("V",     0,   0,90,  90,180.0,-180.0,"/K1/A5 v1.obj"));
		//addBone(new RobotArmBone("W",   6.3,   0.0,  -90,  180,  180.0,   180.0,null));

		setTextureFilename("/K1/K1.png");

		adjustModelOriginsToDHLinks();
		adjustCenterOfMassToDHLinks();
	}

	@Override
	public void update(double dt) {
		super.update(dt);
	}
}
