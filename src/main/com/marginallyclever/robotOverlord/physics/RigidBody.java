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
public class RigidBody extends PoseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	class Force {
		public Point3d p = new Point3d();
		public Vector3d f = new Vector3d();
		public double r, g, b;

		public Force(Point3d p0, Vector3d f0, double rr, double gg, double bb) {
			p.set(p0);
			f.set(f0);
			r=rr;
			g=gg;
			b=bb;
		}
		
		public void render(GL2 gl2) {
			gl2.glPushMatrix();
				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3d(r,g,b);
				gl2.glVertex3d(p.x    ,p.y    ,p.z    );
				gl2.glVertex3d(p.x+f.x,p.y+f.y,p.z+f.z);
				gl2.glEnd();
			gl2.glPopMatrix();
		}
	};
	
	private ArrayList<Force> forces = new ArrayList<Force>();
	
	private Shape shape;
	private double mass=0;

	private Vector3d angularVelocity = new Vector3d();
	private Vector3d linearVelocity = new Vector3d();
	
	private Vector3d forceToApply = new Vector3d();
	private Vector3d torqueToApply = new Vector3d();
		
	private boolean isPaused=false;
	
	
	public RigidBody() {
		this("RigidBody");
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
		updateLinearPosition(dt);
		updateAngularPosition(dt);
	}


	private void updateForces(double dt) {
		addGravity();
		updateLinearForce(dt);
		updateAngularForce(dt);
	}

	private void addGravity() {
		forceToApply.add(new Vector3d(0,0,-9.8*mass));
	}

	// comments taken from godot
	private void updateAngularForce(double dt) {
		if(torqueToApply.lengthSquared()<1e-6) return;
		if(shape==null) return;/*

		Matrix3d inertiaTensor = getInertiaTensorFromShape();
		//principal_inertia_axes_local = inertia_tensor.diagonalize().transposed();
		// note inertia_tensor.diagonalize() returns q and modifies inertia_tensor to be d.  or vice versa. 
		Matrix3d principalInertiaAxiesLocal = new Matrix3d();
		Matrix3d q = new Matrix3d();
		MatrixHelper.diagonalize(inertiaTensor, principalInertiaAxiesLocal, q);
		principalInertiaAxiesLocal.transpose();
		
		//principal_inertia_axes = get_transform().basis * principal_inertia_axes_local;
		Matrix3d principalInertiaAxies = new Matrix3d();
		Matrix3d pose3 = new Matrix3d();
		pose.get(pose3);
		principalInertiaAxies.mul(pose3,principalInertiaAxiesLocal);

		//Basis tb = principal_inertia_axes;
		//Basis tbt = tb.transposed();
		Matrix3d tb = new Matrix3d(principalInertiaAxies);
		Matrix3d tbt = new Matrix3d(tb);
		tbt.transpose();

		//Basis diag;
		//_inv_inertia = inertia_tensor.get_main_diagonal().inverse();
		//diag.scale(_inv_inertia);
		Matrix3d diag = new Matrix3d();
		diag.m00 = 1.0/q.m00;
		diag.m11 = 1.0/q.m11;
		diag.m22 = 1.0/q.m22;
		
		//_inv_inertia_tensor = tb * diag * tbt;
		Matrix3d inverseInertiaTensor = new Matrix3d(tb);
		inverseInertiaTensor.mul(diag);
		inverseInertiaTensor.mul(tbt);
		
		Vector3d torqueToApplyTransformed = new Vector3d();
		inverseInertiaTensor.transform(torqueToApply, torqueToApplyTransformed);
		
		torqueToApplyTransformed.scale(dt);
		angularVelocity.add(torqueToApplyTransformed);
		*/
		
		torqueToApply.scale(dt*0.995);
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
		Vector3d p = getPosition();
		
		// We can describe that spin as a vector w(t) The direction of w(t) gives the direction of 
		// the axis about which the body is spinning. The magnitude of w(t) tells how fast the body is spinning.
		Vector3d w = new Vector3d(angularVelocity);
		double len = w.length();
		if(len>0) w.normalize();
		
		Matrix3d m = new Matrix3d();
		Matrix3d rot = MatrixHelper.getMatrixFromAxisAndRotation(w,len);
		
		myPose.get(m);
		m.mul(rot);
		myPose.set(m);
		
		myPose.setTranslation(p);
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
			gl2.glPopMatrix();
			
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

	// @return the inertia tensor in local space.
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
			inertiaTensor.setScale(mass);
			return inertiaTensor;
		}
	}

	// @return the inertia tensor in local space, already diagonalized.
	private Matrix3d getInertiaTensorFromDimensions(double x, double y, double z) {
		Matrix3d it = new Matrix3d();
		it.m00=calculateMOI(y,z);
		it.m11=calculateMOI(x,z);
		it.m22=calculateMOI(x,y);

		it.m01 = it.m10 = 0;//-mass*x*y;
		it.m02 = it.m20 = 0;//-mass*x*z;
		it.m12 = it.m21 = 0;//-mass*y*z;

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
		
		forces.add(new Force(p,force,1,0,1));
		
		double damping = 0.85;
		// linear component
		Vector3d forceScaled = new Vector3d(force);
		forceScaled.scale(damping*1.0/mass);
		forceToApply.add(forceScaled);

		// angular component
		Vector3d forceNormal = new Vector3d(force);
		forceNormal.normalize();
		
		Vector3d newTorque = new Vector3d();
		newTorque.cross(getR(p),forceNormal);
		
		//torqueToApply.add(newTorque);
		Matrix3d inertiaTensor = getInertiaTensorTransformed();
		inertiaTensor.invert();
		inertiaTensor.transform(newTorque);
		//newTorque.scale(len);

		forces.add(new Force(p,newTorque,0,1,1));

		torqueToApply.add(newTorque);
	}
	
	private Matrix3d getInertiaTensorTransformed() {
		Matrix3d inertiaTensor = getInertiaTensorFromShape();
		Matrix3d pose3 = new Matrix3d();
		myPose.get(pose3);
		inertiaTensor.mul(pose3);
		return inertiaTensor;
	}
	
	private Vector3d getR(Point3d p) {
		Vector3d r = new Vector3d(p);
		r.sub(getPosition());
		return r;
	}
	
	// @return sum of angular and linear velocity at point p. 
	private Vector3d getCombinedVelocityAtPoint(Point3d p) {
		Vector3d sum = new Vector3d();
		sum.cross(angularVelocity,getR(p));

		//forces.add(new Force(p,sum,0,0,1));
		//forces.add(new Force(p,linearVelocity,0,1,0));

		sum.add(linearVelocity);
		
		forces.add(new Force(p,sum,0,0,1));
		
		return sum;
	}
	
	private void testFloorContact() {	
		ArrayList<Cuboid> list = shape.getCuboidList();
		if(!list.isEmpty()) {
			Cuboid cuboid = list.get(0);
			Point3d [] corners = PrimitiveSolids.get8PointsOfBox(cuboid.getBoundsBottom(),cuboid.getBoundsTop());
			double adjustZ =0;
			for( Point3d p : corners ) {
				myPose.transform(p);
				if(p.z<=0) {
					// hit floor
					Vector3d velocityAtPoint = getCombinedVelocityAtPoint(p);
					Vector3d n = new Vector3d(0,0,1);
					applyCollisionImpulse(p,n,velocityAtPoint);
					
					// don't be in the floor 
					if(adjustZ>p.z) adjustZ=p.z; 
					//isPaused=true;
				}
			}
			myPose.m23-=adjustZ;
		}
	}

	// https://en.wikipedia.org/wiki/Collision_response
	private void applyCollisionImpulse(Point3d p, Vector3d n, Vector3d velocityAtPoint) {
		double jr = getImpulseMagnitude(p,n);

		Vector3d n2 = new Vector3d(n);
		n2.scale(jr/mass);
		linearVelocity.sub(n2);
		
		Vector3d r = getR(p);
		Vector3d rxn = new Vector3d();
		rxn.cross(r,n);

		Matrix3d inertiaTensor = getInertiaTensorTransformed();
		inertiaTensor.invert();
		Vector3d rxn2 = new Vector3d();
		inertiaTensor.transform(rxn, rxn2);
		Vector3d rxn3 = new Vector3d();
		rxn3.scale(jr,rxn2);
		
		angularVelocity.sub(rxn3); 
	}
	
	private double getImpulseMagnitude(Point3d p, Vector3d n) {
		Vector3d velocityAtPoint = getCombinedVelocityAtPoint(p);
		
		Vector3d r = getR(p);
		Vector3d rxn = new Vector3d();
		rxn.cross(r,n);
		Vector3d rxn2 = new Vector3d();
		rxn2.cross(rxn, r);

		Matrix3d inertiaTensor = getInertiaTensorTransformed();
		inertiaTensor.invert();
		Vector3d rxn3 = new Vector3d();
		inertiaTensor.transform(rxn2, rxn3);
		
		rxn3.add(n);
		double v = rxn3.dot(n);
		
		double a = (-1.0 + Math.E) * velocityAtPoint.dot(n);
		double b = Math.pow(mass, -1) + v; 
		double jr = a/b;
		
		return jr;
	}
}
