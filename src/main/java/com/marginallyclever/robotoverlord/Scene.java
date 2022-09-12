package com.marginallyclever.robotoverlord;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.IntersectionHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.LightComponent;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.entities.PoseEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.ColorEntity;

import javax.vecmath.Vector3d;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

	private final List<SceneChangeListener> sceneChangeListeners = new ArrayList<>();
	
	public Scene() {
		super();
	}

	@Override
	public void render(GL2 gl2) {
		renderWorldOrigin(gl2);
		renderLights(gl2);
		renderAllEntitiesWithMeshes(gl2);
		// PASS 2: everything transparent?
		//renderAllBoundingBoxes(gl2);
	}

	private void renderWorldOrigin(GL2 gl2) {
		PrimitiveSolids.drawStar(gl2,10);
	}


	private void renderAllEntitiesWithMeshes(GL2 gl2) {
		defaultMaterial.render(gl2);
		for(Entity child : entities) {
			renderEntitiesWithMeshes(gl2, child);
		}
	}

	private void renderEntitiesWithMeshes(GL2 gl2,Entity obj) {
		gl2.glPushName(obj.getPickName());

		PoseComponent pose = obj.findFirstComponent(PoseComponent.class);
		if(pose!=null) {
			renderOneEntityWithMeshAndPose(gl2,obj,pose);
		} else {
			renderOneEntityWithMesh(gl2, obj);
		}

		for (Entity child : obj.getEntities()) {
			renderEntitiesWithMeshes(gl2, child);
		}

		gl2.glPopName();
	}
	private void renderOneEntityWithMeshAndPose(GL2 gl2,Entity obj,PoseComponent pose) {
		gl2.glPushMatrix();
		MatrixHelper.applyMatrix(gl2,pose.getWorld());
		renderOneEntityWithMesh(gl2,obj);
		gl2.glPopMatrix();
	}

	private void renderOneEntityWithMesh(GL2 gl2,Entity obj) {
		MaterialComponent mat = obj.findFirstComponent(MaterialComponent.class);
		if(mat==null) mat = obj.findFirstComponentInParents(MaterialComponent.class);
		if(mat!=null && mat.getEnabled()) mat.render(gl2);

		List<ShapeComponent> shapes = obj.findAllComponents(ShapeComponent.class);
		for(ShapeComponent shape : shapes) {
			if(shape.getEnabled()) shape.render(gl2);
		}
	}

	private void renderLights(GL2 gl2) {
		// global ambient light
		gl2.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, ambientLight.getFloatArray(),0);

		turnOffAllLights(gl2);

		Queue<Entity> found = new LinkedList<>(entities);
		int i=0;
		while(!found.isEmpty()) {
			Entity obj = found.remove();
			LightComponent light = obj.findFirstComponent(LightComponent.class);
			if(light!=null && light.getEnabled()) {
				light.setupLight(gl2,i++);
				if(i==GL2.GL_MAX_LIGHTS) return;
			}
			found.addAll(obj.entities);
		}
	}

	private void turnOffAllLights(GL2 gl2) {
		for(int i=0;i<GL2.GL_MAX_LIGHTS;++i) {
			gl2.glDisable(GL2.GL_LIGHT0+i);
		}
	}

	// Search only my children to find the PhysicalEntity with matching pickName.
	public Entity pickEntityWithName(int pickName) {
		Queue<Entity> found = new LinkedList<>(entities);
		while(!found.isEmpty()) {
			Entity obj = found.remove();
			if( obj.getPickName()==pickName ) {
				return obj;  // found!
			}
			found.addAll(obj.entities);
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
	public List<PoseEntity> findPhysicalObjectsNear(Vector3d target, double radius) {
		radius/=2;
		
		//Log.message("Finding within "+epsilon+" of " + target);
		List<PoseEntity> found = new ArrayList<PoseEntity>();
		
		// check all children
		for( Entity e : entities) {
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
		for( Entity b : entities) {
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
		view.pushStack("Sc", true);
		view.add(ambientLight);
		view.popStack();
	}

	public void addEntityToParent(Entity parent, Entity entity) {
		for(SceneChangeListener listener : sceneChangeListeners) {
			listener.addEntityToParent(parent,entity);
		}
	}

	public void removeEntityFromParent(Entity parent, Entity entity) {
		for(SceneChangeListener listener : sceneChangeListeners) {
			listener.removeEntityFromParent(parent,entity);
		}
	}

	public void addSceneChangeListener(SceneChangeListener listener) {
		sceneChangeListeners.add(listener);
	}

	public void removeSceneChangeListener(SceneChangeListener listener) {
		sceneChangeListeners.remove(listener);
	}
}
