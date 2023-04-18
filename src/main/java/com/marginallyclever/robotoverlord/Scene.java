package com.marginallyclever.robotoverlord;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.*;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.parameters.ColorEntity;
import com.marginallyclever.robotoverlord.parameters.StringEntity;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * {@link Scene} is a container for all the {@link Entity} in a project.  It also contains the absolute path on disk
 * for the root of the Scene.  All assets are relative to this path.
 * @author Dan Royer
 * @since 1.6.0
 */
public class Scene extends Entity {
	private static final Logger logger = LoggerFactory.getLogger(Scene.class);
	private final StringEntity scenePath = new StringEntity("Scene Path", "");

	private final ColorEntity ambientLight = new ColorEntity("Ambient light",0.2,0.2,0.2,1);
	private final BooleanEntity showWorldOrigin = new BooleanEntity("Show world origin",false);
	private final MaterialComponent defaultMaterial = new MaterialComponent();

	private final List<SceneChangeListener> sceneChangeListeners = new ArrayList<>();
	
	public Scene() {
		super();
	}

	/**
	 * Initialize the scene with a path to the root of the project.
	 * @param absolutePath the absolute path to the root of the project.
	 */
	public Scene(String absolutePath) {
		super();
		setScenePath(absolutePath);
	}

	@Override
	public void render(GL2 gl2) {
		if(showWorldOrigin.get()) renderWorldOrigin(gl2);

		renderLights(gl2);
		renderAllEntities(gl2);
		// PASS 2: everything transparent?
		//renderAllBoundingBoxes(gl2);
	}

	private void renderWorldOrigin(GL2 gl2) {
		PrimitiveSolids.drawStar(gl2,10);
	}

	/**
	 * Recursively render all entities.
	 * @param gl2 the OpenGL context
	 */
	private void renderAllEntities(GL2 gl2) {
		defaultMaterial.render(gl2);
		Queue<Entity> toRender = new LinkedList<>(children);
		while(!toRender.isEmpty()) {
			Entity child = toRender.remove();
			renderEntity(gl2, child);
			toRender.addAll(child.getChildren());
		}
	}

	/**
	 * Does not render children.
	 * @param gl2 the OpenGL context
	 * @param entity the entity to render
	 */
	private void renderEntity(GL2 gl2, Entity entity) {
		gl2.glPushMatrix();

		PoseComponent pose = entity.findFirstComponent(PoseComponent.class);
		if(pose!=null) MatrixHelper.applyMatrix(gl2, pose.getWorld());

		renderOneEntityWithMaterial(gl2, entity);

		gl2.glPopMatrix();
	}

	private void renderOneEntityWithMaterial(GL2 gl2, Entity obj) {
		MaterialComponent mat = obj.findFirstComponent(MaterialComponent.class);
		if(mat==null) mat = obj.findFirstComponentInParents(MaterialComponent.class);
		if(mat!=null && mat.getEnabled()) mat.render(gl2);

		List<RenderComponent> renderComponents = obj.findAllComponents(RenderComponent.class);
		for(RenderComponent renderComponent : renderComponents) {
			if(renderComponent.getVisible()) renderComponent.render(gl2);
		}
	}

	private void renderLights(GL2 gl2) {
		// global ambient light
		gl2.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, ambientLight.getFloatArray(),0);

		int maxLights = getMaxLights(gl2);
		turnOffAllLights(gl2,maxLights);

