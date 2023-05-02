package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.*;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * {@link EntityManager} is a container for all the {@link Entity} in a project.
 * It also contains the absolute gcodepath on disk for the root of the Scene.  All assets are relative to this gcodepath.
 *
 * @author Dan Royer
 * @since 1.6.0
 */
public class EntityManager {
	private static final Logger logger = LoggerFactory.getLogger(EntityManager.class);
	private final StringParameter scenePath = new StringParameter("Scene Path", "");
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
	 * Initialize the scene with a gcodepath to the root of the project.
	 * @param absolutePath the absolute gcodepath to the root of the project.
	 */
	public EntityManager(String absolutePath) {
		this();
		setScenePath(absolutePath);
	}

	/**
	 * @param listA all the cuboids being tested against the world.
	 * @return true if any cuboid in {@code listA} intersects any {@link AABB} in the world.
	 */
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
		parent.addEntity(child);
		for(EntityManagerListener listener : entityManagerListeners) {
			listener.addEntityToParent(parent,child);
		}
	}

	public void removeEntityFromParent(Entity child,Entity parent) {
		parent.removeEntity(child);
		for(EntityManagerListener listener : entityManagerListeners) {
			listener.removeEntityFromParent(parent,child);
		}
	}

	public void addSceneChangeListener(EntityManagerListener listener) {
		entityManagerListeners.add(listener);
	}

	public void removeSceneChangeListener(EntityManagerListener listener) {
		entityManagerListeners.remove(listener);
	}

	/**
	 * Set the scene gcodepath.  This is the gcodepath to the directory containing the scene file.
	 * @param absolutePath the absolute gcodepath to the scene directory.
	 */
	public void setScenePath(String absolutePath) {
		File file = new File(absolutePath);
		if(!file.exists()) throw new RuntimeException("File does not exist: "+absolutePath);
		if(!file.isDirectory()) throw new RuntimeException("Not a directory: "+absolutePath);
		//if(!entities.isEmpty()) throw new RuntimeException("Cannot change the scene gcodepath when entities are present.");

		logger.debug("Setting scene gcodepath to "+absolutePath);
		scenePath.set(absolutePath);
	}

	public String getScenePath() {
		return scenePath.get();
	}

	/**
	 * Returns true if unCheckedAssetFilename is in the scene gcodepath.
	 * @param unCheckedAssetFilename a file that may or may not be within the scene gcodepath.
	 * @return true if unCheckedAssetFilename is in the scene gcodepath.
	 */
	public boolean isAssetPathInScenePath(String unCheckedAssetFilename) {
		Path input = Paths.get(unCheckedAssetFilename);
		Path scene = Paths.get(getScenePath());
		return input.toAbsolutePath().startsWith(scene.toAbsolutePath());
	}

	/**
	 * Displays a warning to the user if the asset is not within the scene gcodepath.
	 * @param unCheckedAssetFilename a file that may or may not be within the scene gcodepath.
	 */
	public void warnIfAssetPathIsNotInScenePath(String unCheckedAssetFilename) {
		if(isAssetPathInScenePath(unCheckedAssetFilename)) return;

		String message = Translator.get("Scene.AssetPathNotInScenePathWarning");
		message = message.replace("%1", unCheckedAssetFilename);
		message = message.replace("%2", getScenePath());
		logger.warn("asset "+unCheckedAssetFilename+" not in scene gcodepath: "+getScenePath());

		// try to show a pop-up if we have a display
		if(!GraphicsEnvironment.isHeadless()) {
			JOptionPane.showMessageDialog(
				null,
				message,
				Translator.get("Scene.AssetPathNotInScenePathWarningTitle"),
				JOptionPane.WARNING_MESSAGE);
		}
	}

	public String checkForScenePath(String fn) {
		if (!isAssetPathInScenePath(fn)) {
			String fn2 = addScenePath(fn);
			if ((new File(fn2)).exists()) {
				return fn2;
			}
		} else {
			warnIfAssetPathIsNotInScenePath(fn);
		}
		return fn;
	}

	/**
	 * Returns the relative gcodepath to the asset, or absolute if the asset is not within the scene gcodepath.
	 * @param unCheckedAssetFilename a file that may or may not be within the scene gcodepath.
	 * @return the relative gcodepath to the asset, or absolute if the asset is not within the scene gcodepath.
	 */
	public String removeScenePath(String unCheckedAssetFilename) {
		if(unCheckedAssetFilename==null) return null;

		String scenePathValue = getScenePath();
		if(unCheckedAssetFilename.startsWith(scenePathValue)) {
			return unCheckedAssetFilename.substring(scenePathValue.length());
		}
		return unCheckedAssetFilename;
	}

	public String addScenePath(String fn) {
		return getScenePath() + fn;
	}

	/**
	 * Find an entity by its unique ID.
	 * @param uuid the unique ID to search for.
	 * @return the entity with the given unique ID, or null if not found.
	 */
    public Entity findEntityByUniqueID(String uuid) {
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

	public JSONObject toJSON() {
		JSONObject jo = new JSONObject();
		jo.put("scene", entities.get(0).toJSON());
		return jo;
	}

	public void parseJSON(JSONObject jo) throws JSONException {
		entities.clear();
		entities.add(new Entity());
		if(jo.has("scene")) jo = jo.getJSONObject("scene");
		entities.get(0).parseJSON(jo);
	}
}
