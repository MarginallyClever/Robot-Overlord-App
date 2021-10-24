package com.marginallyclever.robotOverlord.physics;

import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.shape.Shape;

//https://en.wikipedia.org/wiki/Collision_response
public abstract class RigidBody extends PoseEntity {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Force> forces = new ArrayList<Force>();
	
	private Shape shape;
	private double mass=0;

	private Vector3d angularVelocity = new Vector3d();
	private Vector3d linearVelocity = new Vector3d();
	
	private Vector3d forceToApply = new Vector3d();
	private Vector3d torqueToApply = new Vector3d();
	
	private double coefficientOfRestitution = 0.75;
	private double coefficientOfFriction = 0.5;
		
	private boolean isPaused=false;
		
	public RigidBody() {
		this(RigidBody.class.getSimpleName());
	}

	public RigidBody(String name) {
		super(name);
	}
	
	@Override
	public void update(double dt) {
		if(isPaused) return;
		
		while(forces.size()>200) forces.remove(0);
		
		testFloorContact();
		if(isPaused) return;
		updateForces(dt);
		updateAngularPosition(dt);
		updateLinearPosition(dt);
	}

	private void updateForces(double dt) {
		addGravity();
		updateLinearForce(dt);
		updateAngularForce(dt);
	}

	private void addGravity() {
		forceToApply.add(new Vector3d(0,0,-9.8*mass));
	}

	private void updateAngularForce(double dt) {/*
		if(torqueToApply.lengthSquared()<1e-6) {
			torqueToApply.set(0,0,0);
			return;
		}*/
		if(shape==null) return;

		if(mass>0) torqueToApply.scale(1.0/mass);
		torqueToApply.scale(dt);
		angularVelocity.add(torqueToApply);
		torqueToApply.set(0,0,0);
	}
	
	private void updateLinearForce(double dt) {
		if(mass>0) forceToApply.scale(1.0/mass);
		forceToApply.scale(dt);
		linearVelocity.add(forceToApply);
		forceToApply.set(0,0,0);
	}

	private void updateAngularPosition(double dt) {
		// We can describe that spin as a vector w(t) The direction of w(t) gives the direction of 
		// the axis about which the body is spinning. The magnitude of w(t) tells how fast the body is spinning.
		Vector3d w = new Vector3d(angularVelocity);
		double len = w.length();
		if(len>0) w.normalize();
		
		Matrix3d m = new Matrix3d();
		Matrix3d rot = MatrixHelper.getMatrixFromAxisAndRotation(w,len);
		
		myPose.getRotationScale(m);
		m.mul(rot);
		myPose.setRotationScale(m);
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
		super.render(gl2);
		if(shape!=null) {
			gl2.glPushMatrix();
				MatrixHelper.applyMatrix(gl2, myPose);
				shape.render(gl2);
				PrimitiveSolids.drawStar(gl2,1);
			gl2.glPopMatrix();
			
			boolean wasLit = OpenGLHelper.disableLightingStart(gl2);
			drawVelocities(gl2);
			drawForces(gl2);
			OpenGLHelper.disableLightingEnd(gl2,wasLit);
		}
	}

	private void drawForces(GL2 gl2) {
		for( Force f : forces ) {
			f.render(gl2);
		}
	}

