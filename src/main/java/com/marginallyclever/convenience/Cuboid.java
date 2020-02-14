package com.marginallyclever.convenience;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * 
 * @author Dan Royer
 * @since 2.1.0
 *
 */
public class Cuboid {
	protected Matrix4d poseWorld;

	protected Vector3d boundTop;  // max limits
	protected Vector3d boundBottom;  // min limits
	
	public Point3d [] p;  // all 8 corners of the cuboid 
	protected boolean isDirty;
	
	
	public Cuboid() {
		poseWorld = new Matrix4d();
		boundTop = new Vector3d();
		boundBottom = new Vector3d();

		p = new Point3d[8];
		for(int i=0;i<8;++i) p[i] = new Point3d();
		isDirty=false;
	}

	public void set(Cuboid b) {
		poseWorld.set(b.poseWorld);
		boundTop.set(boundTop);
		boundBottom.set(boundBottom);

		for(int i=0;i<8;++i) p[i].set(b.p[i]);
		
		isDirty=b.isDirty;
	}
	
	public void updatePoints() {
		if(!isDirty) return;

		isDirty=false;
		
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
			poseWorld.transform(p[i]);
			// System.out.println(" >> "+p[i]);
		}
	}

	public void setBounds(Point3d _boundTop, Point3d _boundBottom) {
		if(!boundTop.epsilonEquals(_boundTop, 1e-4)) {
			boundTop.set(_boundTop);
			isDirty=true;
		}
		if(!boundBottom.epsilonEquals(_boundBottom, 1e-4)) {
			boundBottom.set(_boundBottom);
			isDirty=true;
		}
	}
	
	public void setMatrix(Matrix4d m) {
		if(!poseWorld.epsilonEquals(m, 1e-4)) {
			poseWorld.set(m);
			isDirty=true;
		}
	}
	
}
