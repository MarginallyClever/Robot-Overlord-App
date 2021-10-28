package com.marginallyclever.robotOverlord.physics;

import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.robotOverlord.demos.demoAssets.Sphere;
import com.marginallyclever.robotOverlord.shape.Shape;

//https://en.wikipedia.org/wiki/Collision_response
public class RigidBodySphere extends RigidBody {
	private static final long serialVersionUID = 1L;
	
	public RigidBodySphere() {
		this(RigidBodySphere.class.getSimpleName());
	}

	public RigidBodySphere(String name) {
		super(name);
	}
		
	protected Matrix3d getInertiaTensorFromShape() {
		Matrix3d inertiaTensor = new Matrix3d();
		inertiaTensor.setIdentity();
		
		Shape shape = getShape();
		if( shape instanceof Sphere ) {
			double r = ((Sphere)shape).getRadius();	
			inertiaTensor.setScale(getMass() * 2.0/5.0 * r*r);
		}
		return inertiaTensor;
	}
		
	@Override
	protected void testFloorContact() {
		Shape shape = getShape();
		ArrayList<Cuboid> list = shape.getCuboidList();
		if(!list.isEmpty()) {
			if( !(shape instanceof Sphere ) ) return;
				
			Point3d p = new Point3d(this.getPosition());
			p.z-=((Sphere)shape).getRadius();
			if(p.z<0) {
				// hit floor
				// don't be in the floor 
				myPose.m23-=p.z;
				p.z=0;
				// now deal with the hit
				Vector3d velocityAtPoint = getCombinedVelocityAtPoint(p);
				Vector3d n = new Vector3d(0,0,1);
				double vn = velocityAtPoint.dot(n);
				if(vn<0) {
					applyCollisionImpulse(p,n);
				}
				
			}
		}
	}
}
