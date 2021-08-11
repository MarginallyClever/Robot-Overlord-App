package com.marginallyclever.robotOverlord.physics;

import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.shape.Shape;

public class RigidBody extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Shape shape;

	private double mass=0;
	private Matrix3d inertiaTensor = new Matrix3d();

	private Vector3d angularVelocity = new Vector3d();
	private Vector3d linearVelocity = new Vector3d();
	
	public RigidBody() {
		this("RigidBody");
	}
	public RigidBody(String name) {
		super(name);
	}
	
	@Override
	public void update(double dt) {
		updateLinearPosition(dt);
		updateAngularPosition(dt);
	}

	private void updateAngularPosition(double dt) {
		
	}

	private void updateLinearPosition(double dt) {
		Vector3d p = getPosition();
		Vector3d dp = new Vector3d(linearVelocity);
		dp.scale(dt);
		p.add(dp);
		setPosition(p);
	}

	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);
			shape.render(gl2);
		gl2.glPopMatrix();
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
		if(shape!=null) updateInertiaTensorFromShape();
	}
	
	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
		if(shape!=null) updateInertiaTensorFromShape();
	}
	
	private void updateInertiaTensorFromShape() {		
		ArrayList<Cuboid> list = shape.getCuboidList();
		if(!list.isEmpty()) {
			Cuboid cuboid = list.get(0);
			double width = cuboid.getExtentX();
			double length = cuboid.getExtentY();
			double height = cuboid.getExtentZ();
			
			updateInertiaTensor(width,length,height);
		}
	}

	private void updateInertiaTensor(double width, double length, double height) {
		double ixx = mass*calculateMOI(width,length);
		double iyy = mass*calculateMOI(width,height);
		double izz = mass*calculateMOI(length,height);
		
		double ixy = -mass*width*length;
		double ixz = -mass*width*height;
		double iyz = -mass*length*height;
		
		inertiaTensor.m00=ixx;
		inertiaTensor.m01=ixy;
		inertiaTensor.m02=ixz;

		inertiaTensor.m10=ixy;
		inertiaTensor.m11=iyy;
		inertiaTensor.m12=iyz;
		
		inertiaTensor.m20=ixz;
		inertiaTensor.m21=iyz;
		inertiaTensor.m22=izz;
	}

	private double calculateMOI(double width, double height) {
		return ( width*width + height*height ) / 12;
	}

	public void setLinearVelocity(Vector3d arg0) {
		linearVelocity.set(arg0);
	}

	public Vector3d getLinearVelocity() {
		return linearVelocity;
	}

	public void setAngularVelocity(Vector3d arg0) {
		angularVelocity.set(arg0);
	}

	public Vector3d getAngularVelocity() {
		return angularVelocity;
	}
}
