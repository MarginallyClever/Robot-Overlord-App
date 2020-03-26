package com.marginallyclever.robotOverlord.entity.scene;

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
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Matrix4dEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

public class PoseEntity extends Entity {
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
	public Matrix4dEntity pose = new Matrix4dEntity();
	// pose relative to the world.
	public Matrix4dEntity poseWorld = new Matrix4dEntity();
	
	// physical limits
	public transient Cuboid cuboid = new Cuboid();
	
	public transient BooleanEntity showBoundingBox = new BooleanEntity("Show Bounding Box",false);
	public transient BooleanEntity showLocalOrigin = new BooleanEntity("Show Local Origin",false);
	public transient BooleanEntity showLineage = new BooleanEntity("Show Lineage",false);


	public PoseEntity() {
		super();
		setName("Pose");
		
		pickName = pickNameCounter++;
		
		addChild(showBoundingBox);
		addChild(showLocalOrigin);
		addChild(showLineage);
		
		pose.setIdentity();
		poseWorld.setIdentity();
	}

	public PoseEntity(String name) {
		this();
		setName(name);
	}
	
	public void set(PoseEntity b) {
		super.set(b);
		pose.set(b.pose);
		poseWorld.set(b.poseWorld);
		cuboid.set(b.cuboid);
	}

	public int getPickName() {
		return pickName;
	}

	/**
	 * Render this physicalEntity into the view
	 * @param gl2
	 */
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose.get());

			// helpful info
			if(showBoundingBox.get()) {
				cuboid.render(gl2);
			}
			if(showLocalOrigin.get()) {
				PrimitiveSolids.drawStar(gl2,10);
			}
			renderLineage(gl2);
			
			// draw children relative to parent
			for(Entity e : children ) {
				if(e instanceof PoseEntity) {
					((PoseEntity)e).render(gl2);
				}
			}
		gl2.glPopMatrix();
	}
	
	protected void renderLineage(GL2 gl2) {
		if(!showLineage.get()) return;
		
		boolean isLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);

		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
		gl2.glDepthFunc(GL2.GL_ALWAYS);

		// connection to children
		for(Entity e : children ) {
			if(e instanceof PoseEntity) {					
				gl2.glColor3d(255, 255, 255);
				Vector3d p = ((PoseEntity)e).getPosition();
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
		Vector3d trans = new Vector3d();
		pose.getTranslation(trans);
		return trans;
	}

	public void setPosition(Vector3d pos) {
		Matrix4d m = new Matrix4d(pose.get());
		m.setTranslation(pos);
		setPose(m);
	}
	
	/**
	 * Ask this entity "can you move to newPose?"
	 * @param newPose
	 * @return true if it can.
	 */
	public boolean canYouMoveTo(Matrix4d newPose) {
		// TODO add rules here.
		return true;
	}
	
	/**
	 * 
	 * @param arg0 fills the vector3 with one possible combination of radian rotations.
	 */
	public void getRotation(Vector3d arg0) {
		Matrix3d temp = new Matrix3d();
		pose.get().get(temp);
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
		arg0.set(pose.get());
		arg0.setTranslation(new Vector3d(0,0,0));
	}
	
	/**
	 * @return {@link Matrix4d} of the local pose
	 */
	public Matrix4d getPose() {
		return new Matrix4d(pose.get());
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
	}
	
	/**
	 * Recalculates poseWorld from pose and parent.poseWorld.  
	 * Does not crawl up the parent hierarchy.
	 */
	public void updatePoseWorld() {
		if(parent instanceof PoseEntity) {
			// this poseWorld is my pose * my parent's pose.
			PoseEntity peParent = (PoseEntity)parent;
			Matrix4d m = new Matrix4d(peParent.poseWorld.get());
			m.mul(pose.get());
			poseWorld.set(m);
		} else {
			// this poseWorld is my pose
			poseWorld.set(pose.get());
		}
		cuboid.setPoseWorld(this.getPoseWorld());
		
		for( Entity c : children ) {
			if(c instanceof PoseEntity) {
				PoseEntity pe = (PoseEntity)c;
				pe.updatePoseWorld();
			}
		}
	}

	
	/**
	 * @return {@link Matrix4d} of the world pose
	 */
	public Matrix4d getPoseWorld() {
		return new Matrix4d(poseWorld.get());
	}
	
	
	/**
	 * Set the pose and poseWorld of this item
	 * @param m
	 */
	public void setPoseWorld(Matrix4d m) {
		if(parent instanceof PoseEntity) {
			PoseEntity pep = (PoseEntity)parent;
			Matrix4d newPose = new Matrix4d(pep.poseWorld.get());
			newPose.invert();
			newPose.mul(m);
			setPose(newPose);
		} else {
			setPose(new Matrix4d(m));
		}
	}
	
	public Scene getWorld() {
		Entity p = parent;
		while (p != null) {
			if (p instanceof Scene) {
				return (Scene) p;
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
		
		cuboid.setPoseWorld(this.getPoseWorld());
		cuboidList.add(cuboid);

		return cuboidList;
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("P","Pose");
			pose.getView(view);
		view.popStack();
		view.pushStack("WP","World Pose");
			poseWorld.getView(view);
		view.popStack();
	}
}
