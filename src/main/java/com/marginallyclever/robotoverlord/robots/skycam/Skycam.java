package com.marginallyclever.robotoverlord.robots.skycam;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RenderComponent;
import com.marginallyclever.robotoverlord.parameters.Vector3DParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Skycam extends RenderComponent {
	private static final Logger logger = LoggerFactory.getLogger(Skycam.class);
	protected transient Vector3DParameter size = new Vector3DParameter("size",100,100,100);
	private PoseComponent eePose;
	
	public Skycam() {
		super();
	}

	@Override
	public void setEntity(Entity entity) {
		super.setEntity(entity);
		if(entity == null) return;

		Entity maybe = entity.findByPath("./ee");
		Entity ee;
		if(maybe!=null) ee = maybe;
		else {
			ee = new Entity("ee");
			// EntityManager.addEntityToParent(ee,entity);
		}
		ee.addComponent(new PoseComponent());
		eePose = ee.getComponent(PoseComponent.class);
		eePose.setPosition(new Vector3d(0,0,0));
	}

	@Override
	public void render(GL2 gl2) {
		PoseComponent myPose = getEntity().getComponent(PoseComponent.class);

		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, myPose.getLocal());

		// user controlled version
		setPosition(MatrixHelper.getPosition(eePose.getLocal()));
		renderModel(gl2);

		gl2.glPopMatrix();
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

	public void renderModel(GL2 gl2) {
		gl2.glColor4d(1,1,1,1);

		Vector3d s = size.get();
		Point3d bottom = new Point3d(-s.x/2,-s.y/2,0);
		Point3d top    = new Point3d(+s.x/2,+s.y/2,+s.z);
		PrimitiveSolids.drawBoxWireframe(gl2, bottom,top);

		Vector3d ep = eePose.getPosition();
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex3d(ep.x,ep.y,ep.z);  gl2.glVertex3d(bottom.x,bottom.y,top.z);
		gl2.glVertex3d(ep.x,ep.y,ep.z);  gl2.glVertex3d(bottom.x,top   .y,top.z);
		gl2.glVertex3d(ep.x,ep.y,ep.z);  gl2.glVertex3d(top   .x,top   .y,top.z);
		gl2.glVertex3d(ep.x,ep.y,ep.z);  gl2.glVertex3d(top   .x,bottom.y,top.z);
		gl2.glEnd();

		gl2.glPopMatrix();
	}
}
