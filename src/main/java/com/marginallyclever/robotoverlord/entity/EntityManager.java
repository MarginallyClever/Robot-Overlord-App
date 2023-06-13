package com.marginallyclever.robotoverlord.entity;

import com.marginallyclever.convenience.AABB;
import com.marginallyclever.convenience.helpers.IntersectionHelper;
import com.marginallyclever.robotoverlord.Collidable;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * {@link EntityManager} is a container for all the {@link Entity} in a project.
 * It also contains the absolute path on disk for the root of the Scene.  All assets are relative to this path.
 *
 * @author Dan Royer
 * @since 1.6.0
 */
public class EntityManager {
	private static final Logger logger = LoggerFactory.getLogger(EntityManager.class);
	private final List<Entity> entities = new ArrayList<>();
	private final Entity rootEntity = new Entity("Scene");
	private final List<EntityManagerListener> entityManagerListeners = new ArrayList<>();
	
	public EntityManager() {
		super();
		rootEntity.addComponent(new PoseComponent());
		entities.add(rootEntity);
	}

	public void clear() {
		List<Entity> children = new ArrayList<>(rootEntity.getChildren());
		for( Entity child : children ) {
			removeEntityFromParent(child,rootEntity);
		}
	}

	/**
	 * @param listA all the cuboids being tested against the world.
	 * @return true if any cuboid in {@code listA} intersects any {@link AABB} in the world.
	 */
	@Deprecated
	// TODO does not belong here!
	public boolean collisionTest(ArrayList<AABB> listA) {
		// check all children
		for( Entity b : entities) {
			if( !(b instanceof Collidable) ) continue;
			
			ArrayList<AABB> listB = ((Collidable)b).getCuboidList();
			if( listB == null ) continue;
			
			if(listB.get(0)==listA.get(0)) {
				// don't test against yourself.
				continue;
			}
			
			// now we have both lists, test them against each other.
			for( AABB AABB1 : listA ) {
				for( AABB AABB2 : listB ) {
					if( IntersectionHelper.cuboidCuboid(AABB1, AABB2) ) {
						logger.info("Collision between "+
							listA.indexOf(AABB1)+
							" and "+
							b.getName()+"."+listB.indexOf(AABB2));
						return true;
					}
				}
			}
		}

		// no intersection
		return false;
	}

	public void addEntityToParent(Entity child,Entity parent) {
		if(child.getParent()!=null) {
			removeEntityFromParent(child,child.getParent());
		}
		parent.addEntity(child);
		fireEntityManagerEvent(new EntityManagerEvent(EntityManagerEvent.ENTITY_ADDED, child, parent));
	}

	public void removeEntityFromParent(Entity child,Entity parent) {
		if(!parent.children.contains(child)) return;

		parent.removeEntity(child);
		fireEntityManagerEvent(new EntityManagerEvent(EntityManagerEvent.ENTITY_REMOVED, child, parent));
	}

	public void fireEntityManagerEvent(EntityManagerEvent event) {
		for(EntityManagerListener listener : entityManagerListeners) {
			listener.entityManagerEvent(event);
		}
	}

	public void addListener(EntityManagerListener listener) {
		entityManagerListeners.add(listener);
	}

	public void removeListener(EntityManagerListener listener) {
		entityManagerListeners.remove(listener);
	}

	/**
	 * Find an entity by its unique ID.
	 * @param uuid the unique ID to search for.
	 * @return the entity with the given unique ID, or null if not found.
	 */
    public Entity findEntityByUniqueID(String uuid) {
		if(uuid==null) return null;

		Queue<Entity> toTest = new LinkedList<>(entities);
		while(!toTest.isEmpty()) {
			Entity entity = toTest.remove();

			if(entity.getUniqueID().equals(uuid)) return entity;

			toTest.addAll(entity.getChildren());
		}
		return null;
    }

	public CameraComponent getCamera() {
		Queue<Entity> toTest = new LinkedList<>(entities);
		while(!toTest.isEmpty()) {
			Entity entity = toTest.remove();

			CameraComponent camera = entity.findFirstComponentRecursive(CameraComponent.class);
			if(camera!=null) return camera;

			toTest.addAll(entity.getChildren());
		}
		return null;
	}

	/**
	 * Deep search for a child with this name.
	 * @param name the name to match
	 * @return the entity.  null if nothing found.
	 */
	@Deprecated
	public Entity findEntityWithName(String name) {
		List<Entity> list = new ArrayList<>(entities);
		while( !list.isEmpty() ) {
			Entity entity = list.remove(0);
			String objectName = entity.getName();

			if(name.equals(objectName)) return entity;

			list.addAll(entity.getChildren());
		}
		return null;
	}

	public List<Entity> getEntities() {
		return entities;
	}

	public Entity getRoot() {
		return entities.get(0);
	}

	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = new JSONObject();
		jo.put("scene", entities.get(0).toJSON(context));
		return jo;
	}

	public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
		entities.clear();
		entities.add(new Entity());
		if(jo.has("scene")) jo = jo.getJSONObject("scene");
		entities.get(0).parseJSON(jo,context);
	}

	/**
	 * <p>Move assets from source to here.  When projectA is imported into projectB, any
	 * asset with filename <i>projectA/xxxx.yyy</i> should be copied to <i>projectB/projectA/xxxx.yyy</i> and the
	 * asset filename in the destination project should be updated to match.</p>
	 * <p>When complete the source scene will only contain the root entity.</p>
	 *
	 * @param source the scene to copy from
	 */
	public void addScene(EntityManager source) {
		// when entities are added to destination they will automatically be removed from source.
		// to prevent concurrent modification exception we have to have a copy of the list.
		List<Entity> entities = new LinkedList<>(source.getRoot().getChildren());
		// now do the move safely.
		for(Entity e : entities) {
			this.addEntityToParent(e,this.getRoot());
		}
	}
}
