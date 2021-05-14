package com.marginallyclever.robotOverlord.entity.scene.robots.sixi3;

import javax.vecmath.Matrix4d;

import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.scene.shape.Shape;

/**
 * Math representation of one link in a robot arm described with DH parameters.
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public class Sixi3Bone {
	// D-H parameters combine to make this matrix which is relative to the parent.
	public Matrix4d pose = new Matrix4d();
	// length (mm) along previous Z to the common normal
	public double d;
	// angle (degrees) about previous Z, from old X to new X
	public double theta;
	// length (mm) of the common normal. Assuming a revolute joint, this is the radius about previous Z
	public double r;
	// angle (degrees) about common normal, from old Z axis to new Z axis
	public double alpha;
	
	public double angleMax, angleMin;
	
	public double mass;
	public double maxTorque;
	public double maxVelocity;
	
	// model and relative offset from DH origin
	public Shape shape;
	
	public DoubleEntity slider = new DoubleEntity("J",0);
	
	public Sixi3Bone() {}
	
	public void set(double rr,double dd,double aa,double tt,double aMax,double aMin,String shapeFilename) {
		d=dd;
		r=rr;
		alpha=aa;
		theta=tt;
		angleMax=aMax;
		angleMin=aMin;
		shape = new Shape(shapeFilename);
	}
	
	public void updateMatrix() {
		assert(!Double.isNaN(theta));
		assert(!Double.isNaN(alpha));
		assert(!Double.isNaN(r));
		assert(!Double.isNaN(d));
		double ct = Math.cos(Math.toRadians(theta));
		double ca = Math.cos(Math.toRadians(alpha));
		double st = Math.sin(Math.toRadians(theta));
		double sa = Math.sin(Math.toRadians(alpha));
		
		pose.m00 = ct;		pose.m01 = -st*ca;		pose.m02 = st*sa;		pose.m03 = r*ct;
		pose.m10 = st;		pose.m11 = ct*ca;		pose.m12 = -ct*sa;		pose.m13 = r*st;
		pose.m20 = 0;		pose.m21 = sa;			pose.m22 = ca;			pose.m23 = d;
		pose.m30 = 0;		pose.m31 = 0;			pose.m32 = 0;			pose.m33 = 1;
	}
}