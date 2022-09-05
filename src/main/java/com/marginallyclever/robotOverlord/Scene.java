package com.marginallyclever.robotOverlord;

import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.components.*;
import com.marginallyclever.robotOverlord.components.sceneElements.SkyBoxEntity;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.ColorEntity;

/**
 * Container for all the visible objects in a scene.
 * @author Dan Royer
 * @since 1.6.0
 */
public class Scene extends Entity {
	@Serial
	private static final long serialVersionUID = 2990084741436544957L;

	private final ColorEntity ambientLight = new ColorEntity("Ambient light",0.2,0.2,0.2,1);
	private final MaterialComponent defaultMaterial = new MaterialComponent();
	public final SkyBoxEntity sky = new SkyBoxEntity();
	
	public Scene() {
		super();
		setName(Scene.class.getSimpleName());
		addChild(sky);
	}

	@Override
	public void render(GL2 gl2) {
		clearAll(gl2);

        // Don't draw triangles facing away from camera
		gl2.glCullFace(GL2.GL_BACK);

		sky.render(gl2);
		renderWorldOrigin(gl2);

		renderLights(gl2);
		renderAllEntitiesWithMeshes(gl2);
		// PASS 2: everything transparent?
		//renderAllBoundingBoxes(gl2);
	}

	private void renderWorldOrigin(GL2 gl2) {
		PrimitiveSolids.drawStar(gl2,10);
	}

	private void clearAll(GL2 gl2) {
		// Clear the screen and depth buffer
		//gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);
		gl2.glClear(GL2.GL_DEPTH_BUFFER_BIT);
	}

	private void renderAllEntitiesWithMeshes(GL2 gl2) {
		defaultMaterial.render(gl2);
		for(Entity child : children) {
			renderEntitiesWithMeshes(gl2, child);
		}
	}

	private void renderEntitiesWithMeshes(GL2 gl2,Entity obj) {
		gl2.glPushName(obj.getPickName());

		PoseComponent pose = obj.getComponent(PoseComponent.class);
		if(pose!=null) {
			renderOneEntityWithMeshAndPose(gl2,obj,pose);
		} else {
			renderOneEntityWithMesh(gl2, obj);
		}

		for (Entity child : obj.getChildren()) {
			renderEntitiesWithMeshes(gl2, child);
		}

		gl2.glPopName();
	}
	private void renderOneEntityWithMeshAndPose(GL2 gl2,Entity obj,PoseComponent pose) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2,pose.getWorld());
		//PrimitiveSolids.drawStar(gl2,1);
		renderOneEntityWithMesh(gl2,obj);
		gl2.glPopMatrix();
	}

	private void renderOneEntityWithMesh(GL2 gl2,Entity obj) {
		MaterialComponent mat = obj.getComponent(MaterialComponent.class);
		if(mat==null) mat = obj.findFirstComponentInParents(MaterialComponent.class);
		if(mat!=null && mat.getEnabled()) mat.render(gl2);

		ShapeComponent shape = obj.getComponent(ShapeComponent.class);
		if(shape!=null && shape.getEnabled()) shape.render(gl2);
	}

	private void renderLights(GL2 gl2) {
		// global ambient light
		gl2.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, ambientLight.getFloatArray(),0);

		turnOffAllLights(gl2);

		Queue<Entity> found = new LinkedList<>(children);
		int i=0;
		while(!found.isEmpty()) {
			Entity obj = found.remove();
			LightComponent light = obj.getComponent(LightComponent.class);
			if(light!=null && light.getEnabled()) {
				light.setupLight(gl2,i++);
				if(i==GL2.GL_MAX_LIGHTS) return;
			}
			found.addAll(obj.children);
		}
	}

	private void turnOffAllLights(GL2 gl2) {
		for(int i=0;i<GL2.GL_MAX_LIGHTS;++i) {
			gl2.glDisable(GL2.GL_LIGHT0+i);
		}
	}

	// Search only my children to find the PhysicalEntity with matching pickName.
	public Entity pickEntityWithName(int pickName) {
		Queue<Entity> found = new LinkedList<>(children);
		while(!found.isEmpty()) {
			Entity obj = found.remove();
			if( obj.getPickName()==pickName ) {
				return obj;  // found!
			}
			found.addAll(obj.children);
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
