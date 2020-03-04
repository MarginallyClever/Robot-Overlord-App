package com.marginallyclever.robotOverlord.entity.physicalObject;

import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.EntityPanel;
import com.marginallyclever.robotOverlord.entity.world.World;

public abstract class PhysicalObject extends Entity {
	protected Matrix4d pose;	// position and orientation relative to it's parent entity.
	public Cuboid cuboid;	// physical limits
	
	private transient PhysicalObjectPanel physicalObjectControlPanel;
	
	public PhysicalObject() {
		super();
		pose = new Matrix4d();
		pose.setIdentity();
		cuboid=new Cuboid();
	}
	
	public void set(PhysicalObject b) {
		super.set(b);
		pose.set(b.pose);
		cuboid.set(b.cuboid);
	}
	
	/**
	 * Get the {@link EntityPanel} for this class' superclass, then the physicalObjectControlPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of physicalObjectControlPanels 
	 */
	@Override
	public ArrayList<JPanel> getContextPanel(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanel(gui);
		if(list==null) list = new ArrayList<JPanel>();

		physicalObjectControlPanel = new PhysicalObjectPanel(gui,this);
		list.add(physicalObjectControlPanel);

		return list;
	}

	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
		
			MatrixHelper.applyMatrix(gl2, pose);
			// draw the children, if any
			for(Entity e : children ) {
				e.render(gl2);
			}
			
			boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
			gl2.glDisable(GL2.GL_LIGHTING);
			
			// physical bounds
			boolean drawBoundingBox=true;
			if(drawBoundingBox) 
			{
				Point3d a = new Point3d(cuboid.getBoundsBottom());
				Point3d b = new Point3d(cuboid.getBoundsTop());
				PrimitiveSolids.drawBoxWireframe(gl2, a,b);
			}

			// now draw some useful info without lighting or depth testing
			IntBuffer depthFunc = IntBuffer.allocate(1);
			gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
			gl2.glDepthFunc(GL2.GL_ALWAYS);

			
			// connection to children
			boolean drawConnectionToChildren=false;
			if(drawConnectionToChildren) {
				for(Entity e : children ) {
					if(e instanceof PhysicalObject) {					
						gl2.glColor3d(255, 255, 255);
						Vector3d p = ((PhysicalObject)e).getPosition();
						gl2.glBegin(GL2.GL_LINES);
						gl2.glVertex3d(0, 0, 0);
						gl2.glVertex3d(p.x,p.y,p.z);
						gl2.glEnd();
					}
				}
			}
			gl2.glDepthFunc(GL2.GL_LESS);
			
			if (isLit) gl2.glEnable(GL2.GL_LIGHTING);
			
		gl2.glPopMatrix();
	}
	
	public Vector3d getPosition() {
		return new Vector3d(pose.m03,pose.m13,pose.m23);
	}
	
	public void setPosition(Vector3d pos) {
		Matrix4d m = new Matrix4d(pose);
		m.setTranslation(pos);
		setPose(m);
	}

	/**
	 * 
	 * @param arg0 fills the vector3 with one possible combination of radian rotations.
	 */
	public void getRotation(Vector3d arg0) {
		Matrix3d temp = new Matrix3d();
		pose.get(temp);
		arg0.set(MatrixHelper.matrixToEuler(temp));
	}

	/**
	 * 
	 * @param arg0 Vector3d of radian rotation values
	 */
	public void setRotation(Vector3d arg0) {
		Matrix4d m4 = new Matrix4d();
		Matrix3d m3 = MatrixHelper.eulerToMatrix(arg0);
		m4.set(m3);
		m4.setTranslation(getPosition());
		setPose(m4);
	}
	
	public void setRotation(Matrix3d arg0) {
		Matrix4d m = new Matrix4d();
		m.set(arg0);
		m.setTranslation(getPosition());
		setPose(m);
	}
	
	public void getRotation(Matrix4d arg0) {
		arg0.set(pose);
		arg0.setTranslation(new Vector3d(0,0,0));
	}
	
	public Matrix4d getPose() {
		return pose;
	}
	
	public void setPose(Matrix4d arg0) {
		pose.set(arg0);
		if(physicalObjectControlPanel!=null) {
			physicalObjectControlPanel.updateFields();	
		}
	}

	public World getWorld() {
		Entity p = parent;
		while (p != null) {
			if (p instanceof World) {
				return (World) p;
			}
			p=p.getParent();
		}
		return null;
	}

	public Cuboid getCuboid() {
		return cuboid;
	}
	
	/**
	 * 
	 * @return a list of cuboids, or null.
	 */
	public ArrayList<Cuboid> getCuboidList() {		
		ArrayList<Cuboid> cuboidList = new ArrayList<Cuboid>();
		
		cuboid.setPose(this.getPose());
		cuboidList.add(cuboid);

		return cuboidList;
	}
}
