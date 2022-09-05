package com.marginallyclever.robotoverlord.robots.robotarm.implementations;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmIK;
import com.marginallyclever.robotoverlord.mesh.ShapeEntity;

public class Sixi3_5axis extends RobotArmIK {
	private static final long serialVersionUID = 1L;

	public Sixi3_5axis() {
		super();
		setName(Sixi3_5axis.class.getSimpleName());
	}
		
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base","/Sixi3b/j0.obj"));
		// base: Mass 201.686g, Center of Mass -1.113, -0.011, 14.664

		// format: name d r alpha theta max min file
		addBone(new RobotArmBone("X", 8.01,     0,270,  0,170,-170,"/Sixi3b/j1.obj"));
		addBone(new RobotArmBone("Y",9.131,17.889,  0,270,370, 170,"/Sixi3b/j2.obj"));
		addBone(new RobotArmBone("Z",    0,12.435,  0,  0,150,-150,"/Sixi3b/j3.obj"));
		addBone(new RobotArmBone("U",    0,     0,270,270,440, 100,"/Sixi3b/j4.obj"));
		addBone(new RobotArmBone("V", 5.12,     0,  0,180,360,   0,"/Sixi3b/j5.obj"));
		//addBone(new RobotArmBone("W",    0,  5.12,  0,  0,350    ,10     ,"/Sixi3b/j6.obj"));

		// numbers from fusion360 file.
		
		this.getBone(0).setMass(870.643);  // g
		this.getBone(1).setMass(1818.102);
		this.getBone(2).setMass(928.555);
		this.getBone(3).setMass(870.643);
		this.getBone(4).setMass(27.664);

		this.getBone(0).setInertiaTensor(new Matrix3d(
				16426.868,		0.051,		-0.389,
				0.051,		6984.50,		-733.301,
				-0.389,		-733.301,		14884.64));  // g/cm^2
		this.getBone(1).setInertiaTensor(new Matrix3d(
				80600.063,		0.003,		9.619,
				0.003,		76663.265,		0.007,
				9.619,		0.007,		15428.804));
		this.getBone(2).setInertiaTensor(new Matrix3d(
				20890.707,		1.666,		-6.381,
				1.666,		19039.542,		2583.349,
				-6.381,		2583.349,		8263.735));
		this.getBone(3).setInertiaTensor(new Matrix3d(
				16426.871,		-0.54,		-0.333,
				-0.54,		6984.62,		-733.337,
				-0.333,		-733.337,		14884.759));
		this.getBone(4).setInertiaTensor(new Matrix3d(
				80.823,		0.372,		0.151,
				0.372,		81.594,		0.131,
				0.151,		0.131,		154.17));

		this.getBone(0).setCenterOfMass(new Point3d(0.002,-2.8793, 7.7502));  // cm
		this.getBone(1).setCenterOfMass(new Point3d(0.0015, 8.522, 16.9545));
		this.getBone(2).setCenterOfMass(new Point3d(0.0076, 0.8836, 34.1415));
		this.getBone(3).setCenterOfMass(new Point3d(0.0092, 12.0103, 38.5988));
		this.getBone(4).setCenterOfMass(new Point3d(-0.0042, 9.1191, 42.7917));
		
		adjustModelOriginsToDHLinks();
		adjustCenterOfMassToDHLinks();
		setTextureFilename("/Sixi3b/SIXI3_BASE.png");
	}
}
