package com.marginallyclever.robotOverlord.entity.scene;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.scene.demoObjectEntity.TrayCabinet;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.skycam.Skycam;
import com.marginallyclever.robotOverlord.entity.scene.shapeEntity.ShapeEntity;
import com.marginallyclever.robotOverlord.entity.sixi3.Sixi3FK;
import com.marginallyclever.robotOverlord.entity.sixi3.Sixi3IK;
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
	private static final long serialVersionUID = 2990084741436544957L;
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
		light.setName("Light");
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

		ShapeEntity table = new ShapeEntity("/table.stl");
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
		//Sixi2 sixi2=new Sixi2();
		//addChild(sixi2);
		Sixi3FK s0 = new Sixi3FK();
		addChild(s0);
		
		Sixi3IK s1 = new Sixi3IK();
		addChild(s1);
		s1.setPosition(new Vector3d(50,0,0));
		
		
		//sixi2.setPosition(new Vector3d(78,-25,0));
		//Matrix3d m=new Matrix3d();
		//m.setIdentity();
		//m.rotZ(Math.toRadians(-90));
		//sixi2.setRotation(m);
		
		TrayCabinet trayCabinet=new TrayCabinet();
		trayCabinet.setName("Cabinet");
		addChild(trayCabinet);
		trayCabinet.setPosition(new Vector3d(35,49.5,0));
		TrayCabinet trayCabinet2=new TrayCabinet();
		trayCabinet2.setName("Cabinet");
		addChild(trayCabinet2);
		trayCabinet2.setPosition(new Vector3d(35,49.5,21.75));
	}
	
	public void createSkycamDemo() {		
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
		light.setName("Light");
    	light.lightIndex=1;
    	light.setPosition(new Vector3d(60,-60,160));
    	light.setDiffuse(1,1,1,1);
    	light.setSpecular(0.5f, 0.5f, 0.5f, 1.0f);
    	light.attenuationLinear.set(0.0014);
    	light.attenuationQuadratic.set(7*1e-6);
    	light.setDirectional(true);
    	
		// adjust grid
		GridEntity grid = new GridEntity();
		addChild(grid);
		grid.width.set(140);
		grid.height.set(90);
		grid.setPosition(new Vector3d(60.0,0,-0.5));
		
    	// add a sixi robot
		Skycam skycam=new Skycam();
		addChild(skycam);
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
			if(obj instanceof LightEntity) continue;

			// name for picking
			if(obj instanceof PoseEntity) {
				gl2.glPushName(((PoseEntity)obj).getPickName());
			}
			
			obj.render(gl2);
			
			// name for picking
			if(obj instanceof PoseEntity) {
				gl2.glPopName();
			}
		}
		
		// PASS 2: everything transparent?
		//renderAllBoundingBoxes(gl2);
	}
	
	private void renderAllBoundingBoxes(GL2 gl2) {
		// turn of textures so lines draw good
		boolean wasTex = gl2.glIsEnabled(GL2.GL_TEXTURE_2D);
		gl2.glDisable(GL2.GL_TEXTURE_2D);
		// turn off lighting so lines draw good
		boolean wasLit = gl2.glIsEnabled(GL2.GL_LIGHTING);
		gl2.glDisable(GL2.GL_LIGHTING);
		// draw on top of everything else
		int wasOver=OpenGLHelper.drawAtopEverythingStart(gl2);

		renderAllBoundingBoxes(gl2,this);
		
		// return state if needed
		OpenGLHelper.drawAtopEverythingEnd(gl2,wasOver);
		if(wasLit) gl2.glEnable(GL2.GL_LIGHTING);
		if(wasTex) gl2.glEnable(GL2.GL_TEXTURE_2D);	
	}
	
	private void renderAllBoundingBoxes(GL2 gl2, Entity me) {
		for( Entity child : me.getChildren() ) {
			if(child instanceof Collidable) {
				ArrayList<Cuboid> list = ((Collidable)child).getCuboidList();
				for( Cuboid c : list ) {
					c.render(gl2);
				}
			}
			renderAllBoundingBoxes(gl2,child);
		}
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
	 * @return true if any cuboid in {@code listA} intersects any {@link Cuboid} in the world.
	 */
	public boolean collisionTest(ArrayList<Cuboid> listA) {
		
		// check all children
		for( Entity b : children ) {
			if( !(b instanceof Collidable) ) continue;
			
			ArrayList<Cuboid> listB = ((Collidable)b).getCuboidList();
			if( listB == null ) continue;
			
			if(listB.get(0)==listA.get(0)) {
				// don't test against yourself.
				continue;
			}
			
			// now we have both lists, test them against each other.
			for( Cuboid cuboidA : listA ) {
				for( Cuboid cuboidB : listB ) {
					if( IntersectionHelper.cuboidCuboid(cuboidA,cuboidB) ) {
						Log.message("Collision between "+
							listA.indexOf(cuboidA)+
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
