package com.marginallyclever.robotOverlord.entity.world;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionTester;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.boxEntity.BoxEntity;
import com.marginallyclever.robotOverlord.entity.cameraEntity.CameraEntity;
import com.marginallyclever.robotOverlord.entity.gridEntity.GridEntity;
import com.marginallyclever.robotOverlord.entity.lightEntity.LightEntity;
import com.marginallyclever.robotOverlord.entity.physicalEntity.PhysicalEntity;
import com.marginallyclever.robotOverlord.entity.robotEntity.sixi2.Sixi2;
import com.marginallyclever.robotOverlord.uiElements.ViewCube;

/**
 * Container for all the visible objects in a scene.
 * @author Dan Royer
 */
public class World extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4832192356002856296L;
	
	public final static Matrix4d pose = new Matrix4d();
	// TODO lose these junk vectors that don't match assumptions, anyhow.
	public final static Vector3d forward = new Vector3d(0,0,1);
	public final static Vector3d right = new Vector3d(1,0,0);
	public final static Vector3d up = new Vector3d(0,1,0);
	
	protected CameraEntity camera = new CameraEntity();
	
	// ray picking
	protected transient Vector3d pickForward=new Vector3d();
	protected transient Vector3d pickRight=new Vector3d();
	protected transient Vector3d pickUp=new Vector3d();
	protected transient Vector3d pickRay=new Vector3d();
	
	// The box in the top right of the user view that shows your orientation in the world.
	// TODO probably doesn't belong here, it's per-user?  per-camera?
	protected transient ViewCube viewCube = new ViewCube();

	
	protected transient WorldPanel worldPanel;
	
	public World() {
		super();
		setName("World");
		addChild(camera);
	}
	
	public void createDefaultWorld() {
		//addChild(new SkyBoxEntity());
		
		// adjust grid
		GridEntity grid;
		addChild(grid = new GridEntity());
		grid.width.set(130);
		grid.height.set(70);
		grid.setPosition(new Vector3d(30,0,-0.5));
		
		// adjust default camera
		camera.setPosition(new Vector3d(0,-100,65));
		//camera.setPan(52);
		camera.setTilt(76);
		
		// add some lights
    	LightEntity light;
    	 
		addChild(light = new LightEntity());
		light.setName("Ambient light");
    	light.index=0;
    	light.setPosition(new Vector3d(0,0,30));
    	light.setAmbient(2.55f,2.55f,2.51f,1);

		addChild(light = new LightEntity());
		light.setName("light 1");
    	light.index=1;
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(80,80,80,1);
    	light.attenuationConstant.set(0.5);
    	light.attenuationLinear.set(0.4);

		// add some collision bounds
		BoxEntity box;
		
		addChild(box = new BoxEntity());
		box.setName("Front wall");
		box.setSize(160,100,1);
		box.setPosition(new Vector3d(30,40,0));
		box.getMaterial().setDiffuseColor(117f/255f,169f/255f,207f/255f,1f);
		
		addChild(box = new BoxEntity());
		box.setName("Back wall");
		box.setSize(100,100,1);
		box.setPosition(new Vector3d(-50,-10,0));
		box.setRotation(new Vector3d(0, 0, Math.toRadians(-90)));
		box.getMaterial().setDiffuseColor(117f/255f,169f/255f,207f/255f,1f);

		addChild(box = new BoxEntity());
		box.setName("Table");
		box.setSize(150,1,80);
		box.setPosition(new Vector3d(30,0,-2.5));
		
    	// add a sixi robot
		Sixi2 sixi2=new Sixi2();
		addChild(sixi2);
		//sixi2.setPosition(new Vector3d(78,-25,0));
		Matrix3d m=new Matrix3d();
		m.setIdentity();
		//m.rotZ(Math.toRadians(-90));
		sixi2.setRotation(m);
	}
	
	public ArrayList<JPanel> getContextPanels(RobotOverlord gui) {
		// Do not allow access to the Entity, because deleting/renaming the world makes no sense to me.
		ArrayList<JPanel> list = new ArrayList<JPanel>();
		
		worldPanel = new WorldPanel(gui,this);
		list.add(worldPanel);

		return list;
	}
	
	public void render(GL2 gl2) {
		// Clear the screen and depth buffer
		// background color
    	//gl2.glClearColor(212.0f/255.0f, 233.0f/255.0f, 255.0f/255.0f, 1.0f);
        // Don't draw triangles facing away from camera
		gl2.glCullFace(GL2.GL_BACK);
		// draw to the back buffer, so we can swap buffer later and avoid vertical sync tearing
    	gl2.glDrawBuffer(GL2.GL_BACK);

		// DRAW THE WORLD
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
		gl2.glLoadIdentity();
		gl2.glPushMatrix();

			camera.render(gl2);
	
			// lights
			for( Entity obj : children ) {
				if(obj instanceof LightEntity) {
					PhysicalEntity light = (PhysicalEntity)obj;
					light.render(gl2);
				}
			}
			
			// draw!
			for( Entity obj : children ) {
				if(!(obj instanceof PhysicalEntity)) continue;
				if(obj instanceof LightEntity) continue;
				if(obj instanceof CameraEntity) continue;
				PhysicalEntity pe = (PhysicalEntity)obj;
				
				gl2.glPushName(pe.getPickName());
				pe.render(gl2);
				gl2.glPopName();
			}
	
			showPickingTest(gl2);

		gl2.glPopMatrix();
	
		
		// DRAW THE HUD
		gl2.glPushMatrix();
			camera.render(gl2);
			
			viewCube.render(gl2,getCamera());
		gl2.glPopMatrix();
	}

	protected void showPickingTest(GL2 gl2) {
		if(pickForward.lengthSquared()<1e-6) return;
		
		gl2.glPushMatrix();
		gl2.glDisable(GL2.GL_LIGHTING);

		Vector3d forward = new Vector3d();
		forward.set(pickForward);
		forward.scale(10);
		forward.sub(camera.getPosition());
		gl2.glColor3f(1,0,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickForward);
		forward.scale(10);
		forward.add(pickRight);
		forward.sub(camera.getPosition());
		gl2.glColor3f(0,1,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickForward);
		forward.scale(10);
		forward.add(pickUp);
		forward.sub(camera.getPosition());
		gl2.glColor3f(0,0,1);
		PrimitiveSolids.drawStar(gl2, forward);
		
		forward.set(pickRay);
		forward.scale(10);
		forward.sub(camera.getPosition());
		gl2.glColor3f(1,1,0);
		PrimitiveSolids.drawStar(gl2, forward);
		
		gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glPopMatrix();
	}

	// Search only my children to find the PhysicalEntity with matchin pickName.
	public PhysicalEntity pickPhysicalEntityWithName(int pickName) {
		if(pickName==0) {
			// Hit nothing!  Default to camera controls
			return camera;
		}

		for( Entity obj : children ) {
			if(!(obj instanceof PhysicalEntity)) continue;
			PhysicalEntity pe = (PhysicalEntity)obj;
			if( pe.getPickName()==pickName ) {
				return pe;  // found!
			}
		}
		
		return null;
	}
		
	public CameraEntity getCamera() {
		return camera;
	}
		
	/**
	 * Find all Entities within epsilon mm of pose.
	 * TODO Much optimization could be done here to reduce the search time.
	 * @param target the center of the cube around which to search.   
	 * @param radius the maximum distance to search for entities.
	 * @return a list of found PhysicalObjects
	 */
	public List<PhysicalEntity> findPhysicalObjectsNear(Vector3d target,double radius) {
		radius/=2;
		
		//System.out.println("Finding within "+epsilon+" of " + target);
		List<PhysicalEntity> found = new ArrayList<PhysicalEntity>();
		
		// check all children
		for( Entity e : children ) {
			if(e instanceof PhysicalEntity) {
				// is physical, therefore has position
				PhysicalEntity po = (PhysicalEntity)e;
				//System.out.println("  Checking "+po.getDisplayName()+" at "+pop);
				Vector3d pop = new Vector3d();
				pop.sub(po.getPosition(),target);
				if(pop.length()<=radius) {
					//System.out.println("  in range!");
					// in range!
					found.add(po);
				}
			}
		}
		
		return found;
	}

	/**
	 * @param listA all the cuboids being tested against the world.
	 * @param ignoreList all the entities in the world to ignore.
	 * @return true if any cuboid in the cuboidList intersects any cuboid in the world.
	 */
	public boolean collisionTest(PhysicalEntity a) {
		ArrayList<Cuboid> listA = a.getCuboidList();
		
		// check all children
		for( Entity b : children ) {
			// we do not test collide with self.  filter for all physical objects EXCEPT a.
			if( !( b instanceof PhysicalEntity ) || b==a ) continue;

			ArrayList<Cuboid> listB = ((PhysicalEntity)b).getCuboidList();
			if( listB == null ) continue;
			
			// now we have both lists, test them against each other.
			for( Cuboid cuboidA : listA ) {
				for( Cuboid cuboidB : listB ) {
					if( IntersectionTester.cuboidCuboid(cuboidA,cuboidB) ) {
						System.out.println("Collision between "+
							a.getName()+"."+listA.indexOf(cuboidA)+
							" and "+
							b.getName()+"."+listB.indexOf(cuboidB));
						return true;
					}
				}
			}
		}

		// no intersection
		return false;
	}
	
	public WorldPanel getWorldPanel() {
		return worldPanel;
	}
}
