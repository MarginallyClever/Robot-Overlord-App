package com.marginallyclever.robotOverlord.demos.robotArms;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;

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
		/* base 
		 * Mass	201.686 g
		 * Center of Mass	-1.113, -0.011, 14.664
		 * moi Ixx	1.553E+05
		 * moi Ixy	-3.935
		 * moi Ixz	-2210.277
		 * moi Iyx	-3.935
		 * moi Iyy	1.498E+05
		 * moi Iyz	-4.605
		 * moi Izx	-2210.277
		 * moi Izy	-4.605
		 * moi Izz	2.620E+05
		 */

		// name d r a t max min file
		addBone(new RobotArmBone("X", 8.01,     0,270,  0,170,-170,"/Sixi3b/j1.obj"));
		addBone(new RobotArmBone("Y",9.131,17.889,  0,270,370, 170,"/Sixi3b/j2.obj"));
		addBone(new RobotArmBone("Z",    0,12.435,  0,  0,150,-150,"/Sixi3b/j3.obj"));
		addBone(new RobotArmBone("U",    0,     0,270,270,440, 100,"/Sixi3b/j4.obj"));
		addBone(new RobotArmBone("V", 5.12,     0,  0,180,360,   0,"/Sixi3b/j5.obj"));
		//addBone(new RobotArmBone("W",    0,  5.12,  0,  0,350    ,10     ,"/Sixi3b/j6.obj"));

		// numbers from fusion360 file.
		this.getBone(0).setMass(870.643);  // kg
		this.getBone(0).setCenterOfMass(new Point3d(0.02,-28.793,77.502));
		this.getBone(0).setInertiaTensor(new Matrix3d(
				1.643E+06,			5.10,				-38.868,
				5.10,				6.984E+05,			-73330.099,
				-38.868,			-73330.099,			1.488E+06));

		this.getBone(1).setMass(1818.102);  // g
		this.getBone(1).setCenterOfMass(new Point3d(0.015, 85.22, 169.545));
		this.getBone(1).setInertiaTensor(new Matrix3d(
				8.060E+06,			0.321,				961.936,
				0.321,				7.666E+06,			0.747,
				961.936,			0.747,				1.543E+06));
		
		this.getBone(2).setMass(928.555);  // g
		this.getBone(2).setCenterOfMass(new Point3d(0.076, 8.836,341.415));
		this.getBone(2).setInertiaTensor(new Matrix3d(
				2.089E+06,			166.563,			-638.127,
				166.563,			1.904E+06,			2.583E+05,
				-638.127,			2.583E+05,			8.264E+05));
		
		this.getBone(3).setMass(870.643);  // g
		this.getBone(3).setCenterOfMass(new Point3d(0.092, 120.103, 385.988));
		this.getBone(3).setInertiaTensor(new Matrix3d(
				1.643E+06,			-54.009,			-33.295,
				-54.009,			6.985E+05,			-73333.678,
				-33.295,			-73333.678,			1.488E+06));

		this.getBone(4).setMass(27.664);  // g
		this.getBone(4).setCenterOfMass(new Point3d(-0.042, 91.191, 427.917));
		this.getBone(4).setInertiaTensor(new Matrix3d(
				8082.34,			37.241,				15.101,
				37.241,				8159.352,			13.111,
				15.101,				13.111,				15417.036));
		
		adjustModelOriginsToDHLinks();
		adjustCenterOfMassToDHLinks();
		setTextureFilename("/Sixi3b/SIXI3_BASE.png");
	}
}
