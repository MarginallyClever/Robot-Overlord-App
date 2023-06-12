package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RenderComponent;
import com.marginallyclever.robotoverlord.parameters.Vector3DParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A skycam robot suspends a gondola inside a box from four cables attached to motors at the top corners of the box.
 *
 * @author Dan Royer
 * @since 1.7.1
 */
@Deprecated
public class Skycam extends RenderComponent {
	private static final Logger logger = LoggerFactory.getLogger(Skycam.class);
	protected transient Vector3DParameter size = new Vector3DParameter("size",100,100,100);
	private PoseComponent eePose;
	
	public Skycam() {
		super();
	}

	@Override
	public void onAttach() {
		Entity maybe = getEntity().findByPath("./ee");
		Entity ee = (maybe!=null) ? maybe : new Entity("ee");
		eePose = ee.getComponent(PoseComponent.class);
		eePose.setPosition(new Vector3d(0,0,0));
	}

	@Override
	public void render(GL3 gl) {
		PoseComponent myPose = getEntity().getComponent(PoseComponent.class);

		gl.glPushMatrix();
		MatrixHelper.applyMatrix(gl, myPose.getLocal());

		// user controlled version
		setPosition(MatrixHelper.getPosition(eePose.getLocal()));
		renderModel(gl);

		gl.glPopMatrix();
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
		eePose.setPosition(p);

		return ok;
	}

	public void renderModel(GL3 gl) {
		gl.glColor4d(1,1,1,1);

		Vector3d s = size.get();
		Point3d bottom = new Point3d(-s.x/2,-s.y/2,0);
		Point3d top    = new Point3d(+s.x/2,+s.y/2,+s.z);
		PrimitiveSolids.drawBoxWireframe(gl, bottom,top);

		Vector3d ep = eePose.getPosition();
		gl.glBegin(GL3.GL_LINES);
		gl.glVertex3d(ep.x,ep.y,ep.z);  gl.glVertex3d(bottom.x,bottom.y,top.z);
		gl.glVertex3d(ep.x,ep.y,ep.z);  gl.glVertex3d(bottom.x,top   .y,top.z);
		gl.glVertex3d(ep.x,ep.y,ep.z);  gl.glVertex3d(top   .x,top   .y,top.z);
		gl.glVertex3d(ep.x,ep.y,ep.z);  gl.glVertex3d(top   .x,bottom.y,top.z);
		gl.glEnd();

		gl.glPopMatrix();
	}
}
