package com.marginallyclever.robotoverlord.robots.robotarm.implementations;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmBone;
import com.marginallyclever.robotoverlord.robots.robotarm.RobotArmIK;
import com.marginallyclever.robotoverlord.mesh.ShapeEntity;

public class Sixi3_6axis extends RobotArmIK {
	private static final long serialVersionUID = 1L;

	public Sixi3_6axis() {
		super();
		setName(Sixi3_6axis.class.getSimpleName());
	}
	
	@Override
	protected void loadModel() {
		setBaseShape(new ShapeEntity("Base","/Sixi3b/j0.obj"));
		// base 
		// Mass	201.686 g
		// Center of Mass	-1.113, -0.011, 14.664

		// name d r alpha theta thetaMax thetaMin modelFile
		addBone(new RobotArmBone("X", 7.974,     0,270,  0,170,-170,"/Sixi3b/j1.obj"));
		addBone(new RobotArmBone("Y", 9.131,17.889,  0,270,370, 170,"/Sixi3b/j2.obj"));
		addBone(new RobotArmBone("Z",     0,12.435,  0,  0,150,-150,"/Sixi3b/j3.obj"));
		addBone(new RobotArmBone("U",     0,     0,270,270,440, 100,"/Sixi3b/j4-6.obj"));
		addBone(new RobotArmBone("V",15.616,     0, 90, 90,90+180,90-180,"/Sixi3b/j5-6.obj"));
		addBone(new RobotArmBone("W",  5.15,     0,  0, 180,360,   0,"/Sixi3b/j6-6.obj"));

		// numbers from fusion360 file.
		
		this.getBone(0).setMass(870.643);  // g
		this.getBone(1).setMass(1818.102);
		this.getBone(2).setMass(928.555);
		this.getBone(3).setMass(870.643);
		this.getBone(4).setMass(924.069);
		this.getBone(5).setMass(27.664);
		
		this.getBone(0).setInertiaTensor(new Matrix3d(
				16426.868,		0.051,		-0.389,
				0.051,		  6984.50,		-733.301,
				-0.389,		 -733.301,		14884.64));  // g/cm^2
		this.getBone(1).setInertiaTensor(new Matrix3d(
				80600.063,		0.003,		9.619,
				0.003,		76663.265,		0.007,
				9.619,			0.007,		15428.804));
		this.getBone(2).setInertiaTensor(new Matrix3d(
				20890.707,		1.666,		-6.381,
				1.666,		19039.542,		2583.349,
				-6.381,		 2583.349,		8263.735));
		this.getBone(3).setInertiaTensor(new Matrix3d(
				16426.871,		-0.54,		-0.333,
				-0.54,		  6984.62,		-733.337,
				-0.333,		 -733.337,		14884.759));
		this.getBone(4).setInertiaTensor(new Matrix3d(
				16387.344,		-0.438,	-473.132,
				-0.438,		17933.40,	-3.32,
				-473.132,		-3.32,	7482.678));
		this.getBone(5).setInertiaTensor(new Matrix3d(
				154.17,			-0.01,		-0.209,
				-0.01,			80.836,		0.385,
				-0.209,			0.385,		81.581));

		this.getBone(0).setCenterOfMass(new Point3d(0.002,-2.8793, 7.7502));  // cm
		this.getBone(1).setCenterOfMass(new Point3d(0.0015, 8.522, 16.9545));
		this.getBone(2).setCenterOfMass(new Point3d(0.0076, 0.8836, 34.1415));
		this.getBone(3).setCenterOfMass(new Point3d(0.0092, 12.0103, 38.5988));
		this.getBone(4).setCenterOfMass(new Point3d(-0.142, 9.133, 47.539));
		this.getBone(5).setCenterOfMass(new Point3d(-4.454, 9.134, 53.946));
		
		adjustModelOriginsToDHLinks();
		adjustCenterOfMassToDHLinks();
		setTextureFilename("/Sixi3b/SIXI3_BASE.png");
	}
}