	private void drawVelocities(GL2 gl2) {
		gl2.glPushMatrix();
			Vector3d p = getPosition();
			gl2.glTranslated(p.x, p.y, p.z);

			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3d(1,0,0);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(
					linearVelocity.x,
					linearVelocity.y,
					linearVelocity.z);
			
			gl2.glColor3d(0,1,0);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(
					angularVelocity.x,
					angularVelocity.y,
					angularVelocity.z);
			
			gl2.glEnd();
		gl2.glPopMatrix();
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}
	
	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}

	private Matrix3d getInertiaTensorFromShape() {		
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
			inertiaTensor.setScale(mass/6);
			return inertiaTensor;
		}
	}

	private Matrix3d getInertiaTensorFromDimensions(double x, double y, double z) {
		Matrix3d it = new Matrix3d();
		it.m00=calculateMOI(y,z);
		it.m11=calculateMOI(x,z);
		it.m22=calculateMOI(x,y);
		return it;
	}

	private double calculateMOI(double a, double b) {
		return mass * ( a*a + b*b ) / 12;
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
	
	public void applyForceAtPoint(Vector3d force,Point3d p) {
		double len = force.length();
		if(len<1e-6) return;
		
		//forces.add(new Force(p,force,1,0,1));
		
		// linear component
		Vector3d forceScaled = new Vector3d(force);
		forceToApply.add(forceScaled);

		// angular component
		Vector3d n = new Vector3d(force);
		n.normalize();
		Vector3d newTorque = new Vector3d();
		newTorque.cross(getR(p),n);
		
		Matrix3d inertiaTensor = getInertiaTensor();
		inertiaTensor.invert();
		inertiaTensor.transform(newTorque);
		newTorque.scale(len);

		//forces.add(new Force(p,newTorque,0,1,1));

		torqueToApply.add(newTorque);
	}
	
	private Matrix3d getInertiaTensor() {
		Matrix3d inertiaTensor = getInertiaTensorFromShape();
		Matrix3d pose3 = new Matrix3d();
		myPose.get(pose3);
		inertiaTensor.mul(pose3,inertiaTensor);
		return inertiaTensor;
	}
	
	private Vector3d getR(Point3d p) {
		Vector3d r = new Vector3d(p);
		r.sub(getPosition());
		return r;
	}
	
	/**
	 * Vp = V + W x R
	 * Where 
	 * V is linear velocity, 
	 * W is the angular pre-collision velocity, and 
	 * R is the offset of the shared contact point
	 * @param p
	 * @return the sum of angular and linear velocity at point p.
	 */  
	protected Vector3d getCombinedVelocityAtPoint(Point3d p) {
		Vector3d sum = getR(p);
		sum.cross(angularVelocity,sum);
		sum.add(linearVelocity);
		
		//System.out.println(angularVelocity+"\t"+linearVelocity+"\t"+sum);
		//forces.add(new Force(p,sum,0,0,1));
		//forces.add(new Force(p,linearVelocity,0,1,0));
		
		return sum;
	}
	
	abstract protected void testFloorContact();

	// https://en.wikipedia.org/wiki/Collision_response
	protected void applyCollisionImpulse(Point3d p, Vector3d n) {
		double jr = getImpulseMagnitude(p,n,coefficientOfRestitution);/*

		Vector3d linearChange = getLinearCollisionResponse(n,jr);
		Vector3d angularChange = getAngularCollisionResponse(n,p,jr); 
		forces.add(new Force(p,linearChange,0,0,1));
		
		linearVelocity.sub(linearChange);
		angularVelocity.sub(angularChange);*/
		
		Vector3d f = new Vector3d(n);
		f.scale(-jr*mass);
		applyForceAtPoint(f,p);

		applyFriction(p,n);
	}
	
	private void applyFriction(Point3d p,Vector3d n) {
		Vector3d velocityAtPoint = getCombinedVelocityAtPoint(p);
		Vector3d relativeVelocity = new Vector3d(velocityAtPoint);
		double d = relativeVelocity.dot(n);
		// friction m
		if(d!=0) {
			Vector3d f2 = new Vector3d(n);
			Vector3d t = new Vector3d(relativeVelocity);
			f2.scale(d);
			t.sub(f2);
			if(t.lengthSquared()>1e-6) {
				//if(t.length()<coefficientOfFriction * d) {
					//t.normalize();
				t.scale(-coefficientOfFriction);
				//}
				forces.add(new Force(p,relativeVelocity,1,0,0));
				forces.add(new Force(p,t,1,1,0));
				applyForceAtPoint(t,p);
			}
		}		
	}
	
	private Vector3d getAngularCollisionResponse(Vector3d n, Point3d p, double jr) {
		Vector3d r = getR(p);
		Vector3d rxn = new Vector3d();
		rxn.cross(r,n);
		Matrix3d inertiaTensor = getInertiaTensor();
		inertiaTensor.invert();
		Vector3d irxn = new Vector3d();
		inertiaTensor.transform(rxn,irxn);
		
		Vector3d newAngularVelocity = new Vector3d(irxn);
		newAngularVelocity.scale(jr);
		return newAngularVelocity;
	}

	private Vector3d getLinearCollisionResponse(Vector3d n,double jr) {
		double s = jr/mass;
		Vector3d sum= new Vector3d( s*n.x,
									s*n.y,
									s*n.z );
		return sum;
	}

	private double getImpulseMagnitude(Point3d p, Vector3d n,double e) {
		Vector3d velocityAtPoint = getCombinedVelocityAtPoint(p);
		Vector3d relativeVelocity = new Vector3d(velocityAtPoint);
		relativeVelocity.scale(-1);
		
		//forces.add(new Force(p,velocityAtPoint,1,1,0));
				
		Vector3d r = getR(p);
		Vector3d rxn = new Vector3d();
		rxn.cross(r,n);
		Matrix3d inertiaTensor = getInertiaTensor();
		inertiaTensor.invert();
		Vector3d irxn = new Vector3d();
		inertiaTensor.transform(rxn, irxn);
		Vector3d irxnr = new Vector3d();
		irxnr.cross(irxn,r);
		
		double a = -(1.0 + e) * relativeVelocity.dot(n);
		double b = Math.pow(mass, -1) + irxnr.dot(n); 
		double jr = a/b;
		
		return jr;
	}
}
