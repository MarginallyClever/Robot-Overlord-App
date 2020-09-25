package com.marginallyclever.robotOverlord.entity.scene.robotEntity.skycam;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.memento.Memento;
import com.marginallyclever.convenience.memento.MementoOriginator;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.entity.scene.PoseEntity;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.RobotEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class Skycam extends RobotEntity implements MementoOriginator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Vector3dEntity size = new Vector3dEntity();
	public PoseEntity ee = new PoseEntity("End Effector");
	
	public Skycam() {
		super();
		setName("Skycam");
		
		addChild(ee);
	}
	
	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2, pose);
		Vector3d s = size.get();
		Vector3d pos = getPosition();
		Point3d bottom = new Point3d(pos.x-s.x/2,pos.y-s.y/2,pos.z);
		Point3d top   = new Point3d(pos.x+s.x/2,pos.y+s.y/2,pos.z+s.z);
		PrimitiveSolids.drawBoxWireframe(gl2, bottom,top);
		
		Vector3d ep = ee.getPosition();
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
	public Memento createKeyframe() {
		return null;
	}

	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sk", "Skycam");
		view.add(size);
		view.popStack();
		
		super.getView(view);
	}

	@Override
	public Memento getState() {
		SkycamMemento m = new SkycamMemento();
		m.relative.set(ee.getPose());
		m.size.set(this.size.get());
		return m;
	}

	@Override
	public void setState(Memento arg0) {
		if(arg0 instanceof SkycamMemento) {
			SkycamMemento m = (SkycamMemento)arg0;
			ee.setPose(m.relative);
			this.size.set(m.size);
		}
	}
}
