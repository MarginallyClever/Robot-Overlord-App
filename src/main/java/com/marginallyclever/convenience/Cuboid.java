package com.marginallyclever.convenience;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 * 
 * @author Dan Royer
 * @since 2.1.0
 *
 */
public class Cuboid extends BoundingVolume {
	protected Matrix4d pose;

	protected Point3d boundTop;  // max limits
	protected Point3d boundBottom;  // min limits
	
	public Point3d [] p;  // all 8 corners
	protected boolean isDirty;
	
	
	public Cuboid() {
		pose = new Matrix4d();
		pose.setIdentity();
		boundTop = new Point3d();
		boundBottom = new Point3d();

		p = new Point3d[8];
		for(int i=0;i<8;++i) p[i] = new Point3d();
		isDirty=false;
	}

	public void set(Cuboid b) {
		pose.set(b.pose);
		boundTop.set(b.boundTop);
		boundBottom.set(b.boundBottom);

		for(int i=0;i<8;++i) p[i].set(b.p[i]);
		
		isDirty=b.isDirty;
	}
	
	public void updatePoints() {
		//if(!isDirty) return;
		//isDirty=false;
		
		p[0].set(boundBottom.x, boundBottom.y, boundBottom.z);
		p[1].set(boundBottom.x, boundBottom.y, boundTop.z);
		p[2].set(boundBottom.x, boundTop.y, boundBottom.z);
		p[3].set(boundBottom.x, boundTop.y, boundTop.z);
		p[4].set(boundTop.x, boundBottom.y, boundBottom.z);
		p[5].set(boundTop.x, boundBottom.y, boundTop.z);
		p[6].set(boundTop.x, boundTop.y, boundBottom.z);
		p[7].set(boundTop.x, boundTop.y, boundTop.z);

		for (int i = 0; i < p.length; ++i) {
			// System.out.print("\t"+p[i]);
			pose.transform(p[i]);
			// System.out.println(" >> "+p[i]);
		}
	}

	public void setBounds(Point3d boundTop, Point3d boundBottom) {
		//if(!this.boundTop.epsilonEquals(boundTop, 1e-4)) 
		{
			this.boundTop.set(boundTop);
			isDirty=true;
		}
		//if(!this.boundBottom.epsilonEquals(boundBottom, 1e-4))
		{
			this.boundBottom.set(boundBottom);
			isDirty=true;
		}
	}
	
	public Point3d getBoundsTop() {
		return this.boundTop;
	}
	
	public Point3d getBoundsBottom() {
		return boundBottom;
	}
	
	public void setPose(Matrix4d m) {
		if(!pose.epsilonEquals(m, 1e-4)) {
			pose.set(m);
			isDirty=true;
		}
	}
}