		Queue<Entity> found = new LinkedList<>(children);
		int i=0;
		while(!found.isEmpty()) {
			Entity obj = found.remove();
			LightComponent light = obj.findFirstComponent(LightComponent.class);
			if(light!=null && light.getEnabled()) {
				light.setupLight(gl2,i++);
				if(i==maxLights) return;
			}
			found.addAll(obj.children);
		}
	}

	private void turnOffAllLights(GL2 gl2,int maxLights) {
		for(int i=0;i<maxLights;++i) {
			gl2.glDisable(GL2.GL_LIGHT0+i);
		}
	}

	/**
	 * @param listA all the cuboids being tested against the world.
	 * @return true if any cuboid in {@code listA} intersects any {@link AABB} in the world.
	 */
	public boolean collisionTest(ArrayList<AABB> listA) {
		// check all children
		for( Entity b : children) {
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
	
	@Override
	public void getView(ViewPanel view) {
		view.pushStack("Sc", true);
		view.add(scenePath).setReadOnly(true);
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

	/**
	 * Set the scene path.  This is the path to the directory containing the scene file.
	 * @param absolutePath the absolute path to the scene directory.
	 */
	public void setScenePath(String absolutePath) {
		File file = new File(absolutePath);
		if(!file.exists()) throw new RuntimeException("File does not exist: "+absolutePath);
		if(!file.isDirectory()) throw new RuntimeException("Not a directory: "+absolutePath);
		//if(!entities.isEmpty()) throw new RuntimeException("Cannot change the scene path when entities are present.");

		logger.debug("Setting scene path to "+absolutePath);
		scenePath.set(absolutePath);
	}

	public String getScenePath() {
		return scenePath.get();
	}

	/**
	 * Returns true if unCheckedAssetFilename is in the scene path.
	 * @param unCheckedAssetFilename a file that may or may not be within the scene path.
	 * @return true if unCheckedAssetFilename is in the scene path.
	 */
	public boolean isAssetPathInScenePath(String unCheckedAssetFilename) {
		Path input = Paths.get(unCheckedAssetFilename);
		Path scene = Paths.get(getScenePath());
		return input.toAbsolutePath().startsWith(scene.toAbsolutePath());
	}

	/**
	 * Displays a warning to the user if the asset is not within the scene path.
	 * @param unCheckedAssetFilename a file that may or may not be within the scene path.
	 */
	public void warnIfAssetPathIsNotInScenePath(String unCheckedAssetFilename) {
		if(isAssetPathInScenePath(unCheckedAssetFilename)) return;

		String message = Translator.get("Scene.AssetPathNotInScenePathWarning");
		message = message.replace("%1", unCheckedAssetFilename);
		message = message.replace("%2", getScenePath());
		logger.warn("asset "+unCheckedAssetFilename+" not in scene path: "+getScenePath());

		// try to show a pop-up if we have a display
		if(!GraphicsEnvironment.isHeadless()) {
			JOptionPane.showMessageDialog(
				null,
				message,
				Translator.get("Scene.AssetPathNotInScenePathWarningTitle"),
				JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Returns the relative path to the asset, or absolute if the asset is not within the scene path.
	 * @param unCheckedAssetFilename a file that may or may not be within the scene path.
	 * @return the relative path to the asset, or absolute if the asset is not within the scene path.
	 */
	public String removeScenePath(String unCheckedAssetFilename) {
		String scenePathValue = getScenePath();
		if(unCheckedAssetFilename.startsWith(scenePathValue)) {
			return unCheckedAssetFilename.substring(scenePathValue.length());
		}
		return unCheckedAssetFilename;
	}

	public String addScenePath(String fn) {
		return getScenePath() + fn;
	}

	public int getMaxLights(GL2 gl2) {
		IntBuffer intBuffer = IntBuffer.allocate(1);
		gl2.glGetIntegerv(GL2.GL_MAX_LIGHTS, intBuffer);
		return intBuffer.get();
	}

	/**
	 * test ray intersection with all entities in the scene.
	 * @param ray the ray to test.
	 */
	public List<RayHit> findRayIntersections(Ray ray) {
		List<RayHit> rayHits = new ArrayList<>();

		Queue<Entity> toTest = new LinkedList<>(children);
		while(!toTest.isEmpty()) {
			Entity child = toTest.remove();
			toTest.addAll(child.getChildren());
			List<ShapeComponent> shapes = child.findAllComponents(ShapeComponent.class);
			for(ShapeComponent shape : shapes) {
				RayHit hit = shape.intersect(ray);
				if(hit!=null) rayHits.add(hit);
			}
		}
		return rayHits;
	}

}
