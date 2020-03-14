package com.marginallyclever.robotOverlord.entity.physicalEntity;

import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.world.World;

public abstract class PhysicalEntity extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1959037711655040359L;
	
	// unique ids for all objects in the world.  
	// zero is reserved to indicate no object.
	static private int pickNameCounter=1;
	// my unique id
	private int pickName;	
	
	// pose relative to my parent Entity.
	protected Matrix4d pose = new Matrix4d();
	// pose relative to the world.
	protected Matrix4d poseWorld = new Matrix4d();
	
	// physical limits
	public transient Cuboid cuboid = new Cuboid();
	
	protected transient BooleanEntity showBoundingBox = new BooleanEntity("Bounding Box",false);
	protected transient BooleanEntity showLocalOrigin = new BooleanEntity("Local Origin",false);
	protected transient BooleanEntity showLineage = new BooleanEntity("Show Lineage",false);


	public PhysicalEntity() {
		super();
		setName("Physics");
		
		pickName = pickNameCounter++;
		
		addChild(showBoundingBox);
		addChild(showLocalOrigin);
		addChild(showLineage);
		
		pose.setIdentity();
		poseWorld.setIdentity();
	}
	
	public void set(PhysicalEntity b) {
		super.set(b);
		pose.set(b.pose);
		poseWorld.set(b.poseWorld);
		cuboid.set(b.cuboid);
	}

	public int getPickName() {
		return pickName;
	}
	
	@Deprecated
	public boolean hasPickName(int name) {
		return pickName==name;
	}

	/**
	 * Render this physicalEntity into the view
	 * @param gl2
	 */
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);

			// helpful info
			if(showBoundingBox.get()) {
				cuboid.render(gl2);
			}
			if(showLocalOrigin.get()) {
				PrimitiveSolids.drawStar(gl2,10);
			}
			renderConnectionToChildren(gl2);
			
			// draw children relative to parent
			for(Entity e : children ) {
				if(e instanceof PhysicalEntity) {
					((PhysicalEntity)e).render(gl2);
				}
			}
		gl2.glPopMatrix();
	}
	
	protected void renderConnectionToChildren(GL2 gl2) {
		if(!showLineage.get()) return;
		
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);

		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
		gl2.glDepthFunc(GL2.GL_ALWAYS);

		// connection to children
		for(Entity e : children ) {
			if(e instanceof PhysicalEntity) {					
				gl2.glColor3d(255, 255, 255);
				Vector3d p = ((PhysicalEntity)e).getPosition();
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
		return new Matrix4d(pose);
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
		cuboid.setPoseWorld(poseWorld);
	}
	
	/**
	 * Recalculates poseWorld from pose and parent.poseWorld.  
	 * Does not crawl up the parent hierarchy.
	 */
	public void updatePoseWorld() {
		if(parent instanceof PhysicalEntity) {
			Matrix4d m = new Matrix4d(pose);
			m.mul(((PhysicalEntity)parent).poseWorld);
			poseWorld.set(m);
		} else {
			poseWorld.set(pose);
		}
	}

	
	/**
	 * @return {@link Matrix4d} of the world pose
	 */
	public Matrix4d getPoseWorld() {
		return new Matrix4d(poseWorld);
	}
	
	
	/**
	 * Set the pose and poseWorld of this item
	 * @param m
	 */
	public void setPoseWorld(Matrix4d m) {
		if(parent instanceof PhysicalEntity) {
			Matrix4d iParent = new Matrix4d(((PhysicalEntity)parent).poseWorld);
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
		return showBoundingBox.get();
	}

	public void setDrawBoundingBox(boolean drawBoundingBox) {
		this.showBoundingBox.set(drawBoundingBox);
	}

	public boolean drawLocalOrigin() {
		return showLocalOrigin.get();
	}

	public void setDrawLocalOrigin(boolean drawLocalOrigin) {
		this.showLocalOrigin.set(drawLocalOrigin);
	}

	public boolean drawConnectionToChildren() {
		return showLineage.get();
	}

	public void setDrawConnectionToChildren(boolean drawConnectionToChildren) {
		this.showLineage.set(drawConnectionToChildren);
	}
}
