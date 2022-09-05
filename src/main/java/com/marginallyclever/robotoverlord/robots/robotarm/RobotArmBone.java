package com.marginallyclever.robotoverlord.robots.robotarm;

import java.io.IOException;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.robotoverlord.mesh.ShapeEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.DoubleEntity;

/**
 * Math representation of one link in a robot arm described with DH parameters.
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class RobotArmBone implements Cloneable {
	private String name = "";
	// D-H parameters combine to make this matrix which is relative to the parent.
	private final Matrix4d pose = new Matrix4d();
	// length (mm) along previous Z to the common normal
	private double d;
	// length (mm) of the common normal. movement on X.  Assuming a revolute joint, this is the radius about previous Z
	private double r;
	// angle (degrees) about common normal, from old Z axis to new Z axis
	private double alpha;
	// angle (degrees) about previous Z, from old X to new X
	public double theta;
	// angle (degrees) at home position.
	public double thetaHome;

	private double thetaMax, thetaMin;
	
	// model and relative offset from DH origin
	private ShapeEntity shape;
	
	// TODO this doesn't belong here
	public final DoubleEntity slider = new DoubleEntity("J",0);

	private double mass;
	@SuppressWarnings("unused")
	private double iMass;
	private Matrix3d inertiaTensor = new Matrix3d();
	private Point3d centerOfMass = new Point3d();
	private Vector3d linearVelocity = new Vector3d();
	private Vector3d force = new Vector3d();
	private Vector3d angularVelocity = new Vector3d();
	private Vector3d torque = new Vector3d();
		
	public RobotArmBone() {
		inertiaTensor.setIdentity();
		setMass(1);
	}
	
	public RobotArmBone(String name,double d,double r,double alpha,double theta,double thetaMax,double thetaMin,String shapeFilename) {
		this();
		this.set(name,d,r,alpha,theta,thetaMax,thetaMin,shapeFilename);
		this.thetaHome=theta;
	}
	
	public void set(String name,double d,double r,double alpha,double theta,double thetaMax,double thetaMin,String shapeFilename) {
		this.setName(name);
		this.d=d;
		this.r=r;
		this.alpha=alpha;
		this.theta=theta;
		this.thetaMax=thetaMax;
		this.thetaMin=thetaMin;
		this.shape = new ShapeEntity(name,shapeFilename);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		RobotArmBone b = (RobotArmBone)super.clone();
		b.angularVelocity = new Vector3d(this.angularVelocity);
		b.centerOfMass = new Point3d(this.centerOfMass);
		b.force = new Vector3d(this.force);
		b.inertiaTensor = new Matrix3d(this.inertiaTensor);
		b.linearVelocity = new Vector3d(this.linearVelocity);
		b.shape = new ShapeEntity(name,this.shape.getModelFilename());
		b.torque = new Vector3d(this.torque);
		return b;
	}
	
	@Override
	public String toString() {
		String s = name
				+","+d
				+","+r
				+","+alpha
				+","+theta
				+","+thetaMax
				+","+thetaMin
				+","+shape.getModelFilename()
				+","+mass
				+","+inertiaTensor
				+","+centerOfMass
				+","+linearVelocity
				+","+force
				+","+angularVelocity
				+","+torque
				+","+thetaHome;
		
		return this.getClass().getSimpleName()+" ["+s+"]";
	}
	
	public void fromString(String s) throws Exception {
		final String header = RobotArmBone.class.getSimpleName()+" [";
		if(!s.startsWith(header)) throw new IOException("missing header.");
		
		// strip header and "]" from either end.
		s=s.substring(header.length(),s.length()-1);
		
		// split by comma
		String [] pieces = s.split(",");
		set(pieces[0], 
			Double.parseDouble(pieces[1]),
			Double.parseDouble(pieces[2]),
			Double.parseDouble(pieces[3]),
			Double.parseDouble(pieces[4]),
			Double.parseDouble(pieces[5]),
			Double.parseDouble(pieces[6]),
			pieces[7]);
		setMass(Double.parseDouble(pieces[8]));
		setInertiaTensor(StringHelper.parseMatrix3d(pieces[9]));
		setCenterOfMass((Point3d)StringHelper.parseTuple3d(pieces[10]));
		setLinearVelocity((Vector3d)StringHelper.parseTuple3d(pieces[11]));
		setForce((Vector3d)StringHelper.parseTuple3d(pieces[12]));
		setLinearVelocity((Vector3d)StringHelper.parseTuple3d(pieces[13]));
		setForce((Vector3d)StringHelper.parseTuple3d(pieces[14]));
		setHome(Double.parseDouble(pieces[15]));
	}
	
	public void setAngleWRTLimits(double newAngle) {
		// if max angle and min angle overlap then there is no limit on this joint.
		double bMiddle = getAngleMiddle();
		double bMax = Math.abs(thetaMax-bMiddle);
		double bMin = Math.abs(thetaMin-bMiddle);
		if(bMin+bMax<360) {
			// prevent pushing the arm to an illegal angle
			newAngle = Math.max(Math.min(newAngle, thetaMax), thetaMin);
		}
		
		theta = newAngle % 360;
	}
	
	public void updateMatrix() {
		assert(!Double.isNaN(theta));
		assert(!Double.isNaN(alpha));
		assert(!Double.isNaN(r));
		assert(!Double.isNaN(d));
		double rt = Math.toRadians(theta);
		double ra = Math.toRadians(alpha);
		double ct = Math.cos(rt);
		double ca = Math.cos(ra);
		double st = Math.sin(rt);
		double sa = Math.sin(ra);
		
		pose.m00 = ct;		pose.m01 = -st*ca;		pose.m02 = st*sa;		pose.m03 = r*ct;
		pose.m10 = st;		pose.m11 = ct*ca;		pose.m12 = -ct*sa;		pose.m13 = r*st;
		pose.m20 = 0;		pose.m21 = sa;			pose.m22 = ca;			pose.m23 = d;
		pose.m30 = 0;		pose.m31 = 0;			pose.m32 = 0;			pose.m33 = 1;
	}

	public void getView(ViewPanel view) {
		view.addRange(slider,(int)thetaMax,(int)thetaMin);
	}
	
	public double getAngleMiddle() {
		return (thetaMax+thetaMin)/2;
	}

	public double getAngleMax() {
		return thetaMax;
	}

	public double getAngleMin() {
		return thetaMin;
	}

	public double getD() {
		return d;
	}

	public double getR() {
		return r;
	}

	public double getAlpha() {
		return alpha;
	}

	public double getTheta() {
		return theta;
	}

	/**
	 * @retyrn the home angle (degrees)
	 */
	public double getHome() {
		return thetaHome;
	}

	/**
	 * Set the home angle (degrees)
	 * @param theta the new angle
	 */
	public void setHome(double theta) {
		thetaHome = theta;
	}
	public Matrix4d getPose() {
		return pose;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		slider.setName(name);
	}

	public ShapeEntity getShape() {
		return shape;
	}

	public void setTextureFilename(String filename) {
		shape.getMaterial().setTextureFilename(filename);
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
		if(mass==0) this.iMass = 1.0;
		else this.iMass = 1.0 / mass;
	}

	public Matrix3d getInertiaTensor() {
		return new Matrix3d(inertiaTensor);
	}

	public void setInertiaTensor(Matrix3d inertiaTensor) {
		this.inertiaTensor.set(inertiaTensor);
	}

	public Point3d getCenterOfMass() {
		return new Point3d(centerOfMass);
	}

	public void setCenterOfMass(Point3d centerOfMass) {
		this.centerOfMass.set(centerOfMass);
	}

	public Vector3d getLinearVelocity() {
		return new Vector3d(linearVelocity);
	}

	public void setLinearVelocity(Vector3d linearVelocity) {
		this.linearVelocity.set(linearVelocity);
	}

	public Vector3d getForce() {
		return new Vector3d(force);
	}

	public void setForce(Vector3d force) {
		this.force.set(force);
	}

	public Vector3d getAngularVelocity() {
		return new Vector3d(angularVelocity);
	}

	public void setAngularVelocity(Vector3d angularVelocity) {
		this.angularVelocity.set(angularVelocity);
	}

	public Vector3d getTorque() {
		return new Vector3d(torque);
	}

	public void setTorque(Vector3d torque) {
		this.torque.set(torque);
	}
/*
	// a.angularVelocity
	public void computeAngularVelocity(RobotArmBone a) {
		if(a!=null) {
			// angular velocity of ith limb in the i+1th reference frame
			
			Vector3d av = a.getAngularVelocity();
			a.getPose().transform(av);
			
			Vector3d z = MatrixHelper.getZAxis(getPose());
			z.scale(this.theta);
		}
	}*/
}