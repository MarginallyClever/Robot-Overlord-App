package com.marginallyclever.robotOverlord.entity.scene;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionTester;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.scene.demoObjectEntity.TrayCabinet;
import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.sixi2.Sixi2;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelEntity;
import com.marginallyclever.robotOverlord.log.Log;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;

/**
 * Container for all the visible objects in a scene.
 * @author Dan Royer
 * @since 1.6.0
 */
public class Scene extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4832192356002856296L;
	
	public ColorEntity ambientLight = new ColorEntity("Ambient light",0.2,0.2,0.2,1);
	
	public Scene() {
		super();
		setName("World");
	}
	
	public void createSixiDemo() {
		//addChild(new SkyBoxEntity());
		
		// adjust default camera
		RobotOverlord ro = (RobotOverlord)getRoot();
		ro.camera.setPosition(new Vector3d(40,-91,106));
		ro.camera.setPan(-16);
		ro.camera.setTilt(53);
		ro.camera.setZoom(100);
		ro.camera.update(0);
		
		// add some lights
    	LightEntity light;

		addChild(light = new LightEntity());
		light.setName("Light 1");
    	light.lightIndex=1;
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.attenuationLinear.set(0.0014);
    	light.attenuationQuadratic.set(7*1e-6);
    	light.setDirectional(true);
    	
		// add some collision bounds
		BoxEntity box;
		
		addChild(box = new BoxEntity());
		box.setName("Front wall");
		box.setSize(233.5,100,1);
		box.setPosition(new Vector3d(69.75,65,0));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);
		
		addChild(box = new BoxEntity());
		box.setName("Back wall");
		box.setSize(180,100,1);
		box.setPosition(new Vector3d(-47.5,-25.5,0));
		box.setRotation(new Vector3d(0, 0, Math.toRadians(-90)));
		box.getMaterial().setDiffuseColor(0f/255f,169f/255f,255f/255f,1f);

		ModelEntity table = new ModelEntity("/table.stl");
		addChild(table);
		table.setName("Table");
		table.setPosition(new Vector3d(0,0,-0.75));
		//box.setSize(160,1,110);
		//box.setPosition(new Vector3d(59.5,0,-2.5));
/*
		// adjust grid
		GridEntity grid = new GridEntity();
		addChild(grid);
		grid.width.set(140);
		grid.height.set(90);
		grid.setPosition(new Vector3d(60.0,0,-0.5));
*/
    	// add a sixi robot
		Sixi2 sixi2=new Sixi2();
		addChild(sixi2);
		//sixi2.setPosition(new Vector3d(78,-25,0));
		Matrix3d m=new Matrix3d();
		m.setIdentity();
		//m.rotZ(Math.toRadians(-90));
		sixi2.setRotation(m);
		
		TrayCabinet trayCabinet=new TrayCabinet();
		addChild(trayCabinet);
		trayCabinet.setPosition(new Vector3d(35,49.5,0));
		TrayCabinet trayCabinet2=new TrayCabinet();
		addChild(trayCabinet2);
		trayCabinet2.setPosition(new Vector3d(35,49.5,21.75));
	}
	
	public void render(GL2 gl2) {
		// Clear the screen and depth buffer
        gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
		
        // Don't draw triangles facing away from camera
		gl2.glCullFace(GL2.GL_BACK);
		
		// PASS 0: all the lights
    	
    	// global ambient light
		gl2.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, ambientLight.getFloatArray(),0);
		
		// point and spot lights
		for( Entity obj : children ) {
			if(obj instanceof LightEntity) {
				LightEntity light = (LightEntity)obj;
				light.setupLight(gl2);
			}
		}
		
		// PASS 1: everything not a light
		for( Entity obj : children ) {
			if(!(obj instanceof PoseEntity)) continue;
			if(obj instanceof LightEntity) continue;
			PoseEntity pe = (PoseEntity)obj;
			
			gl2.glPushName(pe.getPickName());
			pe.render(gl2);
			gl2.glPopName();
		}
		
		// PASS 2: everything transparent?

	}

	// Search only my children to find the PhysicalEntity with matchin pickName.
	public PoseEntity pickPhysicalEntityWithName(int pickName) {
		for( Entity obj : children ) {
			if(!(obj instanceof PoseEntity)) continue;
			PoseEntity pe = (PoseEntity)obj;
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
	public List<PoseEntity> findPhysicalObjectsNear(Vector3d target,double radius) {
		radius/=2;
		
		//Log.message("Finding within "+epsilon+" of " + target);
		List<PoseEntity> found = new ArrayList<PoseEntity>();
		
		// check all children
		for( Entity e : children ) {
			if(e instanceof PoseEntity) {
				// is physical, therefore has position
				PoseEntity po = (PoseEntity)e;
				//Log.message("  Checking "+po.getDisplayName()+" at "+pop);
				Vector3d pop = new Vector3d();
				pop.sub(po.getPosition(),target);
				if(pop.length()<=radius) {
					//Log.message("  in range!");
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
	public boolean collisionTest(PoseEntity a) {
		ArrayList<Cuboid> listA = a.getCuboidList();
		
		// check all children
		for( Entity b : children ) {
			// we do not test collide with self.  filter for all physical objects EXCEPT a.
			if( !( b instanceof PoseEntity ) || b==a ) continue;

			ArrayList<Cuboid> listB = ((PoseEntity)b).getCuboidList();
			if( listB == null ) continue;
			
			// now we have both lists, test them against each other.
			for( Cuboid cuboidA : listA ) {
				for( Cuboid cuboidB : listB ) {
					if( IntersectionTester.cuboidCuboid(cuboidA,cuboidB) ) {
						Log.message("Collision between "+
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
	public void getView(ViewPanel view) {
		view.pushStack("Sc", "Scene");
		view.add(ambientLight);
		view.popStack();
	}
}
