package com.marginallyclever.convenience;

import com.jogamp.opengl.GL2;

import javax.vecmath.Vector3d;

/**
 * A cylinder with a radius and two end points.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class Cylinder implements BoundingVolume {
	private Vector3d p1 = new Vector3d(0,0,0);
	private Vector3d p2 = new Vector3d(0,0,0);
	private Vector3d n = new Vector3d(0,0,0);
	private Vector3d f = new Vector3d(0,0,0);
	private Vector3d r = new Vector3d(0,0,0);
	private float radius=1.0f;
	
	public void SetP1(Vector3d src) {
		p1.set(src);
		UpdateVectors();
	}
	public void SetP2(Vector3d src) {
		p2.set(src);
		UpdateVectors();
	}
	
	public void setRadius(float r) {
		radius = r;
	}
	public float getRadius() {
		return radius;
	}
	
	public Vector3d GetP1() {
		return p1;
	}
	
	public Vector3d GetP2() {
		return p2;
	}
	public Vector3d GetN() {
		return n;
	}
	public Vector3d GetF() {
		return f;
	}
	public Vector3d GetR() {
		return r;
	}
	
	public void UpdateVectors() {
		n.set(p2);
		n.sub(p1);
		n.normalize();
		
		if(n.x > n.y) {
			if(n.x > n.z) {
				// x major
				f.z=n.z;
				f.y=n.x;
				f.x=n.y;
			} else {
				// z major
				f.z=n.y;
				f.y=n.z;
				f.x=n.x;
			}
		} else {
			if(n.y > n.z) {
				// y major
				f.z=n.z;
				f.y=n.x;
				f.x=n.y;
			} else {
				// z major
				f.z=n.y;
				f.y=n.z;
				f.x=n.x;
			}
		}
		r.cross(f, n);
		r.normalize();
		f.cross(n, r);
		f.normalize();
	}
	
	public void render(GL2 gl2) {
		/*
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(this.GetP1().x, this.GetP1().y, this.GetP1().z);
		gl2.glVertex3d(this.GetP2().x, this.GetP2().y, this.GetP2().z);
		gl2.glEnd();
		*/

		Vector3d tx = new Vector3d();
		Vector3d ty = new Vector3d();
		Vector3d t1 = new Vector3d();
		Vector3d t2 = new Vector3d();
		Vector3d n = new Vector3d();
		
		int i;
		int c=10;
		
		// left
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3d(-this.GetN().x,-this.GetN().y,-this.GetN().z);
		for(i=0;i<=c;++i) {
			tx.set(this.GetR());
			ty.set(this.GetF());

			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			tx.scale((float)Math.sin(ratio)*this.getRadius());
			ty.scale((float)Math.cos(ratio)*this.getRadius());
			t1.set(this.GetP1());
			t1.add(tx);
			t1.add(ty);
			gl2.glVertex3d(t1.x,t1.y,t1.z);
		}
		gl2.glEnd();
		// right
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glNormal3d(this.GetN().x,this.GetN().y,this.GetN().z);
		for(i=0;i<=c;++i) {
			tx.set(this.GetR());
			ty.set(this.GetF());

			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			tx.scale((float)Math.sin(ratio)*this.getRadius());
			ty.scale((float)Math.cos(ratio)*this.getRadius());
			t1.set(this.GetP2());
			t1.add(tx);
			t1.add(ty);
			gl2.glVertex3d(t1.x,t1.y,t1.z);
		}
		gl2.glEnd();

		// edge
		gl2.glBegin(GL2.GL_TRIANGLE_STRIP);
		for(i=0;i<=c;++i) {
			tx.set(this.GetR());
			ty.set(this.GetF());

			float ratio= (float)Math.PI * 2.0f * (float)i/(float)c;
			tx.scale((float)Math.sin(ratio)*this.getRadius());
			ty.scale((float)Math.cos(ratio)*this.getRadius());
			t1.set(this.GetP1());
			t1.add(tx);
			t1.add(ty);
			
			t2.set(tx);
			t2.add(ty);
			n.set(t2);
			n.normalize();
			gl2.glNormal3d(n.x,n.y,n.z);
			t2.add(this.GetP2());
			gl2.glVertex3d(t1.x,t1.y,t1.z);
			gl2.glVertex3d(t2.x,t2.y,t2.z);
			
		}
		gl2.glEnd();
	}
}
