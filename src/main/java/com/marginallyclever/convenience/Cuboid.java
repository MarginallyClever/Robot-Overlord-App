package com.marginallyclever.convenience;

import java.nio.IntBuffer;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import com.jogamp.opengl.GL2;

/**
 * 
 * @author Dan Royer
 * @since 2.1.0
 *
 */
public class Cuboid extends BoundingVolume {
	protected Matrix4d pose;  // relative to parent
	protected Matrix4d poseWorld;  // relative to universe

	protected Point3d boundTop;  // max limits
	protected Point3d boundBottom;  // min limits
	
	public Point3d [] p;  // all 8 corners
	protected boolean isDirty;
	
	
	public Cuboid() {
		pose = new Matrix4d();
		pose.setIdentity();
		poseWorld = new Matrix4d();
		poseWorld.setIdentity();
		boundTop = new Point3d();
		boundBottom = new Point3d();

		p = new Point3d[8];
		for(int i=0;i<8;++i) p[i] = new Point3d();
		isDirty=false;
	}

	public void set(Cuboid b) {
		poseWorld.set(b.poseWorld);
		boundTop.set(b.boundTop);
		boundBottom.set(b.boundBottom);

		for(int i=0;i<8;++i) p[i].set(b.p[i]);
		
		isDirty=b.isDirty;
	}
	
	public void updatePoints() {
		if(!isDirty) return;
		isDirty=false;
		
		p[0].set(boundBottom.x, boundBottom.y, boundBottom.z);
		p[1].set(boundBottom.x, boundBottom.y, boundTop   .z);
		p[2].set(boundBottom.x, boundTop   .y, boundBottom.z);
		p[3].set(boundBottom.x, boundTop   .y, boundTop   .z);
		p[4].set(boundTop   .x, boundBottom.y, boundBottom.z);
		p[5].set(boundTop   .x, boundBottom.y, boundTop   .z);
		p[6].set(boundTop   .x, boundTop   .y, boundBottom.z);
		p[7].set(boundTop   .x, boundTop   .y, boundTop   .z);

		for (int i = 0; i < p.length; ++i) {
			// System.out.print("\t"+p[i]);
			poseWorld.transform(p[i]);
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
	
	public void setPoseWorld(Matrix4d m) {
		if(!poseWorld.epsilonEquals(m, 1e-4)) {
			poseWorld.set(m);
			isDirty=true;
		}
	}
	
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			//MatrixHelper.applyMatrix(gl2, poseWorld);

			IntBuffer depthFunc = IntBuffer.allocate(1);
			gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
			gl2.glDepthFunc(GL2.GL_ALWAYS);
			
			boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
			gl2.glDisable(GL2.GL_LIGHTING);
			
			PrimitiveSolids.drawBoxWireframe(gl2, getBoundsBottom(),getBoundsTop());
	
			if (isLit) gl2.glEnable(GL2.GL_LIGHTING);
			
			gl2.glDepthFunc(depthFunc.get());
		gl2.glPopMatrix();
	}
}
