package com.marginallyclever.ro3;

import com.marginallyclever.ro3.apps.viewport.OpenGL3Resource;
import com.marginallyclever.ro3.apps.viewport.ShaderFactory;
import com.marginallyclever.ro3.apps.viewport.ShaderProgramFactory;
import com.marginallyclever.ro3.listwithevents.ListWithEvents;
import com.marginallyclever.ro3.mesh.MeshFactory;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodefactory.NodeFactory;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;
import com.marginallyclever.ro3.physics.ODEPhysics;
import com.marginallyclever.ro3.texture.TextureFactory;

import javax.swing.event.EventListenerList;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link Registry} is a place to store global variables.
 */
public class Registry {
    public static final EventListenerList listeners = new EventListenerList();
    public static final TextureFactory textureFactory = new TextureFactory();
    public static final MeshFactory meshFactory = new MeshFactory();
    public static final ShaderFactory shaderFactory = new ShaderFactory();
    public static final ShaderProgramFactory shaderProgramFactory = new ShaderProgramFactory();
    public static final NodeFactory nodeFactory = new NodeFactory();
    private static Node scene = new Node("Scene");
    public static final ListWithEvents<Camera> cameras = new ListWithEvents<>();
    public static final ListWithEvents<Node> selection = new ListWithEvents<>();
    private static final ODEPhysics physics = new ODEPhysics();

    // a static final list of OpenGL3Reources that must thread-safe as it may be modified from the OpenGL thread while being read from the main thread.
    public static final List<OpenGL3Resource> toBeUnloaded = Collections.synchronizedList(new ArrayList<>());

    public static void start() {
        nodeFactory.clear();
        nodeFactory.scan("com.marginallyclever.ro3");
        nodeFactory.addKnownNodes();

        setScene(new Node("Scene"));
    }

    /**
     * reset is intended to be called when starting a new scene.  It clears out all existing data and
     * resets to a default state.
     */
    public static void reset() {
        selection.clear();

        // reset camera
        cameras.clear();
        Camera first = new Camera("Camera 1");
        cameras.add(first);
        double v = Math.sqrt(Math.pow(50,2)/3d); // match the viewport default orbit distance.
        first.setPosition(new Vector3d(v,v,v));
        first.lookAt(new Vector3d(0,0,0));

        // reset scene
        List<Node> toRemove2 = new ArrayList<>(scene.getChildren());
        for(Node n : toRemove2) {
            scene.removeChild(n);
        }

        textureFactory.removeSceneResources();
        meshFactory.removeSceneResources();
        shaderFactory.removeSceneResources();
        shaderProgramFactory.removeSceneResources();
        physics.reset();
    }

    public static void addSceneChangeListener(SceneChangeListener listener) {
        listeners.add(SceneChangeListener.class,listener);
    }

    public static void removeSceneChangeListener(SceneChangeListener listener) {
        listeners.remove(SceneChangeListener.class,listener);
    }

    public static void setScene(Node newScene) {
        fireBeforeSceneChange(newScene);
        reset();
        scene = newScene;
        fireAfterSceneChange(newScene);
    }

    private static void fireBeforeSceneChange(Node newScene) {
        for (SceneChangeListener listener : listeners.getListeners(SceneChangeListener.class)) {
            listener.beforeSceneChange(newScene);
        }
    }

    private static void fireAfterSceneChange(Node newScene) {
        for (SceneChangeListener listener : listeners.getListeners(SceneChangeListener.class)) {
            listener.afterSceneChange(newScene);
        }
    }

    public static Node getScene() {
        return scene;
    }

    public static ODEPhysics getPhysics() {
        return physics;
    }
}