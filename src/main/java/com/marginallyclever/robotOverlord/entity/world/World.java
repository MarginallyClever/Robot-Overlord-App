package com.marginallyclever.robotOverlord.entity.world;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionTester;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.primitives.BoxEntity;
import com.marginallyclever.robotOverlord.entity.primitives.GridEntity;
import com.marginallyclever.robotOverlord.entity.primitives.LightEntity;
import com.marginallyclever.robotOverlord.entity.primitives.PhysicalEntity;
import com.marginallyclever.robotOverlord.entity.robotEntity.dhRobotEntity.sixi2.Sixi2;
import com.marginallyclever.robotOverlord.swingInterface.view.View;

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

	public World() {
		super();
		setName("World");
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
		RobotOverlord ro = (RobotOverlord)getRoot();
		ro.camera.setPosition(new Vector3d(0,-100,65));
		ro.camera.setPan(52);
		ro.camera.setTilt(76);
		ro.camera.update(0);
		
		// add some lights
    	LightEntity light;
    	 
		addChild(light = new LightEntity());
		light.setName("Ambient light");
    	light.lightIndex=0;
    	light.setPosition(new Vector3d(0,0,30));
    	light.setAmbient(2.55f,2.55f,2.51f,1);

		addChild(light = new LightEntity());
		light.setName("Light 1");
    	light.lightIndex=1;
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.attenuationLinear.set(0.0014);
    	light.attenuationQuadratic.set(0.0007);
    	
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
	
	public void render(GL2 gl2) {
		// Clear the screen and depth buffer
		// background color
    	//gl2.glClearColor(212.0f/255.0f, 233.0f/255.0f, 255.0f/255.0f, 1.0f);
        // Don't draw triangles facing away from camera
		gl2.glCullFace(GL2.GL_BACK);
		// draw to the back buffer, so we can swap buffer later and avoid vertical sync tearing
    	gl2.glDrawBuffer(GL2.GL_BACK);
	
		// pass 1: all the lights
		for( Entity obj : children ) {
			if(obj instanceof LightEntity) {
				PhysicalEntity light = (PhysicalEntity)obj;
				light.render(gl2);
			}
		}
		
		// pass 2: everything not a light
		for( Entity obj : children ) {
			if(!(obj instanceof PhysicalEntity)) continue;
			if(obj instanceof LightEntity) continue;
			PhysicalEntity pe = (PhysicalEntity)obj;
			
			gl2.glPushName(pe.getPickName());
			pe.render(gl2);
			gl2.glPopName();
		}
		
		// pass 3: everything transparent?

	}

	// Search only my children to find the PhysicalEntity with matchin pickName.
	public PhysicalEntity pickPhysicalEntityWithName(int pickName) {
		for( Entity obj : children ) {
			if(!(obj instanceof PhysicalEntity)) continue;
			PhysicalEntity pe = (PhysicalEntity)obj;
			if( pe.getPickName()==pickName ) {
				return pe;  // found!
			}
		}
		
		return null;
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
	
	@Override
	public void getView(View view) {

	}
}
