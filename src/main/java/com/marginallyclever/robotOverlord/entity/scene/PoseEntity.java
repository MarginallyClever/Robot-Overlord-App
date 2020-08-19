package com.marginallyclever.robotOverlord.entity.scene;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.event.UndoableEditEvent;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;
import com.marginallyclever.robotOverlord.swingInterface.actions.ActionPoseEntityMoveWorld;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * A object in the world with a position and orientation (collectively, a "pose")
 * @author Dan Royer
 *
 */
public class PoseEntity extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1959037711655040359L;
	
	// unique ids for all objects in the world.  
	// zero is reserved to indicate no object.
	@JsonIgnore
	static private int pickNameCounter=1;
	// my unique id
	@JsonIgnore
	private int pickName;	
	
	// pose relative to my parent Entity.
	public Matrix4d pose = new Matrix4d();
	// pose relative to the world.
	@JsonIgnore
	public Matrix4d poseWorld = new Matrix4d();
	
	protected ReentrantLock lock1 = new ReentrantLock();
	protected ReentrantLock lock2 = new ReentrantLock();
	
	// physical limits
	@JsonIgnore
	public transient Cuboid cuboid = new Cuboid();

	@JsonIgnore
	public transient BooleanEntity showBoundingBox = new BooleanEntity("Show Bounding Box",false);
	@JsonIgnore
	public transient BooleanEntity showLocalOrigin = new BooleanEntity("Show Local Origin",false);
	@JsonIgnore
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
	 * Render this PoseEntity to the display
	 * @param gl2
	 */
	public void render(GL2 gl2) {
		gl2.glPushMatrix();
			MatrixHelper.applyMatrix(gl2, pose);

			// helpful info
			if(showBoundingBox.get()) cuboid.render(gl2);
			if(showLocalOrigin.get()) PrimitiveSolids.drawStar(gl2,10);
			if(showLineage.get()) renderLineage(gl2);
			
			// draw children relative to parent
			for(Entity e : children ) {
				if(e instanceof PoseEntity) {
					((PoseEntity)e).render(gl2);
				}
			}
		gl2.glPopMatrix();
	}
	
	public void renderLineage(GL2 gl2) {
		boolean isTex = gl2.glIsEnabled(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_TEXTURE_2D);

		// save the lighting mode
		boolean lightWasOn = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);

		IntBuffer depthFunc = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_DEPTH_FUNC, depthFunc);
		gl2.glDepthFunc(GL2.GL_ALWAYS);
		//boolean depthWasOn = gl2.glIsEnabled(GL2.GL_DEPTH_TEST);
		//gl2.glDisable(GL2.GL_DEPTH_TEST);

		gl2.glColor4d(1,1,1,1);
		gl2.glBegin(GL2.GL_LINES);
		// connection to children
		for(Entity e : children ) {
			if(e instanceof PoseEntity) {					
				Vector3d p = ((PoseEntity)e).getPosition();
				gl2.glVertex3d(0, 0, 0);
				gl2.glVertex3d(p.x,p.y,p.z);
			}
		}
		gl2.glEnd();

		//if(depthWasOn) gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glDepthFunc(depthFunc.get());
		// restore lighting
		if(lightWasOn) gl2.glEnable(GL2.GL_LIGHTING);
		if(isTex) gl2.glDisable(GL2.GL_TEXTURE_2D);
	}

	public Vector3d getPosition() {
		Vector3d trans = new Vector3d();
		pose.get(trans);
		return trans;
	}

	public void setPosition(Vector3d pos) {
		Matrix4d m = new Matrix4d(pose);
		m.setTranslation(pos);
		setPose(m);
	}
	
	/**
	 * Ask this entity "can you move to newWorldPose?"
	 * @param newWorldPose the desired world pose of the PoseEntity.
	 * @return true if it can.
	 */
	public boolean canYouMoveTo(Matrix4d newWorldPose) {
		// TODO add rules here.
		return true;
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
		//if(!arg0.epsilonEquals(pose.get(), 1e-6)) {
			pose.set(arg0);
			setChanged();
			updatePoseWorld();
			notifyObservers();
		//}
	}
	
	/**
	 * Recalculates poseWorld from pose and parent.poseWorld.  
	 * Does not crawl up the parent hierarchy.
	 */
	public void updatePoseWorld() {
		Matrix4d m;
		if(parent instanceof PoseEntity) {
			// this poseWorld is my pose * my parent's pose.
			PoseEntity peParent = (PoseEntity)parent;
			m = new Matrix4d(peParent.poseWorld);
			m.mul(pose);
		} else {
			// this poseWorld is my pose
			m = pose;
		}
		

		//if(!m.epsilonEquals(poseWorld.get(), 1e-6))
		{
			poseWorld.set(m);
			cuboid.setPoseWorld(this.getPoseWorld());
			
			for( Entity c : children ) {
				if(c instanceof PoseEntity) {
					PoseEntity pe = (PoseEntity)c;
					pe.updatePoseWorld();
				}
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
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
		if(parent instanceof PoseEntity) {
			PoseEntity pep = (PoseEntity)parent;
			Matrix4d newPose = new Matrix4d(pep.poseWorld);
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
	
	public Matrix4d findMajorAxisTarget(Matrix4d from) {		
		// snap Z axis to major value		
		Vector3d vz = MatrixHelper.getZAxis(from);
		
		double zx = Math.abs(vz.x);
		double zy = Math.abs(vz.y);
		double zz = Math.abs(vz.z);
		
		Vector3d nx=new Vector3d();
		Vector3d ny=new Vector3d();
		Vector3d nz = new Vector3d(); 
		if(zx>zy) {
			if(zx>zz) nz.x = Math.signum(vz.x);  //zx wins
			else      nz.z = Math.signum(vz.z);  //zz wins
		} else {
			if(zy>zz) nz.y = Math.signum(vz.y);  //zy wins
			else      nz.z = Math.signum(vz.z);  //zz wins
		}
		
		// snap X to major value
		nx.cross(MatrixHelper.getYAxis(from),nz);
		// make Y orthogonal to X and Z
		ny.cross(nz, nx);
		// build the new matrix.  Make sure m33=1 and position data is copied in.
		Matrix4d m = new Matrix4d(from);
		MatrixHelper.setXAxis(m,nx);
		MatrixHelper.setYAxis(m,ny);
		MatrixHelper.setZAxis(m,nz);
		return m;
	}
	
	public void snapToMajorAxis() {
		Matrix4d m = findMajorAxisTarget(poseWorld);
		if(m!=null) {
			RobotOverlord ro = (RobotOverlord)getRoot();
			if(canYouMoveTo(m)) {
				ro.undoableEditHappened(new UndoableEditEvent(this,new ActionPoseEntityMoveWorld(this,m) ) );
			}
		}
	}
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("P","Pose");

		view.add(showBoundingBox);
		view.add(showLocalOrigin);
		view.add(showLineage);
		
		ViewElementButton bSnap = view.addButton("Snap Z to major axis");
		bSnap.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				snapToMajorAxis();
			}
		});
		//view.addStaticText("Pick name="+getPickName());
		//	pose.getView(view);
		view.popStack();
		view.pushStack("WP","World Pose");
		view.add(new Vector3dEntity("Position",MatrixHelper.getPosition(poseWorld)));
		//	poseWorld.getView(view);
		view.popStack();
	}
	
	@Override
	public boolean canBeRenamed() {
		return true;
	}
}
