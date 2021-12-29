package com.marginallyclever.robotOverlord.physics.original;

import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.PoseEntity;
import com.marginallyclever.robotOverlord.shape.Shape;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.BooleanEntity;

// See https://en.wikipedia.org/wiki/Collision_response
// See https://www.euclideanspace.com/physics/dynamics/collision/
public abstract class RigidBody extends PoseEntity {
	private static final long serialVersionUID = 1L;

	private static final int FORCE_RECORD_LENGTH = 1;
	
	private ArrayList<Force> forces = new ArrayList<Force>();
	
	private Shape shape;
	private double mass=0;

	private Vector3d angularVelocity = new Vector3d();
	private Vector3d linearVelocity = new Vector3d();
	private Vector3d forceToApply = new Vector3d();
	private Vector3d torqueToApply = new Vector3d();
	
	private BooleanEntity pauseOnCollision = new BooleanEntity("Pause on collision",true);
	private BooleanEntity applyGravity = new BooleanEntity("Apply gravity",true);
	
	private double coefficientOfRestitution = 0.75;
	private double coefficientOfFriction = 0.0;
		
	private boolean isPaused=false;

	public RigidBody() {
		this(RigidBody.class.getSimpleName());
	}

	public RigidBody(String name) {
		super(name);
	}
	
	public void setPauseOnCollision(boolean b) {
		pauseOnCollision.set(b);
	}
	public void setApplyGravity(boolean b) {
		applyGravity.set(b);
	}
	
	@Override
	public void update(double dt) {
		if(isPaused) return;
		
		limitForceRecordLength();

		updateForces(dt);
		updateAngularPosition(dt);
		updateLinearPosition(dt);
		testFloorContact();
	}

	private void limitForceRecordLength() {
		while(forces.size()>=FORCE_RECORD_LENGTH) forces.remove(0);
	}

	private void updateForces(double dt) {
		if(applyGravity.get()) addGravity();
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

		//torqueToApply.scale(dt/mass);
		angularVelocity.add(torqueToApply);
		torqueToApply.set(0,0,0);
	}
	
	
	private void updateLinearForce(double dt) {
		//forceToApply.scale(dt/mass);
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

			int a = OpenGLHelper.drawAtopEverythingStart(gl2);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3d(0,1,1);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(
					linearVelocity.x,
					linearVelocity.y,
					linearVelocity.z);
			
			gl2.glColor3d(1,0,1);
			gl2.glVertex3d(0, 0, 0);
			gl2.glVertex3d(
					angularVelocity.x,
					angularVelocity.y,
					angularVelocity.z);
			gl2.glEnd();
			OpenGLHelper.drawAtopEverythingEnd(gl2,a);
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

	protected abstract Matrix3d getInertiaTensorFromShape();
	
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
		
		forces.add(new Force(p,force,1,0,1));
		
		// linear component
		forceToApply.add(force);

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
		getPoseWorld().get(pose3);
		inertiaTensor.mul(pose3,inertiaTensor);
		return inertiaTensor;
	}
		
	private Vector3d getR(Point3d p) {
		Vector3d r = new Vector3d();
		r.sub(p,MatrixHelper.getPosition(getPoseWorld()));
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
		
		//Log.message(angularVelocity+"\t"+linearVelocity+"\t"+sum);
		//forces.add(new Force(p,sum,0,0,1));
		//forces.add(new Force(p,linearVelocity,0,1,0));

		return sum;
	}
	
	abstract protected void testFloorContact();

	// https://en.wikipedia.org/wiki/Collision_response
	protected void applyCollisionImpulse(Point3d p, Vector3d normal) {
		if(pauseOnCollision.get())
			isPaused=true;
		forces.add(new Force(p,normal,0,0,0));

		Vector3d rb = getR(p);
		Vector3d angularVelChangeB = new Vector3d();
		angularVelChangeB.cross(normal,rb);
		
		Matrix3d inertiaTensorB = getInertiaTensor();
		inertiaTensorB.invert();
		inertiaTensorB.transform(angularVelChangeB);
		
		Vector3d vbLinDueToR = new Vector3d();
		vbLinDueToR.cross(angularVelChangeB,rb);
		
		double scalar = 0;//1 / massA + vaLinDueToR.dot(n);
		scalar += 1 / mass + vbLinDueToR.dot(normal);

		Vector3d dv = new Vector3d();
		dv.sub(new Vector3d(0,0,0),linearVelocity);
		
		double jMod = (coefficientOfRestitution + 1) * dv.length() / scalar;
		Vector3d j = new Vector3d(normal);
		j.scale(jMod);
		
		Vector3d jb = new Vector3d(j);
		jb.scale(30);
		jb.scale(1.0/mass);
		forceToApply.sub(jb);
		angularVelChangeB.scale(30);
		torqueToApply.sub(angularVelChangeB);
		
		//applyFriction(p,normal);
	}
	
	@SuppressWarnings("unused")
	private void applyFriction(Point3d p,Vector3d n) {
		if(coefficientOfFriction<=0) return;
		
		Vector3d velocityAtPoint = getCombinedVelocityAtPoint(p);
		Vector3d relativeVelocity = new Vector3d(velocityAtPoint);
		double d = relativeVelocity.dot(n);
		// friction m
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
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Ph", "Physics");
		view.add(pauseOnCollision);
		view.addButton("Unpause").addPropertyChangeListener( (evt) -> isPaused=false );
		view.popStack();
		shape.getView(view);
		super.getView(view);
	}
	
	public boolean isPaused() {
		return isPaused;
	}
	
	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}
}
