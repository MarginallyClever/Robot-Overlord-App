package com.marginallyclever.robotOverlord.entity.physicalObject;

import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
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
	// position and orientation relative to my parent Entity.
	protected Matrix4d pose = new Matrix4d();
	// position and orientation relative to the world.
	protected Matrix4d poseWorld = new Matrix4d();
	// physical limits
	public Cuboid cuboid;
	
	private transient PhysicalObjectPanel physicalObjectControlPanel;
	
	protected boolean shouldDrawBoundingBox=false;
	protected boolean shouldDrawLocalOrigin=false;
	protected boolean shouldDrawConnectionToChildren=false;

	public PhysicalObject() {
		super();
		pose.setIdentity();
		poseWorld.setIdentity();
		cuboid=new Cuboid();
	}
	
	public void set(PhysicalObject b) {
		super.set(b);
		pose.set(b.pose);
		poseWorld.set(b.poseWorld);
		cuboid.set(b.cuboid);
	}
	
	/**
	 * Get the {@link EntityPanel} for this class' superclass, then the physicalObjectControlPanel for this class, and so on.
	 * 
	 * @param gui the main application instance.
	 * @return the list of physicalObjectControlPanels 
	 */
	@Override
	public ArrayList<JPanel> getContextPanels(RobotOverlord gui) {
		ArrayList<JPanel> list = super.getContextPanels(gui);
		if(list==null) list = new ArrayList<JPanel>();

		physicalObjectControlPanel = new PhysicalObjectPanel(gui,this);
		list.add(physicalObjectControlPanel);

		return list;
	}

	@Override
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);

			// helpful info
			drawBoundingBox(gl2);
			drawLocalOrigin(gl2);
			drawConnectionToChildren(gl2);
			
			// draw children relative to parent
			super.render(gl2);
			
		gl2.glPopMatrix();
	}
	
	protected void drawBoundingBox(GL2 gl2) {
		if(!shouldDrawBoundingBox) return;
		cuboid.render(gl2);
	}
	
	protected void drawLocalOrigin(GL2 gl2) {
		if(!shouldDrawLocalOrigin) return;
		PrimitiveSolids.drawStar(gl2,10);
	}
	
	protected void drawConnectionToChildren(GL2 gl2) {
		if(!shouldDrawConnectionToChildren) return;
		
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);

		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
		gl2.glDepthFunc(GL2.GL_ALWAYS);

		// connection to children
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

		gl2.glDepthFunc(depthFunc.get());
		
		if (isLit) gl2.glEnable(GL2.GL_LIGHTING);
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
	
	/**
	 * @return {@link Matrix4d} of the local pose
	 */
	public Matrix4d getPose() {
		return pose;
	}

	/**
	 * Set the local pose (relative to my parent)
	 * Automatically updates the cumulative pose.
	 * @param arg0 the local pose
	 */
	public void setPose(Matrix4d arg0) {
		// update 
		pose.set(arg0);
		updatePoseWorld();
		
		// make sure the panel is updated.  TODO use the observer?
		if(physicalObjectControlPanel!=null) {
			physicalObjectControlPanel.updateFields();	
		}
	}
	
	/**
	 * Recalculates poseWorld from pose and parent.poseWorld.  
	 * Does not crawl up the parent hierarchy.
	 */
	public void updatePoseWorld() {
		if(parent instanceof PhysicalObject) {
			poseWorld.mul(((PhysicalObject)parent).poseWorld,pose);
		} else {
			poseWorld.set(pose);
		}
	}

	
	/**
	 * @return {@link Matrix4d} of the world pose
	 */
	public Matrix4d getPoseWorld() {
		return poseWorld;
	}
	
	
	/**
	 * Set the pose and poseWorld of this item
	 * @param m
	 */
	public void setPoseWorld(Matrix4d m) {
		if(parent instanceof PhysicalObject) {
			Matrix4d iParent = new Matrix4d(((PhysicalObject)parent).poseWorld);
			iParent.invert();
			m.mul(iParent);
			setPose(m);
		} else {
			setPose(m);
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
		
		cuboid.setPoseWorld(this.getPose());
		cuboidList.add(cuboid);

		return cuboidList;
	}

	
	public boolean shouldDrawBoundingBox() {
		return shouldDrawBoundingBox;
	}

	public void setDrawBoundingBox(boolean shouldDrawBoundingBox) {
		this.shouldDrawBoundingBox = shouldDrawBoundingBox;
	}

	public boolean shouldDrawLocalOrigin() {
		return shouldDrawLocalOrigin;
	}

	public void setDrawLocalOrigin(boolean shouldDrawLocalOrigin) {
		this.shouldDrawLocalOrigin = shouldDrawLocalOrigin;
	}

	public boolean shouldDrawConnectionToChildren() {
		return shouldDrawConnectionToChildren;
	}

	public void setDrawConnectionToChildren(boolean shouldDrawConnectionToChildren) {
		this.shouldDrawConnectionToChildren = shouldDrawConnectionToChildren;
	}

	public PhysicalObjectPanel getPhysicalObjectControlPanel() {
		return physicalObjectControlPanel;
	}
}
