package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.parameters.Vector3dEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

@Deprecated
public class SkycamModel extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2970092713744170430L;

	public static final int DEFAULT_FEEDRATE = 1;
	public static final int DEFAULT_ACCELERATION = 1;
	public static final int MAX_SEGMENTS = 16;

	public static final double MIN_SEGMENT_TIME = 0.025;  // seconds
	public static final double MAX_JOINT_FEEDRATE = 20;

	public static final double[] MAX_JERK = {1,1,1,1};

	public static final double MAX_FEEDRATE = 10;
	public static final double MAX_ACCELERATION = 10;
	
	protected transient Vector3dEntity size = new Vector3dEntity(100,100,100);
	protected transient Vector3dEntity ee = new Vector3dEntity();
	protected Color4f color = new Color4f();
	
	public SkycamModel() {
		super();
	}

	@Override
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
		
		super.render(gl2);
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Model",true);
		view.add(size);
		view.popStack();
		super.getView(view);
	}
	
	public SkycamCursor createPose() {
		return new SkycamCursor();
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
