package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.Vector3DParameter;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentPanelFactory;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

@Deprecated
public class SkycamModel {
	protected transient Vector3DParameter size = new Vector3DParameter("size",100,100,100);
	protected transient Vector3DParameter ee = new Vector3DParameter("ee",0,0,0);
	protected Color4f color = new Color4f();
	
	public SkycamModel() {
		super();
	}

	public void render(GL2 gl2) {
		gl2.glColor4d(color.x,color.y,color.z,color.w);
		
		Vector3d s = size.get();
		Point3d bottom = new Point3d(-s.x/2,-s.y/2,0);
		Point3d top    = new Point3d(+s.x/2,+s.y/2,+s.z);
		PrimitiveSolids.drawBoxWireframe(gl2, bottom,top);
		
		Vector3d ep = ee.get();
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(ep.x,ep.y,ep.z);  gl2.glVertex3d(bottom.x,bottom.y,top.z);
		gl2.glVertex3d(ep.x,ep.y,ep.z);  gl2.glVertex3d(bottom.x,top   .y,top.z);
		gl2.glVertex3d(ep.x,ep.y,ep.z);  gl2.glVertex3d(top   .x,top   .y,top.z);
		gl2.glVertex3d(ep.x,ep.y,ep.z);  gl2.glVertex3d(top   .x,bottom.y,top.z);
		gl2.glEnd();
		
		gl2.glPopMatrix();
	}

	public Vector3d getPosition() {
		return new Vector3d(ee.get());
	}
	
	public boolean setPosition(Matrix4d pose) {
		Vector3d p = MatrixHelper.getPosition(pose);
		return setPosition(p);
	}
	
	public boolean setPosition(Vector3d p) {
		Vector3d s = size.get();
		
		boolean ok=true;
		if(p.x> s.x) { p.x =  s.x; ok=false; }
		if(p.x<-s.x) { p.x = -s.x; ok=false; }
		if(p.y> s.y) { p.y =  s.y; ok=false; }
		if(p.y<-s.y) { p.y = -s.y; ok=false; }
		if(p.z> s.z) { p.z =  s.z; ok=false; }
		if(p.z<-s.z) { p.z = -s.z; ok=false; }
		ee.set(p);
		
		return ok;
	}

	public void goHome() {
		ee.set(0, 0, 0);		
	}

	public void setDiffuseColor(float r,float g,float b,float a) {
		color.x=r;
		color.y=g;
		color.z=b;
		color.w=a;
	}
}
