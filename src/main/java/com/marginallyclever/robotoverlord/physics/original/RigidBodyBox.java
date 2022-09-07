package com.marginallyclever.robotoverlord.physics.original;

import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.entities.ShapeEntity;

//https://en.wikipedia.org/wiki/Collision_response
public class RigidBodyBox extends RigidBody {
	private static final long serialVersionUID = 1L;
	
	public RigidBodyBox() {
		super(RigidBodyBox.class.getSimpleName());
	}

	public RigidBodyBox(String name) {
		super(name);
	}

	@Override
	protected Matrix3d getInertiaTensorFromShape() {	
		ShapeEntity shape = getShape();
		ArrayList<Cuboid> list = shape.getCuboidList();
		if(!list.isEmpty()) {
			Cuboid cuboid = list.get(0);
			double x = cuboid.getExtentX();
			double y = cuboid.getExtentY();
			double z = cuboid.getExtentZ();
			
			return getInertiaTensorFromDimensions(x,y,z);
		} else {
			Matrix3d inertiaTensor = new Matrix3d();
			inertiaTensor.setIdentity();
			inertiaTensor.setScale(getMass()/6);
			return inertiaTensor;
		}
	}

	private Matrix3d getInertiaTensorFromDimensions(double x, double y, double z) {
		Matrix3d it = new Matrix3d();
		it.m00=calculateMOI(y,z);
		it.m11=calculateMOI(x,z);
		it.m22=	calculateMOI(x,y);
		return it;
	}

	private double calculateMOI(double a, double b) {
		return getMass() * ( a*a + b*b ) / 12;
	}

	// assumes ground plane is body 1.
	@Override
	protected void testFloorContact() {
		ShapeEntity shape = getShape();
		ArrayList<Cuboid> list = shape.getCuboidList();
		if(!list.isEmpty()) {
			Cuboid cuboid = list.get(0);
			Point3d [] corners = PrimitiveSolids.get8PointsOfBox(cuboid.getBoundsBottom(),cuboid.getBoundsTop());
			for( Point3d p : corners ) {
				myPose.transform(p);
				if(p.z<0) {
					// hit floor
					// don't be in the floor 
					double d = p.z;
					myPose.m23-=d;
					for( Point3d p2 : corners ) {
						p2.z-=d;
					}
					
					// now deal with the hit
					Vector3d velocityAtPoint = getCombinedVelocityAtPoint(p);
					Vector3d n = new Vector3d(0,0,1);
					double vn = velocityAtPoint.dot(n);
					if(vn<0) {
						applyCollisionImpulse(p,n);
					}
					//isPaused=true;
				}
			}
		}
	}
}
